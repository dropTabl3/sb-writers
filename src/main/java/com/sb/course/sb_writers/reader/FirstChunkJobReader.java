package com.sb.course.sb_writers.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirstChunkJobReader implements ItemReader<Integer> {

    List<Integer> data = List.of(1,2,3,4,5,6,7,8,9,10);
    int i = 0;

    @Override
    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        System.out.println("#FirstChunkJobReader called");

        //restituisco item ed incremento il cursore
        if(i<data.size()){
            return data.get(i++);
        }

        return 0;
    }
}
