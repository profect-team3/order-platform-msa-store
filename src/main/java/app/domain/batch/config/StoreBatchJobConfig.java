package app.domain.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import app.domain.batch.dto.StoreMenuDto;
import app.domain.batch.job.StoreBatchReader;
import app.domain.batch.job.StoreBatchProcessor;
import app.domain.batch.job.StoreBatchWriter;
import app.domain.mongo.model.entity.StoreCollection;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class StoreBatchJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final StoreBatchReader storeBatchReader;
	private final StoreBatchProcessor storeBatchProcessor;
	private final StoreBatchWriter storeBatchWriter;

	@Bean
	public Job storeBatchJob() {
		return new JobBuilder("storeBatchJob", jobRepository)
			.start(storeBatchStep())
			.build();
	}

	@Bean
	public Step storeBatchStep() {
		return new StepBuilder("storeBatchStep", jobRepository)
			.<StoreMenuDto, StoreCollection>chunk(100, transactionManager)
			.reader(storeBatchReader)
			.processor(storeBatchProcessor)
			.writer(storeBatchWriter)
			.build();
	}

}
