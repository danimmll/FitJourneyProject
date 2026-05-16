package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa el detalle de un ejercicio realizado
 * dentro de un entrenamiento.
 * Guarda la información específica de ejecución de cada ejercicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDetail implements Serializable {

    /**
     * Identificador único del detalle.
     */
    @SerializedName("id_workout_detail")
    private int idDetail;

    /**
     * Identificador del entrenamiento asociado.
     */
    @SerializedName("id_workout")
    private int idWorkout;

    /**
     * Identificador del ejercicio realizado.
     */
    @SerializedName("id_exercise")
    private int idExercise;

    /**
     * Número de series completadas.
     */
    @SerializedName("sets_performed")
    private Integer setsPerformed;

    /**
     * Repeticiones realizadas.
     * Ejemplo: "12,10,8".
     */
    @SerializedName("reps_string")
    private String repsString;

    /**
     * Peso máximo utilizado.
     */
    @SerializedName("max_weight")
    private Double maxWeight;

    /**
     * Duración del ejercicio en minutos.
     */
    @SerializedName("duration_minutes")
    private Double durationMinutes;

    /**
     * Velocidad media en km/h.
     */
    @SerializedName("speed_kmh")
    private Double speedKmh;

    /**
     * Información completa del ejercicio asociado.
     */
    private Exercise exercise;
}