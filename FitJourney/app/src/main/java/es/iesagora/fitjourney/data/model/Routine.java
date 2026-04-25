package es.iesagora.fitjourney.data.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Routine {
    private int id_routine;
    private String name;
    private String description;
    private String creation_date;
    private int id_user;
    private int active;
    private List<RoutineExercise> exercises;
}