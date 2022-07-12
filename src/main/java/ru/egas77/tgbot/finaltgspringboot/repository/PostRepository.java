package ru.egas77.tgbot.finaltgspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.egas77.tgbot.finaltgspringboot.models.Post;
import ru.egas77.tgbot.finaltgspringboot.models.User;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> getPostsByUserOrderByDatecreated(User user);
}
