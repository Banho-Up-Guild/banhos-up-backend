package com.github.banhosup.service.admin;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.employee.EmployeeTeamPositionRequest;
import com.github.banhosup.dto.employee.EmployeeTeamResponse;
import com.github.banhosup.dto.employee.EmployeeCreateRequest;
import com.github.banhosup.model.entities.employee.EmployeeEntity;
import com.github.banhosup.model.entities.store.StoreEntity;
import com.github.banhosup.model.entities.user.UserEntity;
import com.github.banhosup.model.enums.Role;
import com.github.banhosup.repository.employee.EmployeeRepository;
import com.github.banhosup.repository.store.StoreRepository;
import com.github.banhosup.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.appointment.AppointmentsRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTeamService {

    private final AppointmentsRepository appointmentsRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // ADMIN: criar uma conta de funcionário e vinculá-la diretamente à unidade.
    @Transactional
    public EmployeeTeamResponse criarFuncionario(
            Long storeId,
            EmployeeCreateRequest request
    ) {
        StoreEntity store = buscarUnidade(storeId);

        if (!store.isAtiva()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível cadastrar funcionários em uma unidade inativa."
            );
        }

        String email = request.email().trim().toLowerCase();
        String cpf = request.cpf().replaceAll("\\D", "");
        String phone = request.phone().replaceAll("\\D", "");
        String registry = request.registry().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "E-mail já cadastrado."
            );
        }

        if (userRepository.existsByCpf(cpf)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "CPF já cadastrado."
            );
        }

        if (employeeRepository.existsByRegistry(registry)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Matrícula já cadastrada."
            );
        }

        UserEntity user = new UserEntity(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                phone,
                cpf,
                Role.EMPLOYEE
        );

        user = userRepository.save(user);

        EmployeeEntity employee = new EmployeeEntity();
        employee.setUser(user);
        employee.setRegistry(registry);
        employee.setPosition(request.position().trim());
        employee.setStore(store);
        employee.setTeamActive(true);

        return EmployeeTeamResponse.from(
                employeeRepository.save(employee)
        );
    }


    // ADMIN: listar funcionários ativos de uma unidade.
    @Transactional(readOnly = true)
    public List<EmployeeTeamResponse> listarEquipe(Long storeId) {

        buscarUnidade(storeId);

        return employeeRepository.findByStore_Id(storeId)
                .stream()
                .filter(EmployeeEntity::isTeamActive)
                .filter(employee -> employee.getUser().isActive())
                .filter(employee -> employee.getUser().getRole() == Role.EMPLOYEE)
                .map(EmployeeTeamResponse::from)
                .toList();
    }


    // ADMIN: listar funcionários disponíveis para uma unidade.
    @Transactional(readOnly = true)
    public List<EmployeeTeamResponse> listarDisponiveis(Long storeId) {
        buscarUnidade(storeId);

        Map<Long, EmployeeEntity> disponiveis = new LinkedHashMap<>();

// Funcionários que nunca foram vinculados a uma unidade.
        employeeRepository
                .findByStoreIsNullAndUser_ActiveTrueOrderByUser_NameAsc()
                .forEach(employee -> disponiveis.putIfAbsent(
                        employee.getId(),
                        employee
                ));

// Funcionários desvinculados de qualquer unidade anterior.
        employeeRepository
                .findByTeamActiveFalseAndUser_ActiveTrueOrderByUser_NameAsc()
                .forEach(employee -> disponiveis.putIfAbsent(
                        employee.getId(),
                        employee
                ));

        return disponiveis.values()
                .stream()
                .filter(employee -> employee.getUser().isActive())
                .filter(employee -> employee.getUser().getRole() == Role.EMPLOYEE)
                .map(EmployeeTeamResponse::from)
                .toList();
    }


    // ADMIN: adicionar funcionário à equipe de uma unidade.
    @Transactional
    public EmployeeTeamResponse adicionar(
            Long storeId,
            Long employeeId
    ) {

        StoreEntity store = buscarUnidade(storeId);
        EmployeeEntity employee = buscarFuncionario(employeeId);

        if (!store.isAtiva()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível adicionar funcionários a uma unidade inativa."
            );
        }

        if (employee.getUser().getRole() != Role.EMPLOYEE) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O usuário informado não é funcionário."
            );
        }

        if (!employee.getUser().isActive()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O funcionário está inativo no sistema."
            );
        }

        if (employee.getStore() != null && employee.isTeamActive()) {

            String mensagem = employee.getStore().getId().equals(storeId)
                    ? "O funcionário já está ativo nesta equipe."
                    : "O funcionário ainda está vinculado a outra unidade. Desvincule-o primeiro.";

            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    mensagem
            );
        }

// Se veio de outra unidade, verifica compromissos futuros.
        if (employee.getStore() != null
                && !employee.getStore().getId().equals(storeId)) {

            boolean possuiCompromissosFuturos =
                    appointmentsRepository
                            .existsByEmployee_IdAndDateTimeAppointmentGreaterThanEqualAndStatusNot(
                                    employeeId,
                                    LocalDateTime.now(),
                                    StatusAppointments.CANCELED
                            );

            if (possuiCompromissosFuturos) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "Não é possível transferir o funcionário enquanto houver agendamentos futuros não cancelados."
                );
            }
        }

        employee.setStore(store);
        employee.setTeamActive(true);

        return EmployeeTeamResponse.from(
                employeeRepository.save(employee)
        );
    }


    // ADMIN: alterar cargo de um membro.
    @Transactional
    public EmployeeTeamResponse atualizarCargo(
            Long storeId,
            Long employeeId,
            EmployeeTeamPositionRequest request
    ) {

        EmployeeEntity employee =
                buscarMembroAtivo(storeId, employeeId);

        employee.setPosition(request.position().trim());

        return EmployeeTeamResponse.from(
                employeeRepository.save(employee)
        );
    }


    // ADMIN: remover funcionário da equipe.
    @Transactional
    public void remover(Long storeId, Long employeeId) {

        EmployeeEntity employee =
                buscarMembroAtivo(storeId, employeeId);

        // Preserva usuário, funcionário, unidade e agendamentos.
        employee.setTeamActive(false);

        employeeRepository.save(employee);
    }


    private EmployeeEntity buscarMembroAtivo(
            Long storeId,
            Long employeeId
    ) {

        EmployeeEntity employee = employeeRepository
                .findByIdAndStore_Id(employeeId, storeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado nesta unidade."
                ));

        if (!employee.isTeamActive() ||
                !employee.getUser().isActive() ||
                employee.getUser().getRole() != Role.EMPLOYEE) {

            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Funcionário não está ativo nesta equipe."
            );
        }

        return employee;
    }


    private EmployeeEntity buscarFuncionario(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));
    }


    private StoreEntity buscarUnidade(Long storeId) {

        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Unidade não encontrada."
                ));
    }
}