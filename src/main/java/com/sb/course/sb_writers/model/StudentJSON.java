package com.sb.course.sb_writers.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentJSON {

    //use @JsonProperty("correct_name_from_json") if the JSON field names are different from the variable names
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
