package com.sb.course.sb_writers.config;

import com.sb.course.sb_writers.listener.SkipListenerImpl;
import com.sb.course.sb_writers.model.*;
import com.sb.course.sb_writers.processor.StudentProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;


import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Configuration
public class JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private StudentProcessor studentProcessor;

    @Autowired
    private SkipListenerImpl skipListenerImpl;

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
                .<StudentCsv, StudentJSON>chunk(3)
                .reader(flatFileItemReader(null))
                .processor(studentProcessor)
                .writer(jsonFileItemWriter(null))
                .faultTolerant()
                .skip(Throwable.class)
                .skipLimit(100)
//                .retryLimit(3)
//                .retry(Throwable.class)
                .listener(skipListenerImpl)
//                .writer(jdbcBatchItemWriter(null))
//                .writer(jdbcBatchItemWriterPrepState())
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<StudentCsv> flatFileItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource inputFilePath
    ){
        FlatFileItemReader<StudentCsv> reader = new FlatFileItemReader<>();
        reader.setResource(inputFilePath);
        reader.setLineMapper(new DefaultLineMapper<StudentCsv>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer(){
                    {
                        setNames("ID", "First Name", "Last Name", "Email");
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<StudentCsv>(){
                    {
                        setTargetType(StudentCsv.class);
                    }
                });
            }
        });
        reader.setLinesToSkip(1); //skip header
        return reader;
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
    public JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource outputFile){
        JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setDataSource(universityDataSource());
        jdbcBatchItemWriter.setSql(
                "INSERT INTO STUDENT(id, first_name, last_name, email) " +
                "VALUES (:id, :firstName, :lastName, :email)"
        );
        //serve a dire a spring che usiamo i nomi delle proprietà del bean StudentCsv per mappare i parametri SQL
        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<StudentCsv>());
        return jdbcBatchItemWriter;
    }
    @Bean
    public JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriterPrepState(){
        JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setDataSource(universityDataSource());
        jdbcBatchItemWriter.setSql(
                "INSERT INTO STUDENT(id, first_name, last_name, email) " +
                        "VALUES (?, ?, ?, ?)"
        );
        jdbcBatchItemWriter.setItemPreparedStatementSetter(new ItemPreparedStatementSetter<StudentCsv>() {
            @Override
            public void setValues(StudentCsv studentCsv, PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, studentCsv.getId());
                preparedStatement.setString(2, studentCsv.getFirstName());
                preparedStatement.setString(3, studentCsv.getLastName());
                preparedStatement.setString(4, studentCsv.getEmail());
            }
        });
        return jdbcBatchItemWriter;
    }


    @StepScope
    @Bean
    public JsonFileItemWriter<StudentJSON> jsonFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource outputFile){
        JsonFileItemWriter<StudentJSON> jsonFileItemWriter =
                new JsonFileItemWriter<>(
                        outputFile,
                        new JacksonJsonObjectMarshaller<StudentJSON>()
                );
        return jsonFileItemWriter;
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
