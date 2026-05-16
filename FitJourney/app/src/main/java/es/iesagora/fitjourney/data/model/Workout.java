package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa un entrenamiento realizado por un usuario.
 * Guarda la información general de la sesión y los ejercicios
 * realizados durante el entrenamiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workout implements Serializable {

    /**
     * Identificador único del entrenamiento.
     */
    @SerializedName("id_workout")
    private int idWorkout;

    /**
     * Identificador del usuario.
     */
    @SerializedName("id_user")
    private int idUser;

    /**
     * Identificador de la rutina utilizada.
     */
    @SerializedName("id_routine")
    private int idRoutine;

    /**
     * Fecha del entrenamiento.
     */
    @SerializedName("workout_date")
    private Date workoutDate;

    /**
     * Duración total en minutos.
     */
    @SerializedName("duration_minutes")
    private Integer durationMinutes;

    /**
     * Calorías estimadas quemadas.
     */
    @SerializedName("calories_burned")
    private Double caloriesBurned;

    /**
     * Notas u observaciones del usuario.
     */
    @SerializedName("notes")
    private String notes;

    /**
     * Información completa de la rutina asociada.
     */
    private Routine routine;

    /**
     * Información completa del usuario asociado.
     */
    private User user;

    /**
     * Lista de ejercicios realizados en el entrenamiento.
     */
    private List<WorkoutDetail> details;
}