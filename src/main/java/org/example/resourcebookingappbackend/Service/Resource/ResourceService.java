package org.example.resourcebookingappbackend.Service.Resource;

import org.example.resourcebookingappbackend.DTO.Resource.ResourceCreateRequest;
import org.example.resourcebookingappbackend.DTO.Resource.ResourceResponse;
import org.example.resourcebookingappbackend.DTO.Resource.ResourceUpdateRequest;
import org.example.resourcebookingappbackend.Exception.ResourceNotFoundException;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Model.Resource;
import org.example.resourcebookingappbackend.Repository.ResourceRepository;
import org.example.resourcebookingappbackend.Security.AccessControlService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final AccessControlService accessControlService;

    public ResourceService(ResourceRepository resourceRepository, AccessControlService accessControlService) {
        this.resourceRepository = resourceRepository;
        this.accessControlService = accessControlService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Transactional
    public ResourceResponse createResource(ResourceCreateRequest request) {

        validateOperatingHours(request.getOpeningTime(), request.getClosingTime());

        Account owner = accessControlService.getAuthenticatedAccount();

        Resource resource = Resource.builder()
                .owner(owner)
                .name(request.getName().trim())
                .category(request.getCategory().trim())
                .location(request.getLocation().trim())
                .capacity(request.getCapacity())
                .amenities(request.getAmenities())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .active(true)
                .build();

        return mapToResponse(resourceRepository.saveAndFlush(resource));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<ResourceResponse> listResources(String category, String location, Integer minCapacity, Boolean active) {

        Specification<Resource> spec = Specification
                .where(ResourceSpecifications.hasCategory(category))
                .and(ResourceSpecifications.hasLocation(location))
                .and(ResourceSpecifications.hasMinCapacity(minCapacity))
                .and(ResourceSpecifications.isActive(active));

        return resourceRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResourceResponse getResource(UUID resourceId) {
        return mapToResponse(findResourceOrThrow(resourceId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Transactional
    public ResourceResponse updateResource(UUID resourceId, ResourceUpdateRequest request) {

        validateOperatingHours(request.getOpeningTime(), request.getClosingTime());

        Account authenticated = accessControlService.getAuthenticatedAccount();
        Resource resource = findResourceOrThrow(resourceId);

        accessControlService.assertOwnerOrAdmin(authenticated, resource.getOwner().getId());

        resource.setName(request.getName().trim());
        resource.setCategory(request.getCategory().trim());
        resource.setLocation(request.getLocation().trim());
        resource.setCapacity(request.getCapacity());
        resource.setAmenities(request.getAmenities());
        resource.setDescription(request.getDescription());
        resource.setImageUrl(request.getImageUrl());
        resource.setOpeningTime(request.getOpeningTime());
        resource.setClosingTime(request.getClosingTime());

        return mapToResponse(resourceRepository.save(resource));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Transactional
    public void deactivateResource(UUID resourceId) {

        Account authenticated = accessControlService.getAuthenticatedAccount();
        Resource resource = findResourceOrThrow(resourceId);

        accessControlService.assertOwnerOrAdmin(authenticated, resource.getOwner().getId());

        resource.setActive(false);

        resourceRepository.save(resource);
    }

    private Resource findResourceOrThrow(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }

    private void validateOperatingHours(java.time.LocalTime openingTime, java.time.LocalTime closingTime) {
        if (!openingTime.isBefore(closingTime)) {
            throw new IllegalArgumentException("Opening time must be before closing time");
        }
    }

    private ResourceResponse mapToResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .ownerId(resource.getOwner().getId())
                .name(resource.getName())
                .category(resource.getCategory())
                .location(resource.getLocation())
                .capacity(resource.getCapacity())
                .amenities(resource.getAmenities())
                .description(resource.getDescription())
                .imageUrl(resource.getImageUrl())
                .openingTime(resource.getOpeningTime())
                .closingTime(resource.getClosingTime())
                .active(resource.isActive())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}