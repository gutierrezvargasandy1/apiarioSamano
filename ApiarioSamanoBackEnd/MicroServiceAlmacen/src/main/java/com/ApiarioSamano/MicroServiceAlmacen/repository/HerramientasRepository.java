package com.ApiarioSamano.MicroServiceAlmacen.repository;

import com.ApiarioSamano.MicroServiceAlmacen.model.Herramientas;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HerramientasRepository extends JpaRepository<Herramientas, Long> {

    // Buscar todas las herramientas de un almacén específico
    List<Herramientas> findByAlmacen(Almacen almacen);

    // Buscar herramientas por nombre (búsqueda parcial, ignorando mayúsculas)
    List<Herramientas> findByNombreContainingIgnoreCase(String nombre);

    List<Herramientas> findByIdProveedor(Integer idProveedor);

}
