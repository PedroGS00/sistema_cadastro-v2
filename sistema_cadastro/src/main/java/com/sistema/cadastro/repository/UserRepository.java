package com.sistema.cadastro.repository;

import com.sistema.cadastro.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    Optional<User> findByCpf(String cpf);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM users u WHERE u.cpf = :cpf AND u.id <> :id", nativeQuery = true)
    Optional<User> findByCpfAndIdNot(@Param("cpf") String cpf, @Param("id") Long id);

    @Query(value = "SELECT * FROM users u WHERE u.email = :email AND u.id <> :id", nativeQuery = true)
    Optional<User> findByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Query(value = "SELECT * FROM users u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:cpf IS NULL OR u.cpf = :cpf) AND " +
           "(:city IS NULL OR LOWER(u.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR u.state = :state)",
           countQuery = "SELECT COUNT(*) FROM users u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:cpf IS NULL OR u.cpf = :cpf) AND " +
           "(:city IS NULL OR LOWER(u.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR u.state = :state)",
           nativeQuery = true)
    Page<User> findByFilters(@Param("name") String name,
                            @Param("email") String email,
                            @Param("cpf") String cpf,
                            @Param("city") String city,
                            @Param("state") String state,
                            Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.created_at >= CURRENT_DATE()", nativeQuery = true)
    long countUsersCreatedToday();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.created_at >= NOW() - INTERVAL '7 days'", nativeQuery = true)
    long countUsersCreatedLastWeek();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.created_at >= NOW() - INTERVAL '30 days'", nativeQuery = true)
    long countUsersCreatedLastMonth();
}
