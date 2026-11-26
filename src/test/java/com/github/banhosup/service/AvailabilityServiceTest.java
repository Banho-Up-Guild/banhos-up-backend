package com.github.banhosup.service;

import com.github.banhosup.dto.availability.AvailabilityCalendarDayResponse;
import com.github.banhosup.dto.availability.AvailableEmployeeResponse;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.entities.StoreBusinessHoursEntity;
import com.github.banhosup.model.entities.StoreEntity;
import com.github.banhosup.model.entities.UserEntity;
import com.github.banhosup.model.entities.Offering;
import com.github.banhosup.model.entities.Pet;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.EmployeeRepository;
import com.github.banhosup.repository.StoreBusinessHoursRepository;
import com.github.banhosup.repository.StoreRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvailabilityServiceTest {

    @Test
    void calendarWithEmployeeZeroUsesAllActiveEmployeesInTheStore() {
        StoreBusinessHoursRepository hoursRepository = mock(StoreBusinessHoursRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);
        AvailabilityService service = new AvailabilityService(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository
        );

        long storeId = 1L;
        YearMonth yearMonth = YearMonth.now().plusMonths(2);
        StoreEntity store = mock(StoreEntity.class);
        when(store.isAtiva()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreBusinessHoursEntity weeklyHours = mock(StoreBusinessHoursEntity.class);
        when(weeklyHours.getDayOfWeek()).thenReturn(yearMonth.atDay(1).getDayOfWeek().getValue());
        when(weeklyHours.isClosed()).thenReturn(false);
        when(weeklyHours.getOpeningTime()).thenReturn(LocalTime.of(9, 0));
        when(weeklyHours.getClosingTime()).thenReturn(LocalTime.of(9, 30));
        when(hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(storeId))
                .thenReturn(List.of(weeklyHours));

        EmployeeEntity employee = mock(EmployeeEntity.class);
        when(employee.getId()).thenReturn(10L);
        when(employeeRepository.findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(storeId))
                .thenReturn(List.of(employee));
        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        List.of(10L),
                        yearMonth.atDay(1).atStartOfDay(),
                        yearMonth.atEndOfMonth().atTime(LocalTime.MAX),
                        com.github.banhosup.model.enums.StatusAppointments.CANCELED
                ))
                .thenReturn(List.of());

        List<AvailabilityCalendarDayResponse> result = service.consultarCalendario(
                storeId,
                0L,
                yearMonth,
                30
        );

        AvailabilityCalendarDayResponse firstDay = result.stream()
                .filter(day -> day.date().equals(yearMonth.atDay(1)))
                .findFirst()
                .orElseThrow();

        assertEquals(List.of(LocalTime.of(9, 0)), firstDay.hours());
        assertEquals(List.of(), firstDay.unavailableHours());
    }

    @Test
    void calendarOnlyBlocksTheExactAppointmentStart() {
        StoreBusinessHoursRepository hoursRepository = mock(StoreBusinessHoursRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);
        AvailabilityService service = new AvailabilityService(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository
        );

        long storeId = 1L;
        YearMonth yearMonth = YearMonth.now().plusMonths(2);
        StoreEntity store = mock(StoreEntity.class);
        when(store.isAtiva()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreBusinessHoursEntity weeklyHours = mock(StoreBusinessHoursEntity.class);
        when(weeklyHours.getDayOfWeek()).thenReturn(yearMonth.atDay(1).getDayOfWeek().getValue());
        when(weeklyHours.isClosed()).thenReturn(false);
        when(weeklyHours.getOpeningTime()).thenReturn(LocalTime.of(9, 0));
        when(weeklyHours.getClosingTime()).thenReturn(LocalTime.of(10, 0));
        when(hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(storeId))
                .thenReturn(List.of(weeklyHours));

        EmployeeEntity employee = mock(EmployeeEntity.class);
        when(employee.getId()).thenReturn(10L);
        when(employeeRepository.findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(storeId))
                .thenReturn(List.of(employee));

        Appointments appointment = mock(Appointments.class);
        when(appointment.getId()).thenReturn(100L);
        when(appointment.getEmployee()).thenReturn(employee);
        when(appointment.getDateTimeAppointment())
                .thenReturn(yearMonth.atDay(1).atTime(9, 0));

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        List.of(10L),
                        yearMonth.atDay(1).atStartOfDay(),
                        yearMonth.atEndOfMonth().atTime(LocalTime.MAX),
                        StatusAppointments.CANCELED
                ))
                .thenReturn(List.of(appointment));

        List<AvailabilityCalendarDayResponse> result = service.consultarCalendario(
                storeId,
                0L,
                yearMonth,
                30
        );

        AvailabilityCalendarDayResponse firstDay = result.stream()
                .filter(day -> day.date().equals(yearMonth.atDay(1)))
                .findFirst()
                .orElseThrow();

        assertEquals(List.of(LocalTime.of(9, 30)), firstDay.hours());
        assertEquals(List.of(LocalTime.of(9, 0)), firstDay.unavailableHours());
    }

    @Test
    void assignmentAllowsNextSlotAfterAppointmentStart() {
        StoreBusinessHoursRepository hoursRepository = mock(StoreBusinessHoursRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);
        AvailabilityService service = new AvailabilityService(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository
        );

        long storeId = 1L;
        LocalDateTime appointmentTime = YearMonth.now().plusMonths(2).atDay(1).atTime(9, 0);
        LocalDateTime requestedTime = appointmentTime.plusMinutes(30);

        StoreEntity store = mock(StoreEntity.class);
        when(store.isAtiva()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreBusinessHoursEntity weeklyHours = mock(StoreBusinessHoursEntity.class);
        when(weeklyHours.getDayOfWeek()).thenReturn(requestedTime.getDayOfWeek().getValue());
        when(weeklyHours.isClosed()).thenReturn(false);
        when(weeklyHours.getOpeningTime()).thenReturn(LocalTime.of(9, 0));
        when(weeklyHours.getClosingTime()).thenReturn(LocalTime.of(12, 0));
        when(hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(storeId))
                .thenReturn(List.of(weeklyHours));

        EmployeeEntity employee = employee(10L, "Ana");
        when(employeeRepository.findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(storeId))
                .thenReturn(List.of(employee));

        Appointments appointment = appointment(100L, employee, appointmentTime);
        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        List.of(10L),
                        requestedTime.toLocalDate().atStartOfDay(),
                        requestedTime.toLocalDate().atTime(LocalTime.MAX),
                        StatusAppointments.CANCELED
                ))
                .thenReturn(List.of(appointment));

        AvailableEmployeeResponse result = service.selecionarFuncionario(
                storeId,
                0L,
                requestedTime,
                30
        );

        assertEquals(10L, result.employeeId());
        assertEquals("Ana", result.employeeName());
    }

    @Test
    void assignmentChoosesAvailableEmployeeWithFewerFutureAppointments() {
        StoreBusinessHoursRepository hoursRepository = mock(StoreBusinessHoursRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);

        AvailabilityService service = new AvailabilityService(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository
        );

        long storeId = 1L;
        LocalDateTime dateTime = YearMonth.now().plusMonths(2).atDay(1).atTime(9, 0);

        StoreEntity store = mock(StoreEntity.class);
        when(store.isAtiva()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreBusinessHoursEntity weeklyHours = mock(StoreBusinessHoursEntity.class);
        when(weeklyHours.getDayOfWeek()).thenReturn(dateTime.getDayOfWeek().getValue());
        when(weeklyHours.isClosed()).thenReturn(false);
        when(weeklyHours.getOpeningTime()).thenReturn(LocalTime.of(9, 0));
        when(weeklyHours.getClosingTime()).thenReturn(LocalTime.of(12, 0));
        when(hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(storeId))
                .thenReturn(List.of(weeklyHours));

        EmployeeEntity firstEmployee = employee(10L, "Ana");
        EmployeeEntity secondEmployee = employee(11L, "Bia");

        when(employeeRepository.findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(storeId))
                .thenReturn(List.of(firstEmployee, secondEmployee));

        Appointments firstAppointment = appointment(
                100L,
                firstEmployee,
                dateTime.withHour(10)
        );

        Appointments secondAppointment = appointment(
                101L,
                firstEmployee,
                dateTime.withHour(10).plusMinutes(30)
        );

        Appointments thirdAppointment = appointment(
                102L,
                secondEmployee,
                dateTime.withHour(10)
        );

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        List.of(10L, 11L),
                        dateTime.toLocalDate().atStartOfDay(),
                        dateTime.toLocalDate().atTime(LocalTime.MAX),
                        StatusAppointments.CANCELED
                ))
                .thenReturn(List.of(firstAppointment, secondAppointment, thirdAppointment));

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentAfterAndStatusNot(
                        anyList(),
                        any(LocalDateTime.class),
                        eq(StatusAppointments.CANCELED)
                ))
                .thenReturn(List.of(firstAppointment, secondAppointment, thirdAppointment));

        AvailableEmployeeResponse result = service.selecionarFuncionario(
                storeId,
                0L,
                dateTime,
                30
        );

        assertEquals(11L, result.employeeId());
        assertEquals("Bia", result.employeeName());
    }

    @Test
    void assignmentCountsFutureAppointmentsAcrossDifferentDays() {
        StoreBusinessHoursRepository hoursRepository = mock(StoreBusinessHoursRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);

        AvailabilityService service = new AvailabilityService(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository
        );

        long storeId = 1L;
        LocalDateTime requestedTime = YearMonth.now().plusMonths(2).atDay(1).atTime(9, 0);

        StoreEntity store = mock(StoreEntity.class);
        when(store.isAtiva()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreBusinessHoursEntity weeklyHours = mock(StoreBusinessHoursEntity.class);
        when(weeklyHours.getDayOfWeek()).thenReturn(requestedTime.getDayOfWeek().getValue());
        when(weeklyHours.isClosed()).thenReturn(false);
        when(weeklyHours.getOpeningTime()).thenReturn(LocalTime.of(9, 0));
        when(weeklyHours.getClosingTime()).thenReturn(LocalTime.of(12, 0));
        when(hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(storeId))
                .thenReturn(List.of(weeklyHours));

        EmployeeEntity employeeWithMoreFutureBookings = employee(13L, "Enzo");
        EmployeeEntity employeeWithFewerFutureBookings = employee(18L, "Funcionario Teste");

        when(employeeRepository.findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(storeId))
                .thenReturn(List.of(
                        employeeWithMoreFutureBookings,
                        employeeWithFewerFutureBookings
                ));

        Appointments firstEmployeeFutureAppointment = appointment(
                100L,
                employeeWithMoreFutureBookings,
                requestedTime.plusDays(2)
        );

        Appointments secondEmployeeFutureAppointment = appointment(
                101L,
                employeeWithMoreFutureBookings,
                requestedTime.plusDays(3)
        );

        Appointments secondEmployeeSameDayAppointment = appointment(
                102L,
                employeeWithFewerFutureBookings,
                requestedTime.withHour(14)
        );

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        List.of(13L, 18L),
                        requestedTime.toLocalDate().atStartOfDay(),
                        requestedTime.toLocalDate().atTime(LocalTime.MAX),
                        StatusAppointments.CANCELED
                ))
                .thenReturn(List.of(secondEmployeeSameDayAppointment));

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentAfterAndStatusNot(
                        anyList(),
                        any(LocalDateTime.class),
                        eq(StatusAppointments.CANCELED)
                ))
                .thenReturn(List.of(
                        firstEmployeeFutureAppointment,
                        secondEmployeeFutureAppointment,
                        secondEmployeeSameDayAppointment
                ));

        AvailableEmployeeResponse result = service.selecionarFuncionario(
                storeId,
                0L,
                requestedTime,
                90
        );

        assertEquals(18L, result.employeeId());
        assertEquals("Funcionario Teste", result.employeeName());
    }

    @Test
    void assignmentRespectsTheRequestedEmployee() {
        StoreBusinessHoursRepository hoursRepository = mock(StoreBusinessHoursRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);

        AvailabilityService service = new AvailabilityService(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository
        );

        long storeId = 1L;
        long employeeId = 13L;
        LocalDateTime requestedTime = YearMonth.now().plusMonths(2).atDay(1).atTime(9, 0);

        StoreEntity store = mock(StoreEntity.class);
        when(store.isAtiva()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreBusinessHoursEntity weeklyHours = mock(StoreBusinessHoursEntity.class);
        when(weeklyHours.getDayOfWeek()).thenReturn(requestedTime.getDayOfWeek().getValue());
        when(weeklyHours.isClosed()).thenReturn(false);
        when(weeklyHours.getOpeningTime()).thenReturn(LocalTime.of(9, 0));
        when(weeklyHours.getClosingTime()).thenReturn(LocalTime.of(12, 0));
        when(hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(storeId))
                .thenReturn(List.of(weeklyHours));

        EmployeeEntity requestedEmployee = employee(employeeId, "Enzo");
        when(requestedEmployee.getUser().isActive()).thenReturn(true);
        when(requestedEmployee.isTeamActive()).thenReturn(true);
        when(employeeRepository.findByIdAndStore_Id(employeeId, storeId))
                .thenReturn(Optional.of(requestedEmployee));

        Appointments futureAppointment = appointment(
                100L,
                requestedEmployee,
                requestedTime.plusDays(2)
        );

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        List.of(employeeId),
                        requestedTime.toLocalDate().atStartOfDay(),
                        requestedTime.toLocalDate().atTime(LocalTime.MAX),
                        StatusAppointments.CANCELED
                ))
                .thenReturn(List.of());

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentAfterAndStatusNot(
                        anyList(),
                        any(LocalDateTime.class),
                        eq(StatusAppointments.CANCELED)
                ))
                .thenReturn(List.of(futureAppointment));

        AvailableEmployeeResponse result = service.selecionarFuncionario(
                storeId,
                employeeId,
                requestedTime,
                90
        );

        assertEquals(employeeId, result.employeeId());
        assertEquals("Enzo", result.employeeName());
    }

    @Test
    void calendarBlocksSlotsThatOverlapTheFullDurationOfAnExistingAppointment() {
        StoreBusinessHoursRepository hoursRepository = mock(StoreBusinessHoursRepository.class);
        StoreRepository storeRepository = mock(StoreRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AppointmentsRepository appointmentsRepository = mock(AppointmentsRepository.class);

        AppointmentsPetOfferingRepository petOfferingRepository =
                mock(AppointmentsPetOfferingRepository.class);

        AvailabilityService service = new AvailabilityService(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository,
                petOfferingRepository
        );

        long storeId = 1L;
        YearMonth yearMonth = YearMonth.now().plusMonths(2);

        StoreEntity store = mock(StoreEntity.class);
        when(store.isAtiva()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreBusinessHoursEntity weeklyHours = mock(StoreBusinessHoursEntity.class);
        when(weeklyHours.getDayOfWeek()).thenReturn(
                yearMonth.atDay(1).getDayOfWeek().getValue()
        );
        when(weeklyHours.isClosed()).thenReturn(false);
        when(weeklyHours.getOpeningTime()).thenReturn(LocalTime.of(9, 0));
        when(weeklyHours.getClosingTime()).thenReturn(LocalTime.of(11, 0));
        when(hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(storeId))
                .thenReturn(List.of(weeklyHours));

        EmployeeEntity employee = employee(10L, "Ana");
        when(employeeRepository
                .findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(storeId))
                .thenReturn(List.of(employee));

        Appointments appointment = appointment(
                100L,
                employee,
                yearMonth.atDay(1).atTime(9, 0)
        );

        when(appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        List.of(10L),
                        yearMonth.atDay(1).atStartOfDay(),
                        yearMonth.atEndOfMonth().atTime(LocalTime.MAX),
                        StatusAppointments.CANCELED
                ))
                .thenReturn(List.of(appointment));

        AppointmentsPetOffering item = mock(AppointmentsPetOffering.class);
        Pet pet = mock(Pet.class);
        Offering offering = mock(Offering.class);

        when(item.getAppointment()).thenReturn(appointment);
        when(item.getPet()).thenReturn(pet);
        when(pet.getId()).thenReturn(200L);
        when(item.getOffering()).thenReturn(offering);
        when(offering.getDuracaoMinutos()).thenReturn(60);

        when(petOfferingRepository.findByAppointments_IdIn(List.of(100L)))
                .thenReturn(List.of(item));

        List<AvailabilityCalendarDayResponse> result = service.consultarCalendario(
                storeId,
                0L,
                yearMonth,
                30
        );

        AvailabilityCalendarDayResponse firstDay = result.stream()
                .filter(day -> day.date().equals(yearMonth.atDay(1)))
                .findFirst()
                .orElseThrow();

        assertEquals(
                List.of(LocalTime.of(9, 0), LocalTime.of(9, 30)),
                firstDay.unavailableHours()
        );

        assertEquals(
                List.of(LocalTime.of(10, 0), LocalTime.of(10, 30)),
                firstDay.hours()
        );
    }

    private EmployeeEntity employee(Long id, String name) {
        EmployeeEntity employee = mock(EmployeeEntity.class);
        UserEntity user = mock(UserEntity.class);

        when(employee.getId()).thenReturn(id);
        when(employee.getUser()).thenReturn(user);
        when(employee.isTeamActive()).thenReturn(true);
        when(user.getName()).thenReturn(name);

        return employee;
    }

    private Appointments appointment(
            Long id,
            EmployeeEntity employee,
            LocalDateTime dateTime
    ) {
        Appointments appointment = mock(Appointments.class);

        when(appointment.getId()).thenReturn(id);
        when(appointment.getEmployee()).thenReturn(employee);
        when(appointment.getDateTimeAppointment()).thenReturn(dateTime);

        return appointment;
    }
}