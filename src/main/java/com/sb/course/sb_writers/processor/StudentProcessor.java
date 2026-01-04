package com.sb.course.sb_writers.processor;

import com.sb.course.sb_writers.model.StudentCsv;
import com.sb.course.sb_writers.model.StudentJSON;
import com.sb.course.sb_writers.model.StudentJdbc;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

//<Input,Output>
@Component
public class StudentProcessor implements ItemProcessor<StudentCsv, StudentJSON> {
    @Override
    public StudentJSON process(StudentCsv data) throws Exception {
        System.out.println("#StudentProcessor called");
        if(data.getId() == 6) {
            throw new NullPointerException();
        }
        return StudentJSON.builder()
                .id(data.getId())
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
                .email(data.getEmail())
                .build();
    }
}
