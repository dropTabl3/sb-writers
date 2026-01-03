package com.sb.course.sb_writers.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "student") //no more in jdk 11+ - need to add dependency jakarta.xml.bind-api
public class StudentXML {

    //use @JsonProperty("correct_name_from_json") if the JSON field names are different from the variable names
    private Long id;
    //@XmlElement(name = "firstName") specify XML element name if different
    private String firstName;
    private String lastName;
    private String email;
}
