package org.example.resourcebookingappbackend.Repository;

import org.example.resourcebookingappbackend.Model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceRepository
        extends JpaRepository<Resources, Long>, JpaSpecificationExecutor<Resources> {


    Optional<Resources> findByIdAndActiveTrue(Long id);
}