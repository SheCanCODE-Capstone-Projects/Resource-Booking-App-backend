package org.example.resourcebookingappbackend.Repository;

import org.example.resourcebookingappbackend.Enum.RoleRequestStatus;
import org.example.resourcebookingappbackend.Model.RoleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleRequestRepository extends JpaRepository<RoleRequest, UUID> {

    boolean existsByAccount_IdAndStatus(UUID accountId, RoleRequestStatus status);

    List<RoleRequest> findAllByAccount_IdOrderByRequestedAtDesc(UUID accountId);

    List<RoleRequest> findAllByStatusOrderByRequestedAtAsc(RoleRequestStatus status);
}