package app.domain.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.PlatformTransactionManager;

import app.domain.batch.dto.BulkDto;
import app.domain.batch.job.BulkReader;
import app.domain.batch.job.BulkProcessor;
import app.domain.batch.job.BulkWriter;
import app.domain.mongo.model.entity.StoreCollection;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BulkJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final BulkReader bulkReader;
	private final BulkProcessor bulkProcessor;
	private final BulkWriter bulkWriter;
	private final DiscordListener discordListener;

	@Bean
	public Job storeBatchJob() {
		return new JobBuilder("storeBatchJob", jobRepository)
			.start(storeBatchStep())
			.listener(discordListener)
			.build();
	}

	@Bean
	public Step storeBatchStep() {
		return new StepBuilder("storeBatchStep", jobRepository)
			.<BulkDto, StoreCollection>chunk(100, transactionManager)
			.reader(bulkReader)
			.processor(bulkProcessor)
			.writer(bulkWriter)
			.faultTolerant()
			.retryLimit(3)
			.retry(DuplicateKeyException.class)
			.build();
	}

}
