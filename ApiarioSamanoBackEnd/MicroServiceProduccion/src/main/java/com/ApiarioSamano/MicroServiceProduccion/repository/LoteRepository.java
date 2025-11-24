package com.ApiarioSamano.MicroServiceProduccion.repository;


import com.ApiarioSamano.MicroServiceProduccion.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepository extends JpaRepository<Lote, Long> {
}
