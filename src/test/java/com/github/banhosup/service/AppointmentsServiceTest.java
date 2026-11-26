package com.github.banhosup.service;

import com.github.banhosup.dto.appointments.request.AppointmentsRequestDTO;
import com.github.banhosup.dto.appointments.request.PetOfferingDTO;
import com.github.banhosup.dto.availability.AvailabilityCalendarDayResponse;
import com.github.banhosup.model.entities.CustomerEntity;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.entities.Offering;
import com.github.banhosup.model.entities.Pet;
import com.github.banhosup.model.entities.StoreEntity;
import com.github.banhosup.model.entities.UserEntity;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.CustomerRepository;
import com.github.banhosup.repository.EmployeeRepository;
import com.github.banhosup.repository.OfferingRepository;
import com.github.banhosup.repository.PetRepository;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentsServiceTest {

    @Test
    void createsAppointmentForAuthenticatedCustomerUsingServerSidePrice() {

        AppointmentsRepository appointmentsRepository =
                mock(AppointmentsRepository.class);

        PetRepository petRepository = mock(PetRepository.class);

        OfferingRepository offeringRepository = mock(OfferingRepository.class);

        AppointmentsPetOfferingRepository petOfferingRepository =
                mock(AppointmentsPetOfferingRepository.class);

        EmployeeRepository employeeRepository =
                mock(EmployeeRepository.class);

        CustomerRepository customerRepository =
                mock(CustomerRepository.class);

        AvailabilityService availabilityService =
                mock(AvailabilityService.class);

        AppointmentsService service = new AppointmentsService(
                appointmentsRepository,
                petRepository,
                offeringRepository,
                petOfferingRepository,
                employeeRepository,
                customerRepository,
                availabilityService
        );

        LocalDateTime dateTime = LocalDateTime.now().plusDays(2)
                .withSecond(0)
                .withNano(0);

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("cliente@teste.com");

        CustomerEntity customer = mock(CustomerEntity.class);

        when(customer.getId()).thenReturn(1L);

        when(customerRepository.findByUser_Email("cliente@teste.com"))
                .thenReturn(Optional.of(customer));

        UserEntity employeeUser = mock(UserEntity.class);

        when(employeeUser.isActive()).thenReturn(true);

        StoreEntity store = mock(StoreEntity.class);

        when(store.getId()).thenReturn(5L);
        when(store.isAtiva()).thenReturn(true);

        EmployeeEntity employee = mock(EmployeeEntity.class);

        when(employee.getId()).thenReturn(13L);
        when(employee.getUser()).thenReturn(employeeUser);
        when(employee.isTeamActive()).thenReturn(true);
        when(employee.getStore()).thenReturn(store);

        when(employeeRepository.findByIdForUpdate(13L))
                .thenReturn(Optional.of(employee));

        Pet pet = mock(Pet.class);

        when(pet.getDono()).thenReturn(customer);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        Offering offering = mock(Offering.class);

        when(offering.getNome()).thenReturn("Banho");
        when(offering.getPreco()).thenReturn(new BigDecimal("180.00"));
        when(offering.getDuracaoMinutos()).thenReturn(60);
        when(offering.getAtivo()).thenReturn(true);

        when(offeringRepository.findById(2L))
                .thenReturn(Optional.of(offering));

        when(availabilityService.consultarCalendario(
                5L,
                13L,
                java.time.YearMonth.from(dateTime),
                60
        )).thenReturn(List.of(
                new AvailabilityCalendarDayResponse(
                        dateTime.toLocalDate(),
                        List.of(dateTime.toLocalTime()),
                        List.of()
                )
        ));

        when(appointmentsRepository.save(any(Appointments.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentsRequestDTO request = new AppointmentsRequestDTO(
                dateTime,
                StatusAppointments.AWAITING_CONFIRMATION,
                "",
                13L,
                List.of(new PetOfferingDTO(1L, List.of(2L)))
        );

        Appointments created = service.saveAppointment(request, authentication);

        assertEquals(employee, created.getEmployee());
        assertEquals(store, created.getStore());
        assertEquals(new BigDecimal("180.00"), created.getTotalPrice());
        assertEquals(StatusAppointments.AWAITING_CONFIRMATION, created.getStatus());

        verify(appointmentsRepository).save(any(Appointments.class));
        verify(petOfferingRepository).saveAll(any());
    }
}