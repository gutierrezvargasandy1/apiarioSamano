package com.ApiarioSamano.MicroServiceProveedores.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "fotografia")
    private byte[] fotografia;

    @Column(name = "nombreEmpresa", nullable = false, length = 200)
    private String nombreEmpresa;

    @Column(name = "nombreRepresentante", nullable = false, length = 200)
    private String nombreRepresentante;

    @Column(name = "numTelefono", length = 20)
    private String numTelefono;

    @Column(name = "materialProvee", length = 200)
    private String materialProvee;
}
