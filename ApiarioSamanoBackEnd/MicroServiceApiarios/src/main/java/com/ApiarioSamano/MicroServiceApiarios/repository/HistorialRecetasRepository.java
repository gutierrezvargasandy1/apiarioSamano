package com.ApiarioSamano.MicroServiceApiarios.repository;

import com.ApiarioSamano.MicroServiceApiarios.model.HistorialMedico;
import com.ApiarioSamano.MicroServiceApiarios.model.HistorialRecetas;
import com.ApiarioSamano.MicroServiceApiarios.model.Receta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialRecetasRepository extends JpaRepository<HistorialRecetas, Long> {
    void deleteByReceta(Receta receta);

    List<HistorialRecetas> findByHistorialMedico(HistorialMedico historialMedico);

    @Query("SELECT hr FROM HistorialRecetas hr WHERE hr.historialMedico.id = :idHistorial")
    List<HistorialRecetas> findByHistorialMedicoId(@Param("idHistorial") Long idHistorial);
}
