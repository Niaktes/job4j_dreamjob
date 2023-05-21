package ru.job4j.dreamjob.repository;

import java.util.Optional;
import ru.job4j.dreamjob.model.User;

public interface UserRepository {

    Optional<User> save(User user);

    Optional<User> findByEmailAndPassword(String email, String password);

    boolean deleteByEmailAndPassword(String email, String password);

}