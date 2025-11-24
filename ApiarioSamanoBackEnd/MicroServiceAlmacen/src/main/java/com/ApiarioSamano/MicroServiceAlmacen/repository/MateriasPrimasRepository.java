package com.ApiarioSamano.MicroServiceAlmacen.repository;

import com.ApiarioSamano.MicroServiceAlmacen.model.MateriasPrimas;
import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MateriasPrimasRepository extends JpaRepository<MateriasPrimas, Long> {

    // Buscar todas las materias primas de un almacén específico
    List<MateriasPrimas> findByAlmacen(Almacen almacen);

    // Buscar materias primas por nombre (búsqueda parcial, ignorando mayúsculas)
    List<MateriasPrimas> findByNombreContainingIgnoreCase(String nombre);

    // Buscar materias primas por proveedor
    List<MateriasPrimas> findByIdProveedor(Integer idProveedor);
}
