package es.iesagora.fitjourney.data.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDetail implements Serializable {
    private int id_detail;
    private int id_workout;
    private int id_exercise;
    private Integer sets_performed;
    private String reps_performed;
    private Double max_weight_used;
    private Exercise exercise;
}