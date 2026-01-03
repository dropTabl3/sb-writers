package com.sb.course.sb_writers.writer;

import com.sb.course.sb_writers.model.StudentResponse;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirstChunkJobWriter implements ItemWriter<StudentResponse> {

    @Override
    public void write(List<? extends StudentResponse> input){
        System.out.println("#FirstChunkJobWriter called");
        //input.forEach(System.out::println);
        for(StudentResponse studentResponse : input){
            System.out.println(studentResponse);
        }
    }
}
