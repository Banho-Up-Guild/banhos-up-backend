package com.github.banhosup.service.dashboard;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.dashboard.DashboardResponse;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.entities.employee.EmployeeEntity;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.employee.EmployeeRepository;
import com.github.banhosup.repository.offering.OfferingRepository;
import com.github.banhosup.repository.store.StoreRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentsRepository appointmentsRepository;
    private final EmployeeRepository employeeRepository;
    private final OfferingRepository offeringRepository;
    private final StoreRepository storeRepository;


    // ADMIN: consulta indicadores globais ou de uma unidade.
    @Transactional(readOnly = true)
    public DashboardResponse resumoAdmin(Long storeId) {

        List<Appointments> appointments;

        if (storeId == null) {

            appointments = appointmentsRepository.findAll();

        } else {

            storeRepository.findById(storeId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND,
                            "Unidade não encontrada."
                    ));

            appointments = appointmentsRepository
                    .findByStore_IdOrderByDateTimeAppointmentAsc(
                    storeId
            );
        }

        long membrosEquipe = employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getStore() != null)
                .filter(employee ->
                        storeId == null
                                || employee.getStore().getId().equals(storeId)
                )
                .filter(employee ->
                        employee.isTeamActive()
                                && employee.getUser().isActive()
                )
                .count();

        return montarResumo(appointments, membrosEquipe);
    }


    // EMPLOYEE: consulta somente indicadores da própria unidade.
    @Transactional(readOnly = true)
    public DashboardResponse resumoFuncionario(
            Authentication authentication
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "É necessário estar autenticado."
            );
        }

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));

        if (!employee.getUser().isActive()
                || !employee.isTeamActive()
                || employee.getStore() == null) {

            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Funcionário sem vínculo ativo com uma unidade."
            );
        }

        Long storeId = employee.getStore().getId();

        List<Appointments> appointments =
                appointmentsRepository
                        .findByStore_IdOrderByDateTimeAppointmentAsc(storeId);

        long membrosEquipe = employeeRepository
                .findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(
                        storeId
                )
                .size();

        return montarResumo(appointments, membrosEquipe);
    }


    private DashboardResponse montarResumo(
            List<Appointments> appointments,
            long membrosEquipe
    ) {

        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        // Agendamentos de hoje, exceto os cancelados.
        long agendamentosHoje = appointments.stream()
                .filter(appointment ->
                        appointment.getDateTimeAppointment()
                                .toLocalDate()
                                .equals(hoje)
                )
                .filter(appointment ->
                        appointment.getStatus()
                                != StatusAppointments.CANCELED
                )
                .count();

        // Solicitações que ainda aguardam confirmação.
        long solicitacoesPendentes = appointments.stream()
                .filter(appointment ->
                        appointment.getStatus()
                                == StatusAppointments.AWAITING_CONFIRMATION
                )
                .count();

        // Serviços ativos no catálogo global.
        long servicosCadastrados =
                offeringRepository.findByAtivoTrue().size();

        return new DashboardResponse(
                agendamentosHoje,
                solicitacoesPendentes,
                servicosCadastrados,
                membrosEquipe
        );
    }
}