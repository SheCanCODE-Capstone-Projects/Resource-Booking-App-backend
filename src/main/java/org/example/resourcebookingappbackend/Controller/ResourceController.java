package org.example.resourcebookingappbackend.Controller; // folder (package) where this file lives

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.resourcebookingappbackend.DTO.AvailabilityResponse;
import org.example.resourcebookingappbackend.DTO.ResourceRequest;
import org.example.resourcebookingappbackend.DTO.ResourceResponse;
import org.example.resourcebookingappbackend.Model.ResourceCategory;
import org.example.resourcebookingappbackend.Service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@Tag(name = "Resources", description = "Discover and manage bookable spaces and equipment")
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService service;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "capacity", "location", "category");

    @Operation(summary = "List resources",
            description = "Returns a page of active resources. All filters are optional and can be combined.")
    @ApiResponse(responseCode = "200", description = "Page of resources returned") // what a success looks like
    @GetMapping
    public Page<ResourceResponse> getAll(
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) ResourceCategory category,
            @Parameter(description = "Filter by location (part of the name, ignores upper/lower case)", example = "Kigali")
            @RequestParam(required = false) String location,
            @Parameter(description = "Minimum number of people the resource must hold")
            @RequestParam(required = false) Integer capacity,
            @Parameter(description = "Page number, starts at 0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page (1 to 50)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by",
                    schema = @Schema(allowableValues = {"name", "capacity", "location", "category"}, defaultValue = "name"))
            @RequestParam(defaultValue = "name") String sortBy) {
        String sortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "name";
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(sortField));
        return service.search(category, location, capacity, pageable);
    }

    @Operation(summary = "Get one resource", description = "Returns the details of a single active resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource found"),
            @ApiResponse(responseCode = "404", description = "Resource not found or deleted")
    })
    @GetMapping("/{id}")
    public ResourceResponse getById(
            @Parameter(description = "Id of the resource")
            @PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Get availability",
            description = "Returns the opening hours of the resource for a date, plus the time slots already booked.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability returned"),
            @ApiResponse(responseCode = "400", description = "Date is missing or has a wrong format"),
            @ApiResponse(responseCode = "404", description = "Resource not found or deleted")
    })
    @GetMapping("/{id}/availability")
    public AvailabilityResponse getAvailability(
            @Parameter(description = "Id of the resource")
            @PathVariable Long id,
            @Parameter(description = "Date to check, format yyyy-MM-dd")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getAvailability(id, date);
    }


    @Operation(summary = "Create a resource", description = "Adds a new resource. Intended for administrators only.") // endpoint description
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "400", description = "Invalid data, e.g. missing field or closing time before opening time") // error
    })

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse create(@Valid @RequestBody ResourceRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Update a resource", description = "Replaces all the details of an existing resource. Intended for administrators only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Resource not found or deleted")
    })

    @PutMapping("/{id}")
    public ResourceResponse update(
            @Parameter(description = "Id of the resource to update", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a resource", description = "Soft delete: the resource is hidden but kept in the database. Intended for administrators only.") // endpoint description
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resource deleted"),
            @ApiResponse(responseCode = "404", description = "Resource not found or already deleted")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Id of the resource to delete", example = "1")
            @PathVariable Long id) {
        service.delete(id);
    }
}