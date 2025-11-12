package com.sistema.cadastro.service;

import com.sistema.cadastro.client.CepClient;
import com.sistema.cadastro.dto.CepDataDTO;
import com.sistema.cadastro.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CepServiceTest {

    @Mock
    private CepClient cepClient;

    @InjectMocks
    private CepService cepService;

    @Test
    @DisplayName("validateAndFetchCep: CEP com tamanho invÃ¡lido lanÃ§a BusinessException")
    void validateAndFetchCep_invalidLength() {
        assertThrows(BusinessException.class, () -> cepService.validateAndFetchCep("1234567")); 
        assertThrows(BusinessException.class, () -> cepService.validateAndFetchCep("123456789")); 
        assertThrows(BusinessException.class, () -> cepService.validateAndFetchCep("12-34-56")); 
    }

    @Test
    @DisplayName("validateAndFetchCep: CEP nÃ£o encontrado (erro=true) lanÃ§a BusinessException")
    void validateAndFetchCep_errorTrue() {
        when(cepClient.getCepData("12345678")).thenReturn(CepDataDTO.builder()
                .cep("12345678")
                .erro(true)
                .build());

        assertThrows(BusinessException.class, () -> cepService.validateAndFetchCep("12345-678"));
        verify(cepClient).getCepData("12345678");
    }

    @Test
    @DisplayName("validateAndFetchCep: falta de cidade (localidade) lanÃ§a BusinessException")
    void validateAndFetchCep_missingCity() {
        when(cepClient.getCepData("87654321")).thenReturn(CepDataDTO.builder()
                .cep("87654321")
                .logradouro("Rua X")
                .bairro("Bairro")
                .localidade("")
                .uf("SP")
                .erro(false)
                .build());

        assertThrows(BusinessException.class, () -> cepService.validateAndFetchCep("87654-321"));
        verify(cepClient).getCepData("87654321");
    }

    @Test
    @DisplayName("validateAndFetchCep: logradouro ausente nÃ£o impede sucesso (apenas warn)")
    void validateAndFetchCep_missingLogradouroAllowed() {
        when(cepClient.getCepData("22223333")).thenReturn(CepDataDTO.builder()
                .cep("22223333")
                .logradouro("")
                .bairro("Bairro")
                .localidade("Cidade")
                .uf("SP")
                .erro(false)
                .build());

        CepDataDTO result = cepService.validateAndFetchCep("22223-333");
        assertNotNull(result);
        assertEquals("22223333", result.getCep());
        assertEquals("SP", result.getUf());
        verify(cepClient).getCepData("22223333");
    }
}
