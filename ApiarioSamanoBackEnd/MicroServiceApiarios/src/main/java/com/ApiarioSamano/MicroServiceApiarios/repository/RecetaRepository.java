package com.ApiarioSamano.MicroServiceApiarios.repository;

import com.ApiarioSamano.MicroServiceApiarios.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {

}