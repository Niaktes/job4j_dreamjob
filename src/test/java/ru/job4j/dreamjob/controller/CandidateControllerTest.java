package ru.job4j.dreamjob.controller;

import java.util.List;
import java.util.Optional;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

class CandidateControllerTest {

    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    public void initService() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    void whenRequestCandidateListPageThenGetPageWithCandidates() {
        Candidate candidate1 = new Candidate(1, "test1", "desc1", now(), 1, 2);
        Candidate candidate2 = new Candidate(2, "test2", "desc2", now(), 3, 4);
        List<Candidate> expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        Model model = new ConcurrentModel();
        String view = candidateController.getAll(model);
        var actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    void whenRequestCandidateCreationPageThenGetPageWithCities() {
        City city1 = new City(1, "Москва");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        Model model = new ConcurrentModel();
        String view = candidateController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    void whenRequestCandidateIdPageThenGetPageWithCanddidateAndCities() {
        Candidate candidate = new Candidate(1, "test", "desc", now(), 1, 2);
        City city1 = new City(1, "Москва");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);
        when(candidateService.findById(any(Integer.class))).thenReturn(Optional.of(candidate));
        when(cityService.findAll()).thenReturn(expectedCities);

        Model model = new ConcurrentModel();
        String view = candidateController.getById(model, any(Integer.class));
        var actualCities = model.getAttribute("cities");
        var actualCandidate = model.getAttribute("candidate");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualCities).isEqualTo(expectedCities);
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
    }

    @Test
    void whenRequestCandidateWrongIdThenGetErrorPageWithMessage() {
        String expectedErrorMessage = "Кандидат с указанным id не найден.";
        when(candidateService.findById(any(Integer.class))).thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String view = candidateController.getById(model, any(Integer.class));
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    void whenRequestCandidateDeleteIdPageThenRedirectToCandidatesPage() {
        when(candidateService.deleteById(any(Integer.class))).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = candidateController.delete(model, any(Integer.class));

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    void whenRequestCandidateDeleteWrongIdThenGetErrorPageWithMessage() {
        String expectedErrorMessage = "Кандидат с указанным id не найден.";
        when(candidateService.deleteById(any(Integer.class))).thenReturn(false);

        Model model = new ConcurrentModel();
        String view = candidateController.delete(model, any(Integer.class));
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        Candidate candidate = new Candidate(1, "test1", "desc1", now(), 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture()))
                .thenReturn(candidate);

        Model model = new ConcurrentModel();
        String view = candidateController.create(candidate, testFile, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    void whenPostCandidateAndSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = candidateController.create(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    void whenUpdateCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        Candidate candidate = new Candidate(1, "test1", "desc1", now(), 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture()))
                .thenReturn(true);

        Model model = new ConcurrentModel();
        String view = candidateController.updateCandidate(candidate, testFile, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    void whenRequestCandidateUpdateWrongIdThenGetErrorPageWithMessage() {
        String expectedErrorMessage = "Кандидат с указанным id не найден.";
        when(candidateService.update(any(), any())).thenReturn(false);

        Model model = new ConcurrentModel();
        String view = candidateController.updateCandidate(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    void whenCandidateUpdateAndSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.update(any(), any())).thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = candidateController.updateCandidate(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

}