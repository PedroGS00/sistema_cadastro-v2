package com.sistema.cadastro.service;

import com.sistema.cadastro.dto.CepDataDTO;
import com.sistema.cadastro.dto.UserDTO;
import com.sistema.cadastro.entity.User;
import com.sistema.cadastro.exception.BusinessException;
import com.sistema.cadastro.exception.ResourceNotFoundException;
import com.sistema.cadastro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CepService cepService;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public User createUser(UserDTO userDTO) {
        log.info("Criando novo usuÃ¡rio: {}", userDTO.getEmail());

        validateUserCreation(userDTO);

        CepDataDTO cepData = cepService.validateAndFetchCep(userDTO.getCep());

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail().toLowerCase().trim());
        user.setCpf(userDTO.getCpf().replaceAll("\\D", ""));
        user.setCep(userDTO.getCep().replaceAll("\\D", ""));
        user.setAddress(cepData.getLogradouro());
        user.setCity(cepData.getLocalidade());
        user.setState(cepData.getUf());
        user.setNeighborhood(cepData.getBairro());
        user.setComplement(cepData.getComplemento());
        
        User savedUser = userRepository.save(user);
        log.info("UsuÃ¡rio criado com sucesso: ID {}", savedUser.getId());
        
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.info("Buscando usuÃ¡rio por ID: {}", id);
        
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UsuÃ¡rio", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        log.info("Listando usuÃ¡rios com paginaÃ§Ã£o: pÃ¡gina {}, tamanho {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersByFilters(String name, String email, String cpf, String city, String state, Pageable pageable) {
        log.info("Buscando usuÃ¡rios com filtros: nome={}, email={}, cpf={}, cidade={}, estado={}",
                name, email, cpf, city, state);
        
        return userRepository.findByFilters(name, email, cpf, city, state, pageable);
    }

    @Transactional(readOnly = true)
    public User getUserByCpf(String cpf) {
        log.info("Buscando usuÃ¡rio por CPF: {}", cpf);
        
        String cleanCpf = cpf.replaceAll("\\D", "");
        
        return userRepository.findByCpf(cleanCpf)
                .orElseThrow(() -> new ResourceNotFoundException("UsuÃ¡rio", "CPF", cpf));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.info("Buscando usuÃ¡rio por email: {}", email);
        
        String cleanEmail = email.toLowerCase().trim();
        
        return userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("UsuÃ¡rio", "email", email));
    }

    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        log.info("Atualizando usuÃ¡rio ID: {}", id);
        
        User existingUser = getUserById(id);

        validateUserUpdate(id, userDTO);

        if (!existingUser.getCep().equals(userDTO.getCep().replaceAll("\\D", ""))) {
            CepDataDTO cepData = cepService.validateAndFetchCep(userDTO.getCep());
            existingUser.setCep(userDTO.getCep().replaceAll("\\D", ""));
            existingUser.setAddress(cepData.getLogradouro());
            existingUser.setCity(cepData.getLocalidade());
            existingUser.setState(cepData.getUf());
            existingUser.setNeighborhood(cepData.getBairro());
            existingUser.setComplement(cepData.getComplemento());
        }

        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail().toLowerCase().trim());
        existingUser.setNumber(userDTO.getNumber());
        
        User updatedUser = userRepository.save(existingUser);
        log.info("UsuÃ¡rio atualizado com sucesso: ID {}", updatedUser.getId());
        
        return updatedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deletando usuÃ¡rio ID: {}", id);
        
        User user = getUserById(id);
        userRepository.delete(user);
        
        log.info("UsuÃ¡rio deletado com sucesso: ID {}", id);
    }

    @Transactional(readOnly = true)
    public long countUsersCreatedToday() {
        return userRepository.countUsersCreatedToday();
    }

    @Transactional(readOnly = true)
    public long countUsersCreatedLastWeek() {
        return userRepository.countUsersCreatedLastWeek();
    }

    @Transactional(readOnly = true)
    public long countUsersCreatedLastMonth() {
        return userRepository.countUsersCreatedLastMonth();
    }

    private void validateUserCreation(UserDTO userDTO) {

        String cleanCpf = userDTO.getCpf().replaceAll("\\D", "");
        if (userRepository.existsByCpf(cleanCpf)) {
            throw new BusinessException("CPF jÃ¡ cadastrado", HttpStatus.CONFLICT, "DUPLICATE_CPF");
        }

        String cleanEmail = userDTO.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BusinessException("Email jÃ¡ cadastrado", HttpStatus.CONFLICT, "DUPLICATE_EMAIL");
        }
    }

    private void validateUserUpdate(Long id, UserDTO userDTO) {

        String cleanCpf = userDTO.getCpf().replaceAll("\\D", "");
        if (userRepository.findByCpfAndIdNot(cleanCpf, id).isPresent()) {
            throw new BusinessException("CPF jÃ¡ cadastrado para outro usuÃ¡rio", HttpStatus.CONFLICT, "DUPLICATE_CPF");
        }

        String cleanEmail = userDTO.getEmail().toLowerCase().trim();
        if (userRepository.findByEmailAndIdNot(cleanEmail, id).isPresent()) {
            throw new BusinessException("Email jÃ¡ cadastrado para outro usuÃ¡rio", HttpStatus.CONFLICT, "DUPLICATE_EMAIL");
        }
    }
}
