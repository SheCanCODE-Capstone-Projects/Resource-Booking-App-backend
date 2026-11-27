package org.example.resourcebookingappbackend.Service.Resource;

import org.example.resourcebookingappbackend.Model.Resource;
import org.springframework.data.jpa.domain.Specification;

public final class ResourceSpecifications {

    private ResourceSpecifications() {
    }

    public static Specification<Resource> hasCategory(String category) {
        return (root, query, cb) -> category == null || category.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
    }

    public static Specification<Resource> hasLocation(String location) {
        return (root, query, cb) -> location == null || location.isBlank()
                ? null
                : cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%");
    }

    public static Specification<Resource> hasMinCapacity(Integer minCapacity) {
        return (root, query, cb) -> minCapacity == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity);
    }

    public static Specification<Resource> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? null
                : cb.equal(root.get("active"), active);
    }
}