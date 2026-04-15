package com.example.thexuong.repository;

import com.example.thexuong.entity.RoleGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleGroupRepository extends JpaRepository<RoleGroup, Long> {
    Optional<RoleGroup> findByName(String name);

    @Override
    @EntityGraph(attributePaths = {"roles"})
    List<RoleGroup> findAll();
}
