package com.ApiarioSamano.MicroServiceAlmacen.repository;

import com.ApiarioSamano.MicroServiceAlmacen.model.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

    // Buscar un almacén por número de seguimiento
    Almacen findByNumeroSeguimiento(String numeroSeguimiento);
}
