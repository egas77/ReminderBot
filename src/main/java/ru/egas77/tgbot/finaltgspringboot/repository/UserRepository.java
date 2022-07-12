package ru.egas77.tgbot.finaltgspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.egas77.tgbot.finaltgspringboot.models.Post;
import ru.egas77.tgbot.finaltgspringboot.models.User;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User getBytgid(long tgId);
    Optional<User> findUserById(long id);
}
