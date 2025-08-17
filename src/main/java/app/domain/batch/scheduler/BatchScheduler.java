package app.domain.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// @Component  // 필요시 주석 해제하여 활성화
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    @Qualifier("storeBatchJob")
    private final Job storeBatchJob;

    @Scheduled(cron = "0 0 4 * * *")  // 매일 새벽 4시에 실행
    public void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(storeBatchJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}