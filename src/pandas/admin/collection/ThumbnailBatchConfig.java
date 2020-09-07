package pandas.admin.collection;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class ThumbnailBatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public ThumbnailBatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public JpaPagingItemReader<Title> reader() {
        return new JpaPagingItemReaderBuilder<Title>()
                .name("titleReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select t from Title t where t.thumbnails is empty and (t.titleUrl is not null or t.seedUrl is not null)")
                .pageSize(100)
                .build();
    }

    @Bean
    public Job thumbnailJob(Step step1) {
        return jobBuilderFactory.get("thumbnailJob2")
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("step1")
                .<Title, Thumbnail>chunk(1)
                .reader(reader())
                .processor(new ThumbnailProcessor())
                .writer(writer())
                .taskExecutor(taskExecutor)
                .throttleLimit(8)
                .build();
    }

    public JpaItemWriter<Thumbnail> writer() {
        JpaItemWriter<Thumbnail> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}
