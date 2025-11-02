package com.mybillbook.repository;

import com.mybillbook.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUser() {
        // Given
        User user = new User();
        user.setMobileNumber("9876543210");
        user.setName("Ramesh Kumar");
        user.setBusinessName("Ramesh Traders");

        // When
        User savedUser = userRepository.save(user);
        User foundUser = entityManager.find(User.class, savedUser.getId());

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getMobileNumber()).isEqualTo("9876543210");
        assertThat(foundUser.getName()).isEqualTo("Ramesh Kumar");
        assertThat(foundUser.getBusinessName()).isEqualTo("Ramesh Traders");
        assertThat(foundUser.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByMobileNumber() {
        // Given
        User user = new User();
        user.setMobileNumber("9876543210");
        user.setName("Suresh Patel");
        user.setBusinessName("Suresh Medical");
        entityManager.persistAndFlush(user);

        // When
        Optional<User> foundUser = userRepository.findByMobileNumber("9876543210");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Suresh Patel");
    }

    @Test
    void shouldReturnEmptyWhenMobileNumberNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByMobileNumber("9999999999");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldNotAllowDuplicateMobileNumbers() {
        // Given
        User user1 = new User();
        user1.setMobileNumber("9876543210");
        user1.setName("User One");
        entityManager.persistAndFlush(user1);

        User user2 = new User();
        user2.setMobileNumber("9876543210");
        user2.setName("User Two");

        // When & Then
        try {
            userRepository.save(user2);
            entityManager.flush();
            assertThat(false).as("Should have thrown constraint violation").isTrue();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
