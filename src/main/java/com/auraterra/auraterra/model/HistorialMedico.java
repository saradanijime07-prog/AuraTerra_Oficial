package com.auraterra.auraterra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "historial_medico")
public class HistorialMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(max = 200, message = "El diagnóstico no puede superar los 200 caracteres")
    @Column(name = "diagnostico", nullable = false, length = 200)
    private String diagnostico;

    @Column(name = "tratamiento", columnDefinition = "TEXT")
    private String descripcionTratamiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Positive(message = "El peso debe ser mayor que cero")
    @Column(name = "peso_kg", precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Size(max = 80, message = "El estado resultante no puede superar los 80 caracteres")
    @Column(name = "estado_resultante", length = 80)
    private String estadoResultante;

    @Column(name = "fecha_control", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    public HistorialMedico() {
    }

    public HistorialMedico(
            String diagnostico,
            String descripcionTratamiento,
            String observaciones,
            BigDecimal pesoKg,
            String estadoResultante,
            LocalDate fecha,
            Animal animal) {
        this.diagnostico = diagnostico;
        this.descripcionTratamiento = descripcionTratamiento;
        this.observaciones = observaciones;
        this.pesoKg = pesoKg;
        this.estadoResultante = estadoResultante;
        this.fecha = fecha;
        this.animal = animal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getDescripcionTratamiento() {
        return descripcionTratamiento;
    }

    public void setDescripcionTratamiento(String descripcionTratamiento) {
        this.descripcionTratamiento = descripcionTratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public String getEstadoResultante() {
        return estadoResultante;
    }

    public void setEstadoResultante(String estadoResultante) {
        this.estadoResultante = estadoResultante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }
}