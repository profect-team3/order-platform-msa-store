// package app.domain.batch;
//
// import org.springframework.batch.core.Job;
// import org.springframework.batch.core.JobParameters;
// import org.springframework.batch.core.JobParametersBuilder;
// import org.springframework.batch.core.launch.JobLauncher;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
//
// import lombok.RequiredArgsConstructor;
//
// @Component
// @RequiredArgsConstructor
// public class BatchScheduler {
//
//     private final JobLauncher jobLauncher;
//     private final Job rdbToMongoJob;
//
//     @Scheduled(cron = "0 */10 * * * *")
//     public void runJob() {
//         try {
//             JobParameters jobParameters = new JobParametersBuilder()
//                     .addString("JobID", String.valueOf(System.currentTimeMillis()))
//                     .toJobParameters();
//             jobLauncher.run(rdbToMongoJob, jobParameters);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }
