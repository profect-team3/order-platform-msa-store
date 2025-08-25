package app.domain.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BulkScheduler {

    private final JobLauncher jobLauncher;
    @Qualifier("storeBatchJob")
    private final Job storeBatchJob;

    @Scheduled(cron = "0 0 4 * * *")
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