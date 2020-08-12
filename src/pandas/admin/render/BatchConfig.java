package pandas.admin.render;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import pandas.admin.collection.Title;

import javax.persistence.EntityManagerFactory;

//@Configuration
//@EnableBatchProcessing
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public ItemProcessor<Title, byte[]> processor() {
        return new Processor();
    }

    public JpaPagingItemReader<Title> reader() {
        return new JpaPagingItemReaderBuilder<Title>()
                .name("titleReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select t from Title t")
                .pageSize(100)
                .maxItemCount(10)
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
    public Step step1(ItemWriter<byte[]> writer) {
        return stepBuilderFactory.get("step1")
                .<Title, byte[]>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    public static class Processor implements ItemProcessor<Title, byte[]>, ItemStream {
        private ChromeDriver chromeDriver;

        @Override
        public byte[] process(Title title) throws Exception {
            chromeDriver.get("https://web.archive.org.au/awa-nobanner/20130328232628/" + title.getTitleUrl());
            return chromeDriver.getScreenshotAs(OutputType.BYTES);
        }

        @Override
        public void open(ExecutionContext executionContext) throws ItemStreamException {
            this.chromeDriver = new ChromeDriver(new ChromeOptions().setHeadless(true));
        }

        @Override
        public void update(ExecutionContext executionContext) throws ItemStreamException {

        }

        @Override
        public void close() throws ItemStreamException {
            chromeDriver.quit();
        }
    }
}
