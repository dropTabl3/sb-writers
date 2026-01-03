package com.sb.course.sb_writers;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
@ComponentScan({
        "com.sb.course.sb_writers.config",
        "com.sb.course.sb_writers.service",
        "com.sb.course.sb_writers.reader",
        "com.sb.course.sb_writers.processor",
        "com.sb.course.sb_writers.writer",
})
public class SbWritersApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbWritersApplication.class, args);
	}

}
