package com.github.banhosup.model.entities.appointments;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.banhosup.model.enums.StatusAppointments;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private StatusAppointments status;

    @Column(nullable = false)
    private String observation;

    public Appointments(LocalDateTime dateTimeAppointment, StatusAppointments status, String observation) {
        this.dateTimeAppointment = dateTimeAppointment;
        this.status = status;
        this.observation = observation;
    }
}
