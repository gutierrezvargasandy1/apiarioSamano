package com.ApiarioSamano.MicroServiceApiarios.repository;

import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import com.ApiarioSamano.MicroServiceApiarios.model.RecetaMedicamento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaMedicamentoRepository extends JpaRepository<RecetaMedicamento, Long> {
    List<RecetaMedicamento> findByReceta(Receta receta);

    void deleteByReceta(Receta receta);
}