package com.sistema.cadastro.integration;

import com.sistema.cadastro.entity.Product;
import com.sistema.cadastro.entity.User;
import com.sistema.cadastro.repository.ProductRepository;
import com.sistema.cadastro.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @BeforeEach
    void setupDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {

                st.execute("DROP TABLE IF EXISTS users CASCADE");
                st.execute("DROP TABLE IF EXISTS products CASCADE");

                st.execute("""
                    CREATE TABLE users (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100) NOT NULL UNIQUE,
                        cpf CHAR(11) NOT NULL UNIQUE,
                        cep CHAR(8) NOT NULL,
                        address VARCHAR(200),
                        city VARCHAR(100),
                        state CHAR(2),
                        neighborhood VARCHAR(100),
                        complement VARCHAR(50),
                        number VARCHAR(10),
                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMP
                    )
                """);

                st.execute("""
                    CREATE TABLE products (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        description VARCHAR(500),
                        sku VARCHAR(50) NOT NULL UNIQUE,
                        price NUMERIC(12,2) NOT NULL,
                        stock INTEGER NOT NULL,
                        weight NUMERIC(11,3),
                        category VARCHAR(50),
                        brand VARCHAR(50),
                        image_url VARCHAR(255),
                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMP
                    )
                """);
            }
            conn.commit();
        } catch (SQLException e) {

            fail("Erro ao criar schema/tabelas para testes: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanupDatabase() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS users CASCADE");
            st.execute("DROP TABLE IF EXISTS products CASCADE");
        } catch (SQLException e) {

            System.err.println("Falha ao limpar banco de testes: " + e.getMessage());
        }
    }

    @Test
    void deveValidarSchemaDoBanco() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            assertTrue(tableExists(meta, "users"), "Tabela 'users' deve existir");
            assertTrue(tableExists(meta, "products"), "Tabela 'products' deve existir");

            assertTrue(columnExists(meta, "users", "email"), "Coluna 'email' deve existir em 'users'");
            assertTrue(columnExists(meta, "users", "cpf"), "Coluna 'cpf' deve existir em 'users'");
            assertTrue(columnExists(meta, "products", "sku"), "Coluna 'sku' deve existir em 'products'");

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_name = ? AND constraint_type = 'UNIQUE'")) {
                ps.setString(1, "users");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Consulta de constraints deve retornar resultado");
                    int uniqueCount = rs.getInt(1);
                    assertTrue(uniqueCount >= 2, "Esperado ao menos 2 UNIQUE em 'users' (email, cpf)");
                }
            }
        }
    }

    @Test
    void deveExecutarCrudCompletoDeUsuario() {

        User u = User.builder()
                .name("Fulano")
                .email("fulano@example.com")
                .cpf("12345678909")
                .cep("12345678")
                .city("SP")
                .state("SP")
                .build();

        User saved = userRepository.save(u);
        assertNotNull(saved.getId(), "ID deve ser gerado ao salvar usuÃ¡rio");

        Optional<User> byId = userRepository.findById(saved.getId());
        assertTrue(byId.isPresent(), "UsuÃ¡rio deve ser recuperado por ID");
        assertEquals("fulano@example.com", byId.get().getEmail(), "Email deve ser mantido");

        saved.setCity("SÃ£o Paulo");
        saved.setAddress("Rua A, 100");
        User updated = userRepository.save(saved);
        assertEquals("SÃ£o Paulo", updated.getCity(), "Cidade deve ser atualizada");

        userRepository.deleteById(updated.getId());
        assertFalse(userRepository.findById(updated.getId()).isPresent(), "UsuÃ¡rio deve ser removido");
    }

    @Test
    void deveExecutarCrudCompletoDeProduto() {

        Product p = Product.builder()
                .name("Mouse Gamer")
                .sku("SKU-001")
                .price(new BigDecimal("99.90"))
                .stock(50)
                .category("PerifÃ©ricos")
                .brand("Tech")
                .build();

        Product saved = productRepository.save(p);
        assertNotNull(saved.getId(), "ID deve ser gerado ao salvar produto");

        Optional<Product> bySku = productRepository.findBySku("SKU-001");
        assertTrue(bySku.isPresent(), "Produto deve ser recuperado por SKU");

        saved.increaseStock(10);
        productRepository.save(saved);
        Product reloaded = productRepository.findById(saved.getId()).orElseThrow();
        assertEquals(60, reloaded.getStock(), "Estoque deve ser atualizado corretamente");

        productRepository.delete(reloaded);
        assertFalse(productRepository.existsBySku("SKU-001"), "Produto deve ser removido");
    }

    @Test
    void deveFalharAoInserirUsuarioComEmailDuplicado() {
        User u1 = User.builder()
                .name("Primeiro")
                .email("duplicado@example.com")
                .cpf("11144477735")
                .cep("22222222")
                .build();
        userRepository.save(u1);

        User u2 = User.builder()
                .name("Segundo")
                .email("duplicado@example.com")
                .cpf("22233344455")
                .cep("33333333")
                .build();

        Exception ex = assertThrows(Exception.class, () -> userRepository.saveAndFlush(u2),
                "Deve lanÃ§ar exceÃ§Ã£o devido Ã  UNIQUE constraint do email");
        assertNotNull(ex, "ExceÃ§Ã£o nÃ£o deve ser nula");
    }

    private boolean tableExists(DatabaseMetaData meta, String table) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, table, null)) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (table.equalsIgnoreCase(name)) return true;
            }
            return false;
        }
    }

    private boolean columnExists(DatabaseMetaData meta, String table, String column) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                if (column.equalsIgnoreCase(name)) return true;
            }
            return false;
        }
    }
}
