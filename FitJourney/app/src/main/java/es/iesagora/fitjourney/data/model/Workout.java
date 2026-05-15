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
 * Un entrenamiento puede estar asociado a una rutina concreta
 * y contiene información como la fecha, duración, calorías
 * quemadas y observaciones del usuario.
 * También almacena el detalle de los ejercicios realizados
 * durante la sesión de entrenamiento.
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
     * Identificador del usuario que realizó el entrenamiento.
     */
    @SerializedName("id_user")
    private int idUser;

    /**
     * Identificador de la rutina utilizada en el entrenamiento.
     */
    @SerializedName("id_routine")
    private int idRoutine;

    /**
     * Fecha en la que se realizó el entrenamiento.
     */
    @SerializedName("workout_date")
    private Date workoutDate;

    /**
     * Duración total del entrenamiento en minutos.
     */
    @SerializedName("duration_minutes")
    private Integer durationMinutes;

    /**
     * Cantidad estimada de calorías quemadas.
     */
    @SerializedName("calories_burned")
    private Double caloriesBurned;

    /**
     * Notas u observaciones añadidas por el usuario.
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
     * Lista de ejercicios realizados durante el entrenamiento.
     */
    private List<WorkoutDetail> details;
}