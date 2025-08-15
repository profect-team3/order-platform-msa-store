package app.domain.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import app.domain.batch.model.dto.StoreMenuDto;
import app.domain.mongo.model.entity.StoreCollection;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RdbToMongoBatchJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final DataSource dataSource;
	private final StoreProcessor storeProcessor;
	private final UpsertMongoStoreWriter upsertMongoStoreWriter;

	@Bean
	public Job rdbToMongoJob() {
		return new JobBuilder("rdbToMongoJob", jobRepository)
			.start(storeSyncStep())
			.build();
	}

	@Bean
	public Step storeSyncStep() {
		return new StepBuilder("storeSyncStep", jobRepository)
			.<StoreMenuDto, StoreCollection>chunk(100, transactionManager)
			.reader(storeReader())
			.processor(storeProcessor)
			.writer(upsertMongoStoreWriter)
			.build();
	}

	@Bean
	public JdbcCursorItemReader<StoreMenuDto> storeReader() {
		String baseSql = "SELECT s.*, r.region_name, r.full_name, c.category_name, " +
			"COALESCE(AVG(review.rating), 0) AS avgRating, " +
			"json_agg(json_build_object('menuId', m.menu_id, 'name', m.name, 'price', m.price, 'description', m.description, 'isHidden', m.is_hidden)) AS menuJson " +
			"FROM p_store s " +
			"JOIN p_region r ON s.region_id = r.region_id " +
			"JOIN p_category c ON s.category_id = c.category_id " +
			"LEFT JOIN p_menu m ON s.store_id = m.store_id " +
			"LEFT JOIN p_review review ON s.store_id = review.store_id " +
			"GROUP BY s.store_id, r.region_id, c.category_id";

		return new JdbcCursorItemReaderBuilder<StoreMenuDto>()
			.name("storeReader")
			.fetchSize(100)
			.sql(baseSql)
			.dataSource(dataSource)
			.rowMapper(new BeanPropertyRowMapper<>(StoreMenuDto.class))
			.build();
	}
}