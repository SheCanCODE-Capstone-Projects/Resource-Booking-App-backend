package org.example.resourcebookingappbackend.Enum;

/**
 * Roles an administrator may grant through the API. ADMIN is intentionally
 * excluded - it only ever exists as a seeded database credential and is never
 * assignable or visible through any endpoint.
 */
public enum AssignableRole {
    PUBLISHER, USER
}
