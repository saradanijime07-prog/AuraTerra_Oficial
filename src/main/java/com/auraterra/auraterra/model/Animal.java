package com.auraterra.auraterra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "animales")
@Data
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    @Size(max = 150, message = "La especie no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String especie;

    @Min(value = 0, message = "La edad no puede ser negativa")
    @Column
    private Integer edad;

    @Size(max = 80, message = "El estado de salud no puede superar los 80 caracteres")
    @Column(name = "estado_salud", nullable = false, length = 80)
    private String estadoSalud = "En observación";

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Size(max = 150, message = "La zona de rescate no puede superar los 150 caracteres")
    @Column(name = "zona_rescate", length = 150)
    private String zonaRescate;

    @Column(name = "fecha_ingreso", updatable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @PrePersist
    protected void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        if (this.fechaIngreso == null) {
            this.fechaIngreso = ahora;
        }
        this.fechaActualizacion = ahora;
        if (this.estadoSalud == null || this.estadoSalud.isBlank()) {
            this.estadoSalud = "En observación";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}