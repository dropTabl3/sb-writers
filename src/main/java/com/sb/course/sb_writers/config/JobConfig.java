package com.sb.course.sb_writers.config;

import com.sb.course.sb_writers.model.*;
import com.sb.course.sb_writers.processor.FirstChunkJobProcessor;
import com.sb.course.sb_writers.reader.FirstChunkJobReader;
import com.sb.course.sb_writers.writer.FirstChunkJobWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

@Configuration
public class JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private FirstChunkJobReader firstChunkJobReader;

    @Autowired
    private FirstChunkJobProcessor firstChunkJobProcessor;

    @Autowired
    private FirstChunkJobWriter firstChunkJobWriter;

    //essentially the datasource configured in application.properties
//    @Autowired
//    private DataSource dataSource;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.unidatasource")
    public DataSource universityDataSource(){
        return DataSourceBuilder.create().build();
    }


    @Bean
    public Job chunkJob(){
        return jobBuilderFactory.get("CHUNK_JOB")
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .build();
    }

    public Step firstChunkStep(){
        return stepBuilderFactory.get("F_CHUNK_STEP")
                .<StudentJdbc, StudentJdbc>chunk(3)
                .reader(jdbcCursorItemReader())
                //.processor(firstChunkJobProcessor)
                .writer(firstChunkJobWriter)
                .build();
    }

    public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader(){
        JdbcCursorItemReader<StudentJdbc> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(universityDataSource());
        //in caso di mismatch utilizzare alias come best practice; tuttavia spring batch implementa un equals tale per cui anche senza alias funziona
        reader.setSql("SELECT id, first_name as firstName, last_name as lastName, email from STUDENT");
        reader.setRowMapper(new BeanPropertyRowMapper<>() {
            {
                setMappedClass(StudentJdbc.class);
            }
        });
        return reader;
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<StudentJdbc> flatFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource outputFile){
        FlatFileItemWriter<StudentJdbc> reader = new FlatFileItemWriter<>();
        reader.setResource(outputFile);
        //in caso di mismatch utilizzare alias come best practice; tuttavia spring batch implementa un equals tale per cui anche senza alias funziona
        reader.setSql("SELECT id, first_name as firstName, last_name as lastName, email from STUDENT");
        reader.setRowMapper(new BeanPropertyRowMapper<>() {
            {
                setMappedClass(StudentJdbc.class);
            }
        });
        return reader;
    }


}
