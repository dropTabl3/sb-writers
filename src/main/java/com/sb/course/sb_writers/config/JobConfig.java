package com.sb.course.sb_writers.config;

import com.sb.course.sb_writers.model.*;
import com.sb.course.sb_writers.processor.FirstChunkJobProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;


import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
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
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;

@Configuration
public class JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private FirstChunkJobProcessor firstChunkJobProcessor;

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
                .writer(flatFileItemWriter(null))
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
        FlatFileItemWriter<StudentJdbc> flatFileItemWriter = new FlatFileItemWriter<>();
        flatFileItemWriter.setResource(outputFile);

        //header colonne
        flatFileItemWriter.setHeaderCallback(new FlatFileHeaderCallback(){
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("ID,FIRST_NAME,LAST_NAME,EMAIL");
            }
        });

        //mappatura campi modello su colonne
        flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<StudentJdbc>(){
            {
                setFieldExtractor(new BeanWrapperFieldExtractor<StudentJdbc>(){
                    {
                        setNames(new String[]{"id","firstName","lastName","email"});
                    }
                });
            }
        });

        flatFileItemWriter.setFooterCallback(new FlatFileFooterCallback() {
            @Override
            public void writeFooter(Writer writer) throws IOException {
                writer.write("Created @: " + LocalDateTime.now());
            }
        });
        return flatFileItemWriter;
    }


}
