package com.reserveflow.service;

import lombok.RequiredArgsConstructor;
import org.example.resourcebookingappbackend.DTO.AvailabilityResponse;
import org.example.resourcebookingappbackend.DTO.ResourceRequest;
import org.example.resourcebookingappbackend.DTO.ResourceResponse;
import org.example.resourcebookingappbackend.Exception.ResourceNotFoundException;
import org.example.resourcebookingappbackend.Model.ResourceCategory;
import org.example.resourcebookingappbackend.Model.Resources;
import org.example.resourcebookingappbackend.Repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {



        private final ResourceRepository repository;

        @Transactional(readOnly = true)
        public Page<ResourceResponse> search(ResourceCategory category, String location, Integer minCapacity, Pageable pageable) {

            Specification<Resources> spec = (root, query, cb) -> cb.isTrue(root.<Boolean>get("active"));

            if (category != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.<ResourceCategory>get("category"), category));
            }
            if (hasText(location)) {
                String l = "%" + location.trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.<String>get("location")), l));
            }
            if (minCapacity != null) {
                spec = spec.and((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.<Integer>get("capacity"), minCapacity));
            }

            return repository.findAll(spec, pageable).map(ResourceResponse::from);
        }

        @Transactional(readOnly = true)
        public ResourceResponse getById(Long id) {
            return ResourceResponse.from(findActive(id));
        }

        @Transactional(readOnly = true)
        public AvailabilityResponse getAvailability(Long id, LocalDate date) {
            Resources resource = findActive(id);
            return new AvailabilityResponse(resource.getId(), date,
                    resource.getOpeningTime(), resource.getClosingTime(),
                    findBookedSlots(id, date));
        }

        @Transactional
        public ResourceResponse create(ResourceRequest request) {
            validateHours(request);
            Resources resource = new Resources();
            copyValues(request, resource);
            return ResourceResponse.from(repository.save(resource));
        }

        @Transactional
        public ResourceResponse update(Long id, ResourceRequest request) {
            validateHours(request);
            Resources resource = findActive(id);
            copyValues(request, resource);
            return ResourceResponse.from(resource);
        }

        @Transactional
        public void delete(Long id) {
            findActive(id).setActive(false);
        }


        private Resources findActive(Long id) {
            return repository.findByIdAndActiveTrue(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));
        }

        private void validateHours(ResourceRequest request) {
            if (!request.closingTime().isAfter(request.openingTime())) {
                throw new IllegalArgumentException("Closing time must be after opening time");
            }
        }


        private void copyValues(ResourceRequest request, Resources resource) {
            resource.setName(request.name().trim());
            resource.setCategory(request.category());
            resource.setLocation(request.location().trim());
            resource.setCapacity(request.capacity());
            resource.setDescription(request.description());
            resource.setImageUrl(request.imageUrl());
            resource.setOpeningTime(request.openingTime());
            resource.setClosingTime(request.closingTime());
            resource.setAmenities(request.amenities() == null ? null : String.join(",", request.amenities()));
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }


        private List<AvailabilityResponse.TimeSlot> findBookedSlots(Long resourceId, LocalDate date) {
            return List.of();
        }
    }
