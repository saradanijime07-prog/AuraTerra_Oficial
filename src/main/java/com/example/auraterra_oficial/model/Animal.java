package com.example.auraterra_oficial.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "animales")
@Data
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    @Column(nullable = false)
    private String especie;

    private Integer edad;

    @Column(name = "estado_salud")
    private String estadoSalud;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}