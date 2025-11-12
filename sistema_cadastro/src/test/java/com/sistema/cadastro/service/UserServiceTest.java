package com.sistema.cadastro.service;

import com.sistema.cadastro.dto.CepDataDTO;
import com.sistema.cadastro.dto.UserDTO;
import com.sistema.cadastro.entity.User;
import com.sistema.cadastro.exception.BusinessException;
import com.sistema.cadastro.exception.ResourceNotFoundException;
import com.sistema.cadastro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CepService cepService;

    @InjectMocks
    private UserService userService;

    private UserDTO validDto;

    @BeforeEach
    void setup() {
        validDto = UserDTO.builder()
                .name("JoÃ£o da Silva")
                .email("Joao.SILVA@Email.com ")
                .cpf("12345678909")
                .cep("12345-678")
                .number("123")
                .build();
    }

    private User buildUserFromDto(UserDTO dto) {
        User u = new User();
        u.setId(1L);
        u.setName(dto.getName());
        u.setEmail(dto.getEmail().toLowerCase().trim());
        u.setCpf(dto.getCpf().replaceAll("\\D", ""));
        u.setCep(dto.getCep().replaceAll("\\D", ""));
        u.setAddress("Rua A");
        u.setCity("SÃ£o Paulo");
        u.setState("SP");
        u.setNeighborhood("Centro");
        u.setComplement("Apto 10");
        u.setNumber(dto.getNumber());
        return u;
    }

    private CepDataDTO buildCepData() {
        CepDataDTO dto = new CepDataDTO();
        dto.setCep("12345678");
        dto.setLogradouro("Rua A");
        dto.setBairro("Centro");
        dto.setLocalidade("SÃ£o Paulo");
        dto.setUf("SP");
        dto.setComplemento("Apto 10");
        return dto;
    }

    @Test
    @DisplayName("createUser: sucesso com normalizaÃ§Ã£o de email/CPF/CEP e preenchimento de endereÃ§o via CEP")
    void createUser_success() {
        when(userRepository.existsByCpf("12345678909")).thenReturn(false);
        when(userRepository.existsByEmail("joao.silva@email.com")).thenReturn(false);
        when(cepService.validateAndFetchCep("12345-678")).thenReturn(buildCepData());

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.createUser(validDto);

        assertNotNull(result.getId());
        assertEquals("joao.silva@email.com", result.getEmail());
        assertEquals("12345678909", result.getCpf());
        assertEquals("12345678", result.getCep());
        assertEquals("Rua A", result.getAddress());
        assertEquals("SÃ£o Paulo", result.getCity());
        assertEquals("SP", result.getState());
        assertEquals("Centro", result.getNeighborhood());
        assertEquals("Apto 10", result.getComplement());

        verify(userRepository).existsByCpf("12345678909");
        verify(userRepository).existsByEmail("joao.silva@email.com");
        verify(cepService).validateAndFetchCep("12345-678");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: CPF duplicado lanÃ§a BusinessException com cÃ³digo DUPLICATE_CPF")
    void createUser_duplicateCpf() {
        when(userRepository.existsByCpf("12345678909")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(validDto));
        assertEquals("DUPLICATE_CPF", ex.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(cepService, never()).validateAndFetchCep(anyString());
    }

    @Test
    @DisplayName("getUserById: encontrado retorna usuÃ¡rio")
    void getUserById_found() {
        User existing = buildUserFromDto(validDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        User result = userService.getUserById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("getUserById: nÃ£o encontrado lanÃ§a ResourceNotFoundException")
    void getUserById_notFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(2L));
    }

    @Test
    @DisplayName("getAllUsers: paginaÃ§Ã£o delegada ao repositÃ³rio")
    void getAllUsers_pageable() {
        Pageable pageable = PageRequest.of(0, 2);
        List<User> list = List.of(buildUserFromDto(validDto));
        Page<User> page = new PageImpl<>(list, pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(pageable);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getUsersByFilters: delega filtros ao repositÃ³rio e retorna pÃ¡gina")
    void getUsersByFilters_filters() {
        Pageable pageable = PageRequest.of(0, 1);
        List<User> list = List.of(buildUserFromDto(validDto));
        Page<User> page = new PageImpl<>(list, pageable, 1);
        when(userRepository.findByFilters("joao", "email", "12345678909", "SÃ£o Paulo", "SP", pageable)).thenReturn(page);

        Page<User> result = userService.getUsersByFilters("joao", "email", "12345678909", "SÃ£o Paulo", "SP", pageable);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByFilters("joao", "email", "12345678909", "SÃ£o Paulo", "SP", pageable);
    }

    @Test
    @DisplayName("getUserByCpf: normaliza CPF e retorna usuÃ¡rio")
    void getUserByCpf_success() {
        User existing = buildUserFromDto(validDto);
        when(userRepository.findByCpf("12345678909")).thenReturn(Optional.of(existing));
        User result = userService.getUserByCpf("123.456.789-09");
        assertEquals("12345678909", result.getCpf());
        verify(userRepository).findByCpf("12345678909");
    }

    @Test
    @DisplayName("getUserByCpf: nÃ£o encontrado lanÃ§a ResourceNotFoundException")
    void getUserByCpf_notFound() {
        when(userRepository.findByCpf("00000000000")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByCpf("000.000.000-00"));
    }

    @Test
    @DisplayName("getUserByEmail: nÃ£o encontrado lanÃ§a ResourceNotFoundException")
    void getUserByEmail_notFound() {
        when(userRepository.findByEmail("nao@existe.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("nao@existe.com"));
    }

    @Nested
    class UpdateUserTests {

        @Test
        @DisplayName("updateUser: sucesso com mudanÃ§a de CEP atualiza endereÃ§o via CepService")
        void updateUser_success_withCepChange() {
            User existing = buildUserFromDto(validDto);
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.findByCpfAndIdNot("12345678909", 1L)).thenReturn(Optional.empty());

            when(userRepository.findByEmailAndIdNot("novo.email@email.com", 1L)).thenReturn(Optional.empty());

            when(cepService.validateAndFetchCep("87654-321")).thenReturn(
                    CepDataDTO.builder()
                            .cep("87654321")
                            .logradouro("Rua B")
                            .complemento("Apto 20")
                            .bairro("Centro")
                            .localidade("SÃ£o Paulo")
                            .uf("SP")
                            .erro(false)
                            .build()
            );
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserDTO updateDto = UserDTO.builder()
                    .name("Novo Nome")
                    .email("Novo.Email@Email.com ")
                    .cpf("12345678909")
                    .cep("87654-321") 
                    .number("999")
                    .build();

            User updated = userService.updateUser(1L, updateDto);
            assertEquals("87654321", updated.getCep());
            assertEquals("Rua B", updated.getAddress());
            assertEquals("SÃ£o Paulo", updated.getCity());
            assertEquals("SP", updated.getState());
            assertEquals("Centro", updated.getNeighborhood());
            assertEquals("Apto 20", updated.getComplement());
            verify(cepService).validateAndFetchCep("87654-321");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("updateUser: email duplicado lanÃ§a BusinessException")
        void updateUser_duplicateEmail() {
            User existing = buildUserFromDto(validDto);
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.findByCpfAndIdNot("12345678909", 1L)).thenReturn(Optional.empty());

            when(userRepository.findByEmailAndIdNot("email@teste.com", 1L)).thenReturn(Optional.of(new User()));

            UserDTO updateDto = UserDTO.builder()
                    .name("Nome")
                    .email("email@teste.com")
                    .cpf("12345678909")
                    .cep("12345678")
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateUser(1L, updateDto));
            assertEquals("DUPLICATE_EMAIL", ex.getErrorCode());
            verify(cepService, never()).validateAndFetchCep(anyString());
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("deleteUser: sucesso delegando ao repositÃ³rio")
    void deleteUser_success() {
        User existing = buildUserFromDto(validDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(userRepository).delete(existing);

        userService.deleteUser(1L);
        verify(userRepository).delete(existing);
    }

    @Test
    @DisplayName("counters: createdToday/LastWeek/LastMonth delegam ao repositÃ³rio")
    void countersDelegation() {
        when(userRepository.countUsersCreatedToday()).thenReturn(5L);
        when(userRepository.countUsersCreatedLastWeek()).thenReturn(10L);
        when(userRepository.countUsersCreatedLastMonth()).thenReturn(20L);

        assertEquals(5L, userService.countUsersCreatedToday());
        assertEquals(10L, userService.countUsersCreatedLastWeek());
        assertEquals(20L, userService.countUsersCreatedLastMonth());
    }
}
