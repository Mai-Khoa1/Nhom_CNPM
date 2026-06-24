package com.horseracing.repository;

import com.horseracing.entity.TepTin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TepTinRepository extends JpaRepository<TepTin, String> {
}
