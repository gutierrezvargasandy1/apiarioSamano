package com.example.MicroServiceGestorDeArchivos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.example.MicroServiceGestorDeArchivos.entity.Archivo;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, String> {

        List<Archivo> findByActivoTrue();

        Optional<Archivo> findById(String id);
}
