package com.ApiarioSamano.MicroServiceApiarios.repository;

import com.ApiarioSamano.MicroServiceApiarios.model.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {
}
