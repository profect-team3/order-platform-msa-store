package app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import app.domain.batch.controller.BulkController;

@WebMvcTest(BulkController.class)
@DisplayName("BulkController 테스트")
public class BulkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobLauncher jobLauncher;

    @MockitoBean
    @Qualifier("storeBatchJob")
    private Job storeBatchJob;

    private JobExecution jobExecution;

    @BeforeEach
    void setUp() {
        JobInstance jobInstance = new JobInstance(1L, "storeBatchJob");
        jobExecution = new JobExecution(jobInstance, new JobParameters());
    }

    @Test
    @DisplayName("성공: 배치 작업 시작")
    @WithMockUser
    void startBatchJob_Success() throws Exception {
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);

        mockMvc.perform(post("/store/jobs/store-sync")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Batch job started successfully."))
            .andExpect(jsonPath("$.jobId").value(jobExecution.getJobId()));
    }

    @Test
    @DisplayName("실패: 이미 실행 중인 작업")
    @WithMockUser
    void startBatchJob_AlreadyRunning() throws Exception {
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .thenThrow(new JobExecutionAlreadyRunningException("Job is already running"));

        mockMvc.perform(post("/store/jobs/store-sync")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Job is already running: Job is already running"));
    }

    @Test
    @DisplayName("실패: 잘못된 작업 파라미터")
    @WithMockUser
    void startBatchJob_InvalidParameters() throws Exception {
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .thenThrow(new JobParametersInvalidException("Invalid job parameters"));

        mockMvc.perform(post("/store/jobs/store-sync")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("Error while starting job: Invalid job parameters"));
    }

    @Test
    @DisplayName("실패: 예기치 않은 오류")
    @WithMockUser
    void startBatchJob_UnexpectedError() throws Exception {
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/store/jobs/store-sync")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred: Unexpected error"));
    }
}
