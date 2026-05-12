package es.iesagora.fitjourney.data.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa el detalle de un ejercicio realizado
 * dentro de un entrenamiento.
 * Esta clase almacena la información específica de ejecución
 * de cada ejercicio, como las series realizadas, repeticiones
 * y peso máximo utilizado.
 * También contiene la información completa del ejercicio asociado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDetail implements Serializable {

    /**
     * Identificador único del detalle del entrenamiento.
     */
    private int idDetail;

    /**
     * Identificador del entrenamiento al que pertenece.
     */
    private int idWorkout;

    /**
     * Identificador del ejercicio realizado.
     */
    private int idExercise;

    /**
     * Número de series realizadas durante el ejercicio.
     */
    private Integer setsPerformed;

    /**
     * Repeticiones realizadas.
     * Puede almacenarse como texto para permitir formatos
     * personalizados como "12-10-8" o similares.
     */
    private String repsPerformed;

    /**
     * Peso máximo utilizado durante el ejercicio.
     */
    private Double maxWeightUsed;

    /**
     * Información completa del ejercicio asociado.
     */
    private Exercise exercise;
}