package com.ApiarioSamano.MicroServiceApiarios.factory;

public abstract class Factory<T, R> {
    public abstract T crear(R data);
}
