package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa la relación entre una rutina y un ejercicio.
 * Esta clase almacena la configuración concreta de un ejercicio
 * dentro de una rutina, como las series, repeticiones y peso objetivo.
 * También contiene la información completa del ejercicio asociado.
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
     * Identificador de la rutina a la que pertenece el ejercicio.
     */
    @SerializedName("id_routine")
    private int idRoutine;

    /**
     * Identificador del ejercicio asociado.
     */
    @SerializedName("id_exercise")
    private int idExercise;

    /**
     * Número de series objetivo para el ejercicio.
     */
    @SerializedName("target_sets")
    private Integer targetSets;

    /**
     * Número de repeticiones objetivo por serie.
     */
    @SerializedName("target_reps")
    private Integer targetReps;

    /**
     * Peso objetivo recomendado para realizar el ejercicio.
     */
    @SerializedName("target_weight")
    private Double targetWeight;

    /**
     * Información completa del ejercicio asociado.
     */
    private Exercise exercise;
}