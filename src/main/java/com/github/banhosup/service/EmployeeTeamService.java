package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.EmployeeTeamPositionRequest;
import com.github.banhosup.dto.EmployeeTeamResponse;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeTeamService {

    private final EmployeeRepository employeeRepository;

    // Lista a equipe da unidade do funcionário autenticado.
    @Transactional(readOnly = true)
    public List<EmployeeTeamResponse> listarMinhaEquipe(
            Authentication authentication
    ) {

        EmployeeEntity responsavel =
                buscarFuncionarioAutenticado(authentication);

        Long storeId = responsavel.getStore().getId();

        return employeeRepository
                .findByStore_IdAndUser_ActiveTrueOrderByUser_NameAsc(storeId)
                .stream()
                .filter(EmployeeEntity::isTeamActive)
                .filter(employee ->
                        employee.getUser().getRole() ==
                                com.github.banhosup.model.enums.Role.EMPLOYEE
                )
                .map(EmployeeTeamResponse::from)
                .toList();
    }

    // Lista funcionários que ainda não possuem unidade.
    @Transactional(readOnly = true)
    public List<EmployeeTeamResponse> listarFuncionariosDisponiveis(
            Authentication authentication
    ) {

        EmployeeEntity responsavel =
                buscarFuncionarioAutenticado(authentication);

        Long storeId = responsavel.getStore().getId();

        List<EmployeeEntity> disponiveis = new java.util.ArrayList<>();

// Funcionários que ainda não possuem unidade.
        disponiveis.addAll(
                employeeRepository
                        .findByStoreIsNullAndUser_ActiveTrueOrderByUser_NameAsc()
        );

// Funcionários anteriormente desvinculados desta mesma unidade.
        employeeRepository.findByStore_Id(storeId)
                .stream()
                .filter(employee -> !employee.isTeamActive())
                .filter(employee -> employee.getUser().isActive())
                .forEach(disponiveis::add);

        return disponiveis.stream()
                .filter(employee ->
                        employee.getUser().getRole() ==
                                com.github.banhosup.model.enums.Role.EMPLOYEE
                )
                .map(this::toResponse)
                .toList();
    }

    // Vincula um funcionário já cadastrado à unidade atual.
    @Transactional
    public EmployeeTeamResponse vincularFuncionario(
            Long employeeId,
            Authentication authentication
    ) {

        EmployeeEntity responsavel =
                buscarFuncionarioAutenticado(authentication);

        EmployeeEntity funcionario = buscarFuncionario(employeeId);

        if (funcionario.getId().equals(responsavel.getId())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Você não pode alterar seu próprio vínculo."
            );
        }

        if (funcionario.getUser().getRole() !=
                com.github.banhosup.model.enums.Role.EMPLOYEE) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O usuário informado não é funcionário."
            );
        }

        if (!funcionario.getUser().isActive()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O funcionário está inativo."
            );
        }

        Long storeId = responsavel.getStore().getId();

        if (funcionario.getStore() != null &&
                !funcionario.getStore().getId().equals(storeId)) {

            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "O funcionário já pertence a outra unidade."
            );
        }

        if (funcionario.getStore() != null &&
                funcionario.isTeamActive()) {

            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "O funcionário já está na equipe."
            );
        }

// Vincula ou reativa o vínculo da mesma unidade.
        funcionario.setStore(responsavel.getStore());
        funcionario.setTeamActive(true);

        return toResponse(employeeRepository.save(funcionario));
    }

    // Altera o cargo de um membro da própria equipe.
    @Transactional
    public EmployeeTeamResponse atualizarCargo(
            Long employeeId,
            EmployeeTeamPositionRequest request,
            Authentication authentication
    ) {

        EmployeeEntity responsavel =
                buscarFuncionarioAutenticado(authentication);

        EmployeeEntity funcionario =
                buscarFuncionarioDaMinhaEquipe(
                        employeeId,
                        responsavel.getStore().getId()
                );

        if (funcionario.getId().equals(responsavel.getId())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Você não pode alterar seu próprio cargo por esta operação."
            );
        }

        funcionario.setPosition(request.position().trim());

        return toResponse(employeeRepository.save(funcionario));
    }

    // Remove somente o vínculo com a unidade.
    @Transactional
    public void desvincularFuncionario(
            Long employeeId,
            Authentication authentication
    ) {

        EmployeeEntity responsavel =
                buscarFuncionarioAutenticado(authentication);

        EmployeeEntity funcionario =
                buscarFuncionarioDaMinhaEquipe(
                        employeeId,
                        responsavel.getStore().getId()
                );

        if (funcionario.getId().equals(responsavel.getId())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Você não pode remover seu próprio vínculo."
            );
        }

// Desativa o vínculo, mas preserva a unidade histórica.
        funcionario.setTeamActive(false);

        employeeRepository.save(funcionario);
    }

    private EmployeeEntity buscarFuncionarioAutenticado(
            Authentication authentication
    ) {

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Registro de funcionário não encontrado."
                ));

        if (!employee.getUser().isActive() ||
                !employee.isTeamActive()) {

            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Funcionário inativo na equipe."
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

    private EmployeeEntity buscarFuncionario(Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));
    }

    private EmployeeEntity buscarFuncionarioDaMinhaEquipe(
            Long employeeId,
            Long storeId
    ) {

        EmployeeEntity employee = employeeRepository
                .findByIdAndStore_Id(employeeId, storeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado na sua unidade."
                ));

        if (!employee.isTeamActive() ||
                !employee.getUser().isActive() ||
                employee.getUser().getRole() !=
                        com.github.banhosup.model.enums.Role.EMPLOYEE) {

            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Funcionário não está ativo na equipe."
            );
        }

        return employee;
    }

    private EmployeeTeamResponse toResponse(EmployeeEntity employee) {
            return EmployeeTeamResponse.from(employee);
        }
    }