package com.sistema.cadastro.service;

import com.sistema.cadastro.client.CepClient;
import com.sistema.cadastro.dto.CepDataDTO;
import com.sistema.cadastro.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CepService {

    private final CepClient cepClient;
    private static final Logger log = LoggerFactory.getLogger(CepService.class);

    @Cacheable(value = "cepData", key = "#cep", unless = "#result == null")
    public CepDataDTO validateAndFetchCep(String cep) {
        log.info("Validando e buscando dados do CEP: {}", cep);
        
        if (cep == null || cep.trim().isEmpty()) {
            throw new BusinessException("CEP Ã© obrigatÃ³rio");
        }

        String cleanCep = cep.replaceAll("\\D", "");
        
        if (cleanCep.length() != 8) {
            throw new BusinessException("CEP deve ter 8 dÃ­gitos");
        }
        
        try {
            CepDataDTO cepData = cepClient.getCepData(cleanCep);
            
            if (cepData == null || cepData.getErro() != null && cepData.getErro()) {
                throw new BusinessException("CEP nÃ£o encontrado ou invÃ¡lido");
            }

            if (cepData.getLogradouro() == null || cepData.getLogradouro().trim().isEmpty()) {
                log.warn("CEP {} retornou sem logradouro", cleanCep);
            }
            
            if (cepData.getLocalidade() == null || cepData.getLocalidade().trim().isEmpty()) {
                throw new BusinessException("CEP retornou sem cidade");
            }
            
            if (cepData.getUf() == null || cepData.getUf().trim().isEmpty()) {
                throw new BusinessException("CEP retornou sem estado");
            }
            
            log.info("Dados do CEP {} recuperados com sucesso", cleanCep);
            return cepData;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar dados do CEP {}: {}", cleanCep, e.getMessage());
            throw new BusinessException("Erro ao buscar dados do CEP", e);
        }
    }

    public CepDataDTO getCachedCepData(String cep) {
        if (cep == null || cep.trim().isEmpty()) {
            return null;
        }
        
        String cleanCep = cep.replaceAll("\\D", "");
        
        try {
            return validateAndFetchCep(cleanCep);
        } catch (Exception e) {
            log.warn("Erro ao buscar CEP em cache: {}", e.getMessage());
            return null;
        }
    }

    public boolean isValidCep(String cep) {
        if (cep == null || cep.trim().isEmpty()) {
            return false;
        }
        
        String cleanCep = cep.replaceAll("\\D", "");
        
        if (cleanCep.length() != 8) {
            return false;
        }
        
        try {
            CepDataDTO cepData = validateAndFetchCep(cleanCep);
            return cepData != null && (cepData.getErro() == null || !cepData.getErro());
        } catch (Exception e) {
            return false;
        }
    }
}
