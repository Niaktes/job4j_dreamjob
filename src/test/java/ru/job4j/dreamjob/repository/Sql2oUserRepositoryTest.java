package ru.job4j.dreamjob.repository;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import javax.sql.DataSource;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;
    private static User user;

    @BeforeAll
    static void initRepository() throws Exception {
         Properties properties = new Properties();
         try (InputStream inputStream = Sql2oCandidateRepositoryTest.class.getClassLoader()
                 .getResourceAsStream("connection.properties")) {
             properties.load(inputStream);
         }
         String url = properties.getProperty("datasource.url");
         String username = properties.getProperty("datasource.username");
         String password = properties.getProperty("datasource.password");

         DatasourceConfiguration configuration = new DatasourceConfiguration();
         DataSource dataSource = configuration.connectionPool(url, username, password);
         Sql2o sql2o = configuration.databaseClient(dataSource);

         sql2oUserRepository = new Sql2oUserRepository(sql2o);
         user = new User(0, "email", "name", "password");
    }

    @AfterEach
    public void deleteUser() {
        sql2oUserRepository.deleteByEmailAndPassword(user.getEmail(), user.getPassword());
    }

    @Test
    void whenSaveAndFindThenGetUser() {
        User savedUser = sql2oUserRepository.save(user).get();
        User findedUser = sql2oUserRepository.findByEmailAndPassword(user.getEmail(), user.getPassword()).get();
        assertThat(savedUser).usingRecursiveComparison().isEqualTo(user);
        assertThat(findedUser).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    void whenSaveAlreadyExistUserThenGetOptionalEmpty() {
        sql2oUserRepository.save(user);
        User anotherUser = new User(3, "email", "anotherName", "anotherPassword");
        Optional<User> anotherSavedUser = sql2oUserRepository.save(anotherUser);
        assertThat(anotherSavedUser).isEmpty();
    }

    @Test
    void whenTryToFindUserByWrongEmailOrPasswordThenGetOptionalEmpty() {
        sql2oUserRepository.save(user);
        Optional<User> wrongEmailUser = sql2oUserRepository.findByEmailAndPassword(
                "wrongEmail", "password"
        );
        Optional<User> wrongPasswordUser = sql2oUserRepository.findByEmailAndPassword(
                "email", "wrongPassword"
        );
        assertThat(wrongEmailUser).isEmpty();
        assertThat(wrongPasswordUser).isEmpty();
    }

}