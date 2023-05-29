package ru.job4j.dreamjob.controller;

import java.io.IOException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

class FileControllerTest {

    private FileService fileService;
    private FileController fileController;
    private MultipartFile testFile;

    @BeforeEach
    public void initService() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    void whenRequestFileByIdThenGetResponseEntity() throws IOException {
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        when(fileService.getFileById(any(Integer.class))).thenReturn(Optional.of(fileDto));

        ResponseEntity<?> actualContent = fileController.getById(any(Integer.class));

        assertThat(actualContent).isEqualTo(ResponseEntity.ok(fileDto.getContent()));
    }

    @Test
    void whenRequestFileByWrongIdThenGetResponseEntityNotFound() {
        when(fileService.getFileById(any(Integer.class))).thenReturn(Optional.empty());

        ResponseEntity<?> actualContent = fileController.getById(any(Integer.class));

        assertThat(actualContent).isEqualTo(ResponseEntity.notFound().build());
    }

}