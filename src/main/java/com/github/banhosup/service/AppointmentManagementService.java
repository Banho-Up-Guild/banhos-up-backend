package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.appointments.response.AppointmentItemResponse;
import com.github.banhosup.dto.appointments.response.AppointmentManagementResponse;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.EmployeeRepository;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentManagementService {

    private final AppointmentsRepository appointmentsRepository;
    private final AppointmentsPetOfferingRepository itemsRepository;
    private final EmployeeRepository employeeRepository;


    // FUNCIONÁRIO: lista somente agendamentos da própria unidade.
    @Transactional(readOnly = true)
    public List<AppointmentManagementResponse> listarFuncionario(
            Authentication authentication,
            LocalDate startDate,
            LocalDate endDate,
            StatusAppointments status
    ) {

        EmployeeEntity employee = buscarFuncionarioAutenticado(authentication);

        Long storeId = employee.getStore().getId();

        List<Appointments> appointments =
                appointmentsRepository
                        .findByEmployee_Store_IdOrderByDateTimeAppointmentAsc(
                                storeId
                        );

        return appointments.stream()
                .filter(appointment ->
                        correspondeFiltros(
                                appointment,
                                startDate,
                                endDate,
                                status
                        )
                )
                .map(this::toResponse)
                .toList();
    }


    // ADMIN: lista agendamentos globalmente.
    @Transactional(readOnly = true)
    public List<AppointmentManagementResponse> listarAdmin(
            Long storeId,
            LocalDate startDate,
            LocalDate endDate,
            StatusAppointments status
    ) {

        validarPeriodo(startDate, endDate);

        return appointmentsRepository
                .findAll(Sort.by(Sort.Direction.ASC, "dateTimeAppointment"))
                .stream()
                .filter(appointment -> {

                    if (storeId == null) {
                        return true;
                    }

                    return appointment.getEmployee().getStore() != null
                            && appointment.getEmployee()
                            .getStore().getId().equals(storeId);
                })
                .filter(appointment ->
                        correspondeFiltros(
                                appointment,
                                startDate,
                                endDate,
                                status
                        )
                )
                .map(this::toResponse)
                .toList();
    }


    // FUNCIONÁRIO: confirma ou recusa um pedido da própria unidade.
    @Transactional
    public AppointmentManagementResponse atualizarStatusFuncionario(
            Long appointmentId,
            StatusAppointments novoStatus,
            Authentication authentication
    ) {

        EmployeeEntity employee = buscarFuncionarioAutenticado(authentication);

        Appointments appointment = appointmentsRepository
                .findByIdAndEmployee_Store_Id(
                        appointmentId,
                        employee.getStore().getId()
                )
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Agendamento não encontrado na sua unidade."
                ));

        validarTransicao(appointment, novoStatus);

        appointment.setStatus(novoStatus);

        return toResponse(appointmentsRepository.save(appointment));
    }


    // ADMIN: atualiza agendamento de qualquer unidade.
    @Transactional
    public AppointmentManagementResponse atualizarStatusAdmin(
            Long appointmentId,
            StatusAppointments novoStatus
    ) {

        Appointments appointment = appointmentsRepository
                .findById(appointmentId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Agendamento não encontrado."
                ));

        validarTransicao(appointment, novoStatus);

        appointment.setStatus(novoStatus);

        return toResponse(appointmentsRepository.save(appointment));
    }


    private EmployeeEntity buscarFuncionarioAutenticado(
            Authentication authentication
    ) {

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));

        if (!employee.getUser().isActive()) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Usuário inativo."
            );
        }

        if (employee.getStore() == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Funcionário sem unidade vinculada."
            );
        }

        return employee;
    }


    private boolean correspondeFiltros(
            Appointments appointment,
            LocalDate startDate,
            LocalDate endDate,
            StatusAppointments status
    ) {

        validarPeriodo(startDate, endDate);

        if (startDate != null &&
                appointment.getDateTimeAppointment()
                        .isBefore(startDate.atStartOfDay())) {
            return false;
        }

        if (endDate != null &&
                appointment.getDateTimeAppointment()
                        .isAfter(endDate.atTime(LocalTime.MAX))) {
            return false;
        }

        return status == null || appointment.getStatus() == status;
    }


    private void validarPeriodo(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate != null &&
                endDate != null &&
                startDate.isAfter(endDate)) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A data inicial não pode ser posterior à data final."
            );
        }
    }


    private void validarTransicao(
            Appointments appointment,
            StatusAppointments novoStatus
    ) {

        if (novoStatus != StatusAppointments.CONFIRMED &&
                novoStatus != StatusAppointments.CANCELED) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Status não permitido para esta operação."
            );
        }

        if (appointment.getStatus() !=
                StatusAppointments.AWAITING_CONFIRMATION) {

            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Este agendamento já foi processado."
            );
        }
    }


    private AppointmentManagementResponse toResponse(
            Appointments appointment
    ) {

        List<AppointmentsPetOffering> associations =
                itemsRepository.findByAppointments_Id(appointment.getId());

        List<AppointmentItemResponse> items = associations.stream()
                .map(item -> new AppointmentItemResponse(
                        item.getPet().getId(),
                        item.getPet().getNome(),
                        item.getPet().getEspecie(),
                        item.getOffering().getId(),
                        item.getOffering().getNome(),
                        item.getOffering().getPreco()
                ))
                .toList();

        String customerName = associations.stream()
                .map(item -> item.getPet().getDono().getUser().getName())
                .findFirst()
                .orElse("Cliente não identificado");

        String customerEmail = associations.stream()
                .map(item -> item.getPet().getDono().getUser().getEmail())
                .findFirst()
                .orElse(null);

        return new AppointmentManagementResponse(
                appointment.getId(),
                appointment.getDateTimeAppointment(),
                appointment.getStatus(),
                appointment.getObservation(),
                appointment.getTotalPrice(),
                appointment.getEmployee().getId(),
                appointment.getEmployee().getUser().getName(),
                customerName,
                customerEmail,
                items
        );
    }
}