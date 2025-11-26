package com.ApiarioSamano.MicroServiceApiarios.factory;

import org.springframework.stereotype.Component;

import com.ApiarioSamano.MicroServiceApiarios.model.HistorialRecetas;

@Component
public class HistorialRecetasFactory implements Factory<HistorialRecetas, Void> {

    @Override
    public HistorialRecetas crear(Void unused) {
        return new HistorialRecetas();
    }
}