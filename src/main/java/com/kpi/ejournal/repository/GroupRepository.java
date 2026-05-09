package com.kpi.ejournal.repository;

import com.kpi.ejournal.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<GroupEntity, Long>
{
    boolean existsByCode(String code);
}