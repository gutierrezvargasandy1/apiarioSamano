package com.ApiarioSamano.MicroServiceAlmacen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import com.ApiarioSamano.MicroServiceAlmacen.model.Medicamento;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    // Buscar todos los medicamentos de un almacen específico
    List<Medicamento> findByAlmacen(Almacen almacen);

    // Buscar medicamentos por nombre (opcional)
    List<Medicamento> findByNombreContainingIgnoreCase(String nombre);

    List<Medicamento> findByIdProveedor(Integer idProveedor);

}
