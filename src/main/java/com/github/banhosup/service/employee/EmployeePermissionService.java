package com.github.banhosup.service.employee;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.employee.EmployeePermissionsRequest;
import com.github.banhosup.dto.employee.EmployeePermissionsResponse;
import com.github.banhosup.model.entities.employee.EmployeeEntity;
import com.github.banhosup.model.entities.employee.EmployeePermissionEntity;
import com.github.banhosup.model.entities.user.UserEntity;
import com.github.banhosup.model.enums.EmployeePermission;
import com.github.banhosup.model.enums.Role;
import com.github.banhosup.repository.employee.EmployeePermissionRepository;
import com.github.banhosup.repository.employee.EmployeeRepository;
import com.github.banhosup.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeePermissionService {

    private final EmployeeRepository employeeRepository;

    private final EmployeePermissionRepository permissionRepository;

    private final UserRepository userRepository;


    // Verifica se o usuário possui determinada permissão.
    // ADMIN possui acesso global.
    @Transactional(readOnly = true)
    public boolean hasPermission(
            Authentication authentication,
            String permissionName
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();

        if (email == null || email.isBlank()) {
            return false;
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null || !user.isActive()) {
            return false;
        }

        // O administrador possui todas as permissões.
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Clientes não podem utilizar operações de funcionários.
        if (user.getRole() != Role.EMPLOYEE) {
            return false;
        }

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(email)
                .orElse(null);

        if (employee == null
                || !employee.getUser().isActive()
                || !employee.isTeamActive()
                || employee.getStore() == null) {
            return false;
        }

        EmployeePermission permission;

        try {
            permission = EmployeePermission.valueOf(permissionName);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        return permissionRepository
                .existsByEmployee_User_EmailAndPermission(
                        email,
                        permission
                );
    }


    // Substitui as permissões de um funcionário.
    @Transactional
    public EmployeePermissionsResponse atualizarPermissoes(
            Long employeeId,
            EmployeePermissionsRequest request
    ) {

        EmployeeEntity employee = buscarFuncionario(employeeId);

        if (employee.getUser().getRole() != Role.EMPLOYEE) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O usuário informado não é um funcionário."
            );
        }

        Set<EmployeePermission> permissoes = request.permissions();

        // Remove as permissões antigas.
        permissionRepository.deleteAllByEmployee_Id(employeeId);

        // Insere as novas permissões.
        List<EmployeePermissionEntity> novasPermissoes =
                permissoes.stream()
                        .map(permission ->
                                new EmployeePermissionEntity(
                                        employee,
                                        permission
                                )
                        )
                        .toList();

        permissionRepository.saveAll(novasPermissoes);

        return montarResposta(employee, permissoes.stream().toList());
    }


    // Consulta as permissões de determinado funcionário.
    @Transactional(readOnly = true)
    public EmployeePermissionsResponse buscarPermissoes(Long employeeId) {

        EmployeeEntity employee = buscarFuncionario(employeeId);

        List<EmployeePermission> permissoes =
                permissionRepository
                        .findAllByEmployee_IdOrderByPermissionAsc(employeeId)
                        .stream()
                        .map(EmployeePermissionEntity::getPermission)
                        .toList();

        return montarResposta(employee, permissoes);
    }


    // Consulta as permissões do funcionário autenticado.
    @Transactional(readOnly = true)
    public EmployeePermissionsResponse buscarPermissoesDoUsuario(
            Authentication authentication
    ) {

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Registro de funcionário não encontrado."
                ));

        if (!employee.getUser().isActive() ||
                !employee.isTeamActive() ||
                employee.getStore() == null) {

            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Funcionário sem vínculo ativo com uma unidade."
            );
        }

        return buscarPermissoes(employee.getId());
    }


    private EmployeeEntity buscarFuncionario(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));
    }


    private EmployeePermissionsResponse montarResposta(
            EmployeeEntity employee,
            List<EmployeePermission> permissoes
    ) {

        return new EmployeePermissionsResponse(
                employee.getId(),
                employee.getUser().getName(),
                permissoes
        );
    }

}