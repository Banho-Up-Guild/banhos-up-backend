package com.github.banhosup.service;

import com.github.banhosup.dto.appointments.request.AppointmentsRequestDTO;
import com.github.banhosup.dto.appointments.request.PetOfferingDTO;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.entities.Offering;
import com.github.banhosup.model.entities.Pet;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.EmployeeRepository;
import com.github.banhosup.repository.OfferingRepository;
import com.github.banhosup.repository.PetRepository;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentsServiceTest {

    @Test
    void allowsSameStartTimeForDifferentEmployees() {
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);
        PetRepository petRepository = mock(PetRepository.class);
        OfferingRepository offeringRepository = mock(OfferingRepository.class);
        AppointmentsPetOfferingRepository petOfferingRepository =
                mock(AppointmentsPetOfferingRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

        AppointmentsService service = new AppointmentsService(
                appointmentsRepository,
                petRepository,
                offeringRepository,
                petOfferingRepository,
                employeeRepository
        );

        LocalDateTime dateTime = LocalDateTime.now().plusDays(2);
        long employeeId = 13L;
        Pet pet = mock(Pet.class);
        Offering offering = mock(Offering.class);
        EmployeeEntity employee = mock(EmployeeEntity.class);
        when(employee.getId()).thenReturn(employeeId);

        when(appointmentsRepository.existsByEmployee_IdAndDateTimeAppointmentAndStatusNot(
                employeeId,
                dateTime,
                StatusAppointments.CANCELED
        )).thenReturn(false);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(offeringRepository.findById(2L)).thenReturn(Optional.of(offering));
        when(offering.getPreco()).thenReturn(new BigDecimal("180.00"));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(appointmentsRepository.save(any(Appointments.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentsRequestDTO request = new AppointmentsRequestDTO(
                dateTime,
                StatusAppointments.AWAITING_CONFIRMATION,
                "",
                employeeId,
                List.of(new PetOfferingDTO(1L, List.of(2L)))
        );

        Appointments created = service.saveAppointment(request);

        assertEquals(employee, created.getEmployee());
        verify(appointmentsRepository).existsByEmployee_IdAndDateTimeAppointmentAndStatusNot(
                eq(employeeId),
                eq(dateTime),
                eq(StatusAppointments.CANCELED)
        );
    }
}
