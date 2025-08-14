// package app.domain.batch;
//
// import javax.sql.DataSource;
//
// import org.springframework.batch.core.Job;
// import org.springframework.batch.core.Step;
// import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
// import org.springframework.batch.core.job.builder.JobBuilder;
// import org.springframework.batch.core.repository.JobRepository;
// import org.springframework.batch.core.step.builder.StepBuilder;
// import org.springframework.batch.item.data.MongoItemWriter;
// import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
// import org.springframework.batch.item.database.JdbcPagingItemReader;
// import org.springframework.batch.item.database.PagingQueryProvider;
// import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
// import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.jdbc.core.BeanPropertyRowMapper;
// import org.springframework.transaction.PlatformTransactionManager;
//
// import app.domain.mongo.model.entity.MongoStore;
// import app.domain.store.model.entity.Store;
// import lombok.RequiredArgsConstructor;
//
// @Configuration
// @EnableBatchProcessing
// @RequiredArgsConstructor
// public class RdbToMongoBatchJobConfig {
//
//     private final DataSource dataSource;
//     private final MongoTemplate mongoTemplate;
//     private final StoreToMongoProcessor storeToMongoProcessor;
//
//     @Bean
//     public Job rdbToMongoJob(JobRepository jobRepository, Step step1) {
//         return new JobBuilder("rdbToMongoJob", jobRepository)
//                 .start(step1)
//                 .build();
//     }
//
//     @Bean
//     public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
//         return new StepBuilder("step1", jobRepository)
//                 .<Store, MongoStore>chunk(100, transactionManager)
//                 .reader(reader())
//                 .processor(storeToMongoProcessor)
//                 .writer(writer())
//                 .build();
//     }
//
//     @Bean
//     public JdbcPagingItemReader<Store> reader() throws Exception {
//         return new JdbcPagingItemReaderBuilder<Store>()
//                 .name("storeReader")
//                 .dataSource(dataSource)
//                 .pageSize(100)
//                 .queryProvider(queryProvider())
//                 .rowMapper(new BeanPropertyRowMapper<>(Store.class))
//                 .build();
//     }
//
//     @Bean
//     public PagingQueryProvider queryProvider() throws Exception {
//         SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
//         factory.setDataSource(dataSource);
//         factory.setSelectClause("SELECT s.*, r.region_name, r.full_name, c.category_name");
//         factory.setFromClause("FROM p_store s JOIN p_region r ON s.region_id = r.region_id JOIN p_category c ON s.category_id = c.category_id");
//         factory.setSortKey("store_id");
//         return factory.getObject();
//     }
//
//     @Bean
//     public MongoItemWriter<MongoStore> writer() {
//         return new MongoItemWriterBuilder<MongoStore>()
//                 .template(mongoTemplate)
//                 .collection("stores")
//                 .build();
//     }
// }
