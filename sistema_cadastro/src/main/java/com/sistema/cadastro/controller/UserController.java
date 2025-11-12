package com.sistema.cadastro.controller;

import com.sistema.cadastro.dto.UserDTO;
import com.sistema.cadastro.entity.User;
import com.sistema.cadastro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UsuÃ¡rios", description = "Endpoints para gerenciamento de usuÃ¡rios")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo usuÃ¡rio", description = "Cria um novo usuÃ¡rio com validaÃ§Ã£o de CPF, email e CEP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "UsuÃ¡rio criado com sucesso",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos"),
            @ApiResponse(responseCode = "409", description = "CPF ou email jÃ¡ cadastrado"),
            @ApiResponse(responseCode = "422", description = "CEP invÃ¡lido ou nÃ£o encontrado")
    })
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuÃ¡rio por ID", description = "Retorna os dados de um usuÃ¡rio especÃ­fico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UsuÃ¡rio encontrado",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID do usuÃ¡rio", required = true)
            @PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Listar usuÃ¡rios", description = "Retorna uma lista paginada de usuÃ¡rios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuÃ¡rios recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<User>> getAllUsers(
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar usuÃ¡rios com filtros", description = "Retorna uma lista paginada de usuÃ¡rios com base em filtros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuÃ¡rios recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<User>> getUsersByFilters(
            @Parameter(description = "Nome do usuÃ¡rio (parcial)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Email do usuÃ¡rio (parcial)")
            @RequestParam(required = false) String email,
            @Parameter(description = "CPF do usuÃ¡rio (exato)")
            @RequestParam(required = false) String cpf,
            @Parameter(description = "Cidade do usuÃ¡rio (parcial)")
            @RequestParam(required = false) String city,
            @Parameter(description = "Estado do usuÃ¡rio (UF)")
            @RequestParam(required = false) String state,
            @Parameter(description = "ParÃ¢metros de paginaÃ§Ã£o")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<User> users = userService.getUsersByFilters(name, email, cpf, city, state, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar usuÃ¡rio por CPF", description = "Retorna os dados de um usuÃ¡rio especÃ­fico pelo CPF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UsuÃ¡rio encontrado",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado")
    })
    public ResponseEntity<User> getUserByCpf(
            @Parameter(description = "CPF do usuÃ¡rio (apenas nÃºmeros)", required = true)
            @PathVariable String cpf) {
        User user = userService.getUserByCpf(cpf);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar usuÃ¡rio por email", description = "Retorna os dados de um usuÃ¡rio especÃ­fico pelo email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UsuÃ¡rio encontrado",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado")
    })
    public ResponseEntity<User> getUserByEmail(
            @Parameter(description = "Email do usuÃ¡rio", required = true)
            @PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuÃ¡rio", description = "Atualiza os dados de um usuÃ¡rio existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UsuÃ¡rio atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos"),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado"),
            @ApiResponse(responseCode = "409", description = "CPF ou email jÃ¡ cadastrado para outro usuÃ¡rio")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID do usuÃ¡rio", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar usuÃ¡rio", description = "Remove um usuÃ¡rio do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "UsuÃ¡rio deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID do usuÃ¡rio", required = true)
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/today")
    @Operation(summary = "EstatÃ­sticas - UsuÃ¡rios criados hoje", description = "Retorna a quantidade de usuÃ¡rios criados no dia atual")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> countUsersCreatedToday() {
        long count = userService.countUsersCreatedToday();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/week")
    @Operation(summary = "EstatÃ­sticas - UsuÃ¡rios criados na semana", description = "Retorna a quantidade de usuÃ¡rios criados nos Ãºltimos 7 dias")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> countUsersCreatedLastWeek() {
        long count = userService.countUsersCreatedLastWeek();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/month")
    @Operation(summary = "EstatÃ­sticas - UsuÃ¡rios criados no mÃªs", description = "Retorna a quantidade de usuÃ¡rios criados nos Ãºltimos 30 dias")
    @ApiResponse(responseCode = "200", description = "EstatÃ­stica recuperada com sucesso")
    public ResponseEntity<Long> countUsersCreatedLastMonth() {
        long count = userService.countUsersCreatedLastMonth();
        return ResponseEntity.ok(count);
    }
}
