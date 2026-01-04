package com.sb.course.sb_writers.listener;

import com.sb.course.sb_writers.model.StudentCsv;
import com.sb.course.sb_writers.model.StudentJSON;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
@Component
public class SkipListenerImpl implements SkipListener<StudentCsv, StudentJSON> {
    @Override
    public void onSkipInRead(Throwable throwable) {
        if(throwable instanceof FlatFileParseException) {
            createFile("C:\\Users\\massi\\workspace\\repositories\\spring-batch-root\\sb-writers\\logs\\job_1\\step_1\\reader\\SkipInRead.txt",
                    ((FlatFileParseException) throwable).getInput());
        }
    }

    @Override
    public void onSkipInWrite(StudentJSON studentJSON, Throwable throwable) {
        createFile("C:\\Users\\massi\\workspace\\repositories\\spring-batch-root\\sb-writers\\logs\\job_1\\step_1\\writer\\SkipInWrite.txt",
                studentJSON.toString());
    }

    @Override
    public void onSkipInProcess(StudentCsv studentCsv, Throwable throwable) {
        createFile("C:\\Users\\massi\\workspace\\repositories\\spring-batch-root\\sb-writers\\logs\\job_1\\step_1\\processor\\SkipInProcess.txt",
                studentCsv.toString());
    }

    public void createFile(String filePath, String data) {
        try(FileWriter fileWriter = new FileWriter(new File(filePath), true)) {
            fileWriter.write(data + "," + new Date() + "\n");
        }catch(Exception e) {

        }
    }
}
