package com.ApiarioSamano.MicroServiceProduccion.repository;

import com.ApiarioSamano.MicroServiceProduccion.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    @Query("SELECT p FROM Producto p WHERE p.lote.id = :idLote")
    List<Producto> findByLoteId(Long idLote);

    Optional<Producto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

    @Query("SELECT p FROM Producto p JOIN p.lote l WHERE l.id = :idLote AND p.activo = true")
    List<Producto> findProductosActivosByLoteId(Long idLote);

    @Query("SELECT p FROM Producto p WHERE p.nombre ILIKE %:nombre% AND p.activo = true")
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p.foto FROM Producto p WHERE p.id = :id")
    Optional<byte[]> findFotoById(Long id);

    @Query("SELECT COUNT(p) > 0 FROM Producto p WHERE p.codigoBarras = :codigoBarras AND p.lote.id = :idLote")
    boolean existsByCodigoBarrasAndLoteId(String codigoBarras, Long idLote);
}