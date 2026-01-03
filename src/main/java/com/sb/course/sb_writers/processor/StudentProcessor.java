package com.sb.course.sb_writers.processor;

import com.sb.course.sb_writers.model.StudentJSON;
import com.sb.course.sb_writers.model.StudentJdbc;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

//<Input,Output>
@Component
public class StudentProcessor implements ItemProcessor<StudentJdbc, StudentJSON> {
    @Override
    public StudentJSON process(StudentJdbc data) throws Exception {
        System.out.println("#FirstChunkJobProcessor called");
        return StudentJSON.builder()
                .id(data.getId())
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
                .email(data.getEmail())
                .build();
    }
}
