package com.sb.course.sb_writers;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
@ComponentScan({
        "com.sb.course.sb_readers.config",
        "com.sb.course.sb_readers.service",
        "com.sb.course.sb_readers.reader",
        "com.sb.course.sb_readers.processor",
        "com.sb.course.sb_readers.writer",
})
public class SbWritersApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbWritersApplication.class, args);
	}

}
