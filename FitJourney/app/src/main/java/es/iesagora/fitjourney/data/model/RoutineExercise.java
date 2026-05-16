package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa la relación entre una rutina y un ejercicio.
 * Guarda la configuración específica de un ejercicio dentro
 * de una rutina de entrenamiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExercise implements Serializable {

    /**
     * Identificador único de la relación rutina-ejercicio.
     */
    @SerializedName("id_routine_exercise")
    private int idRoutineExercise;

    /**
     * Identificador de la rutina.
     */
    @SerializedName("id_routine")
    private int idRoutine;

    /**
     * Identificador del ejercicio.
     */
    @SerializedName("id_exercise")
    private int idExercise;

    /**
     * Número de series objetivo.
     */
    @SerializedName("target_sets")
    private Integer targetSets;

    /**
     * Número de repeticiones objetivo.
     */
    @SerializedName("target_reps")
    private Integer targetReps;

    /**
     * Peso objetivo recomendado.
     */
    @SerializedName("target_weight")
    private Double targetWeight;

    /**
     * Información completa del ejercicio asociado.
     */
    private Exercise exercise;
}