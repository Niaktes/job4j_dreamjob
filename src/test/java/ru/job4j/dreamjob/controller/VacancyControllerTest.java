package ru.job4j.dreamjob.controller;

import java.util.List;
import java.util.Optional;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

class VacancyControllerTest {

    private VacancyService vacancyService;
    private CityService cityService;
    private VacancyController vacancyController;
    private MultipartFile testFile;

    @BeforeEach
    public void initService() {
        vacancyService = mock(VacancyService.class);
        cityService = mock(CityService.class);
        vacancyController = new VacancyController(vacancyService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    void whenRequestVacancyListPageThenGetPageWithVacancies() {
        Vacancy vacancy1 = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        Vacancy vacancy2 = new Vacancy(2, "test2", "desc2", now(), false, 3, 4);
        List<Vacancy> expectedVacancies = List.of(vacancy1, vacancy2);
        when(vacancyService.findAll()).thenReturn(expectedVacancies);

        Model model = new ConcurrentModel();
        String view = vacancyController.getAll(model);
        var actualVacancies = model.getAttribute("vacancies");

        assertThat(view).isEqualTo("vacancies/list");
        assertThat(actualVacancies).isEqualTo(expectedVacancies);
    }

    @Test
    void whenRequestVacancyCreationPageThenGetPageWithCities() {
        City city1 = new City(1, "Москва");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        Model model = new ConcurrentModel();
        String view = vacancyController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("vacancies/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    void whenRequestVacancyIdPageThenGetPageWithVacancyAndCities() {
        Vacancy vacancy = new Vacancy(1, "test", "desc", now(), true, 1, 2);
        City city1 = new City(1, "Москва");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);
        when(vacancyService.findById(any(Integer.class))).thenReturn(Optional.of(vacancy));
        when(cityService.findAll()).thenReturn(expectedCities);

        Model model = new ConcurrentModel();
        String view = vacancyController.getById(model, any(Integer.class));
        var actualCities = model.getAttribute("cities");
        var actualVacancy = model.getAttribute("vacancy");

        assertThat(view).isEqualTo("vacancies/one");
        assertThat(actualCities).isEqualTo(expectedCities);
        assertThat(actualVacancy).usingRecursiveComparison().isEqualTo(vacancy);
    }

    @Test
    void whenRequestVacancyWrongIdThenGetErrorPageWithMessage() {
        String expectedErrorMessage = "Вакансия с указанным идентификатором не найдена";
        when(vacancyService.findById(any(Integer.class))).thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String view = vacancyController.getById(model, any(Integer.class));
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    void whenRequestVacancyDeleteIdPageThenRedirectToVacanciesPage() {
        when(vacancyService.deleteById(any(Integer.class))).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = vacancyController.delete(model, any(Integer.class));

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    void whenRequestVacancyDeleteWrongIdThenGetErrorPageWithMessage() {
        String expectedErrorMessage = "Вакансия с указанным идентификатором не найдена";
        when(vacancyService.deleteById(any(Integer.class))).thenReturn(false);

        Model model = new ConcurrentModel();
        String view = vacancyController.delete(model, any(Integer.class));
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    void whenPostVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws Exception {
        Vacancy vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.save(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture()))
                .thenReturn(vacancy);

        Model model = new ConcurrentModel();
        String view = vacancyController.create(vacancy, testFile, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    void whenPostVacancyAndSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(vacancyService.save(any(), any())).thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = vacancyController.create(new Vacancy(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    void whenUpdateVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws Exception {
        Vacancy vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.update(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture()))
                .thenReturn(true);

        Model model = new ConcurrentModel();
        String view = vacancyController.updateVacancy(vacancy, testFile, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    void whenRequestVacancyUpdateWrongIdThenGetErrorPageWithMessage() {
        String expectedErrorMessage = "Вакансия с указанным идентификатором не найдена";
        when(vacancyService.update(any(), any())).thenReturn(false);

        Model model = new ConcurrentModel();
        String view = vacancyController.updateVacancy(new Vacancy(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    void whenUpdateVacancyAndSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(vacancyService.update(any(), any())).thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = vacancyController.updateVacancy(new Vacancy(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

}