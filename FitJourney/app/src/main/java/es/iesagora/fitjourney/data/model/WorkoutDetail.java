package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa el detalle de un ejercicio realizado
 * dentro de un entrenamiento.
 * Esta clase almacena la información específica de ejecución
 * de cada ejercicio, diferenciando entre hipertrofia, cardio
 * y estiramientos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDetail implements Serializable {

    @SerializedName("id_workout_detail")
    private int idDetail;

    @SerializedName("id_workout")
    private int idWorkout;

    @SerializedName("id_exercise")
    private int idExercise;

    /**
     * Número de series (sets) completadas.
     */
    @SerializedName("sets_performed")
    private Integer setsPerformed;


    /**
     * Repeticiones realizadas. Guardado como "12,10,8".
     */
    @SerializedName("reps_string")
    private String repsString;

    /**
     * Peso máximo utilizado durante el ejercicio de fuerza.
     */
    @SerializedName("max_weight")
    private Double maxWeight;

    /**
     * Minutos que ha durado el ejercicio de cardio o estiramiento.
     */
    @SerializedName("duration_minutes")
    private Double durationMinutes;

    /**
     * Velocidad media en km/h (Solo para ejercicios de Cardio).
     */
    @SerializedName("speed_kmh")
    private Double speedKmh;

    /**
     * Información completa del ejercicio asociado.
     */
    private Exercise exercise;
}