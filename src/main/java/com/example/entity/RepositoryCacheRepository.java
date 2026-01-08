package com.example.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RepositoryCacheRepository extends JpaRepository<RepositoryCache, Long> {
    // Query Cache By Full Name
    Optional<RepositoryCache> findByFullName(String fullName);
}
