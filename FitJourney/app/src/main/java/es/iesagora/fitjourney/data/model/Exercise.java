package es.iesagora.fitjourney.data.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exercise implements Serializable {
    private int id_exercise;
    private String name;
    private String muscle_group;
    private String difficulty;
    private String description;
    private String image_url;
    private String video_url;
    private String equipment;
    private String creation_date;
    private int active;
}