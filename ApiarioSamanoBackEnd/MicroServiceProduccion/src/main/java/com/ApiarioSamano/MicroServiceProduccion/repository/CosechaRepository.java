package com.ApiarioSamano.MicroServiceProduccion.repository;

import com.ApiarioSamano.MicroServiceProduccion.model.Cosecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CosechaRepository extends JpaRepository<Cosecha, Long> {
    List<Cosecha> findByLoteId(Long idLote);

    List<Cosecha> findByIdApiario(Long idApiario);

    List<Cosecha> findByFechaCosechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT c FROM Cosecha c WHERE c.lote.tipoProducto ILIKE %:tipoLote%")
    List<Cosecha> findByTipoLoteContaining(String tipoLote);

    @Query("SELECT SUM(c.cantidad) FROM Cosecha c WHERE c.lote.id = :idLote")
    Double sumCantidadByLoteId(Long idLote);

    @Query("SELECT c FROM Cosecha c WHERE c.calidad = :calidad")
    List<Cosecha> findByCalidad(String calidad);
}
