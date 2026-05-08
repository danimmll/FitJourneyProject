package es.iesagora.fitjourney.data.model;

import java.io.Serializable;
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
    private int id_workout;

    /**
     * Identificador del usuario que realizó el entrenamiento.
     */
    private int id_user;

    /**
     * Identificador de la rutina utilizada en el entrenamiento.
     */
    private int id_routine;

    /**
     * Fecha en la que se realizó el entrenamiento.
     */
    private String workout_date;

    /**
     * Duración total del entrenamiento en minutos.
     */
    private Integer duration_minutes;

    /**
     * Cantidad estimada de calorías quemadas.
     */
    private Double calories_burned;

    /**
     * Notas u observaciones añadidas por el usuario.
     */
    private String notes;

    /**
     * Estado del entrenamiento.
     * 1 = Activo
     * 0 = Inactivo
     */
    private int active;

    /**
     * Información completa de la rutina asociada.
     */
    private Routine routine;

    /**
     * Lista de ejercicios realizados durante el entrenamiento.
     */
    private List<WorkoutDetail> details;
}