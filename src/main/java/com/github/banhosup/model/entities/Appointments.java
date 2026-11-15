package com.github.banhosup.model.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.banhosup.model.enums.StatusAppointment;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class Appointments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "A data e a hora do agendamento são obrigatórios")
    @Future(message = "O agendamento deve ser para uma data futura")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    @Column(nullable = false)
    private LocalDateTime dateTimeAppointment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAppointment status;

    @Column(nullable = false)
    private String observation;
}
