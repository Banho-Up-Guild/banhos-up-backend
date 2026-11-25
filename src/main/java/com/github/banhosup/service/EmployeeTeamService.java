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
                .map(this::toResponse)
                .toList();
    }

    // Lista funcionários que ainda não possuem unidade.
    @Transactional(readOnly = true)
    public List<EmployeeTeamResponse> listarFuncionariosDisponiveis(
            Authentication authentication
    ) {

        buscarFuncionarioAutenticado(authentication);

        return employeeRepository
                .findByStoreIsNullAndUser_ActiveTrueOrderByUser_NameAsc()
                .stream()
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

        if (!funcionario.getUser().isActive()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O funcionário está inativo."
            );
        }

        if (funcionario.getStore() != null) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "O funcionário já está vinculado a uma unidade."
            );
        }

        funcionario.setStore(responsavel.getStore());

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

        funcionario.setStore(null);

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

        return employeeRepository
                .findByIdAndStore_Id(employeeId, storeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado na sua unidade."
                ));
    }

    private EmployeeTeamResponse toResponse(EmployeeEntity employee) {

        return new EmployeeTeamResponse(
                employee.getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhone(),
                employee.getRegistry(),
                employee.getPosition(),
                employee.getStore() != null
                        ? employee.getStore().getId()
                        : null,
                employee.getStore() != null
                        ? employee.getStore().getNome()
                        : null,
                employee.getUser().isActive()
        );
    }
}