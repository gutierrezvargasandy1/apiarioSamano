package com.ApiarioSamano.MicroServiceApiarios.factory;

import com.ApiarioSamano.MicroServiceApiarios.dto.DispositivoDTO.DispositivoRequestDTO;
import com.ApiarioSamano.MicroServiceApiarios.model.Dispositivo;
import org.springframework.stereotype.Component;

@Component
public class DispositivoFactory extends Factory<Dispositivo, DispositivoRequestDTO> {

    @Override
    public Dispositivo crear(DispositivoRequestDTO dto) {
        Dispositivo dispositivo = new Dispositivo();

        dispositivo.setDispositivoId(dto.getDispositivoId());
        dispositivo.setApiarioId(dto.getApiarioId());
        dispositivo.setTipo(dto.getTipo());
        dispositivo.setSensores(dto.getSensores());
        dispositivo.setActuadores(dto.getActuadores());
        dispositivo.setTimestamp(dto.getTimestamp());

        return dispositivo;
    }
}