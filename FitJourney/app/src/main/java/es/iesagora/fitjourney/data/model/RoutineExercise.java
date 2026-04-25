package es.iesagora.fitjourney.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExercise {
    private int id_routine_exercise;
    private int id_routine;
    private int id_exercise;
    private Integer target_sets;
    private Integer target_reps;
    private Double target_weight;
    private Exercise exercise;
}