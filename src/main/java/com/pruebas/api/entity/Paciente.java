package com.pruebas.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "pacientes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pacienteId;

    @NonNull
    private String nombre;

    @NonNull
    private Integer edad;

    @NonNull
    private String correo;
}
