package es.iesagora.fitjourney.data.model;

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
    private int id_routine_exercise;

    /**
     * Identificador de la rutina a la que pertenece el ejercicio.
     */
    private int id_routine;

    /**
     * Identificador del ejercicio asociado.
     */
    private int id_exercise;

    /**
     * Número de series objetivo para el ejercicio.
     */
    private Integer target_sets;

    /**
     * Número de repeticiones objetivo por serie.
     */
    private Integer target_reps;

    /**
     * Peso objetivo recomendado para realizar el ejercicio.
     */
    private Double target_weight;

    /**
     * Información completa del ejercicio asociado.
     */
    private Exercise exercise;
}