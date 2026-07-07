package com.horseracing.repository;

import com.horseracing.entity.NaiNgua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository cho entity NaiNgua (Jockey/nài ngựa).
 */
@Repository
public interface NaiNguaRepository extends JpaRepository<NaiNgua, String>, JpaSpecificationExecutor<NaiNgua> {

    Page<NaiNgua> findByMaChuNgua(String maChuNgua, Pageable pageable);

    java.util.List<NaiNgua> findByMaChuNgua(String maChuNgua);

    boolean existsBySoGiayPhep(String soGiayPhep);

    boolean existsBySoGiayPhepAndMaNaiNguaNot(String soGiayPhep, String maNaiNgua);
}
