package com.ApiarioSamano.MicroServiceApiarios.repository;

import com.ApiarioSamano.MicroServiceApiarios.model.Apiarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiariosRepository extends JpaRepository<Apiarios, Long> {
}
