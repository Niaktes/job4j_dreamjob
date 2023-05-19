package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.File;

import javax.sql.DataSource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Sql2oCandidateRepositoryTest {

    private static Sql2oCandidateRepository sql2oCandidateRepository;
    private static Sql2oFileRepository sql2oFileRepository;
    private static File file;

    @BeforeAll
    static void initRepositories() throws Exception {
        Properties properties = new Properties();
        try (InputStream inputStream = Sql2oVacancyRepositoryTest.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        String url = properties.getProperty("datasource.url");
        String username = properties.getProperty("datasource.username");
        String password = properties.getProperty("datasource.password");

        DatasourceConfiguration configuration = new DatasourceConfiguration();
        DataSource dataSource = configuration.connectionPool(url, username, password);
        Sql2o sql2o = configuration.databaseClient(dataSource);

        sql2oCandidateRepository = new Sql2oCandidateRepository(sql2o);
        sql2oFileRepository = new Sql2oFileRepository(sql2o);
        file = new File("test", "test");
        sql2oFileRepository.save(file);
    }

    @AfterAll
    public static void deleteFile() {
        sql2oFileRepository.deleteById(file.getId());
    }

    @AfterEach
    public void clearCandidates() {
        Collection<Candidate> candidates = sql2oCandidateRepository.findAll();
        for (Candidate candidate : candidates) {
            sql2oCandidateRepository.deleteById(candidate.getId());
        }
    }

    @Test
    void whenSaveThenGetSame() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Candidate candidate = sql2oCandidateRepository.save(
                new Candidate(0, "name", "description", creationDate, 1, file.getId()));
        Candidate savedCandidate = sql2oCandidateRepository.findById(candidate.getId()).get();
        assertThat(savedCandidate).usingRecursiveComparison().isEqualTo(candidate);
    }

    @Test
    void whenSaveSeveralThenGetAll() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Candidate candidate1 = sql2oCandidateRepository.save(
                new Candidate(0, "name1", "description1", creationDate, 1, file.getId()));
        Candidate candidate2 = sql2oCandidateRepository.save(
                new Candidate(0, "nam2", "description2", creationDate, 1, file.getId()));
        Candidate candidate3 = sql2oCandidateRepository.save(
                new Candidate(0, "nam3", "description3", creationDate, 1, file.getId()));
        Collection<Candidate> result = sql2oCandidateRepository.findAll();
        assertThat(result).isEqualTo(List.of(candidate1, candidate2, candidate3));
    }

    @Test
    void whenDontSaveThenNothingFound() {
        assertThat(sql2oCandidateRepository.findAll()).isEqualTo(emptyList());
        assertThat(sql2oCandidateRepository.findById(1)).isEmpty();
    }

    @Test
    void whenDeleteThenGetEmptyOptional() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Candidate candidate = sql2oCandidateRepository.save(
                new Candidate(0, "name", "description", creationDate, 1, file.getId()));
        boolean isDeleted = sql2oCandidateRepository.deleteById(candidate.getId());
        Optional<Candidate> savedCandidate = sql2oCandidateRepository.findById(candidate.getId());
        assertThat(isDeleted).isTrue();
        assertThat(savedCandidate).isEmpty();
    }

    @Test
    void whenDeleteByInvalidIdThenGetFalse() {
        assertThat(sql2oCandidateRepository.deleteById(1)).isFalse();
    }

    @Test
    void whenUpdateThenGetUpdated() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Candidate candidate = sql2oCandidateRepository.save(
                new Candidate(0, "name", "description", creationDate, 1, file.getId()));
        Candidate updatedCandidate = new Candidate(
                candidate.getId(), "new name", "new description", creationDate.plusDays(1), 1, file.getId()
        );
        boolean isUpdated = sql2oCandidateRepository.update(updatedCandidate);
        Candidate savedCandidate = sql2oCandidateRepository.findById(updatedCandidate.getId()).get();
        assertThat(isUpdated).isTrue();
        assertThat(savedCandidate).usingRecursiveComparison().isEqualTo(updatedCandidate);
    }

    @Test
    void whenUpdateUnExistingCandidateThenGetFalse() {
        LocalDateTime creationDate = now().truncatedTo(ChronoUnit.MINUTES);
        Candidate candidate = new Candidate(0, "name", "description", creationDate, 1, file.getId());
        assertThat(sql2oCandidateRepository.update(candidate)).isFalse();
    }

}