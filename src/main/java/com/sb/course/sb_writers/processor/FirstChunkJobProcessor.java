package com.sb.course.sb_writers.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

//<Input,Output>
@Component
public class FirstChunkJobProcessor implements ItemProcessor<Integer, Long> {
    @Override
    public Long process(Integer integer) throws Exception {
        System.out.println("#FirstChunkJobProcessor called");
        return Long.valueOf(integer + 100);
    }
}
