package es.iesagora.fitjourney.data.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workout {
    private int id_workout;
    private int id_user;
    private int id_routine;
    private String workout_date;
    private Integer duration_minutes;
    private Double calories_burned;
    private String notes;
    private int active;
    private Routine routine;
    private List<WorkoutDetail> details;
}