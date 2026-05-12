package es.iesagora.fitjourney.data.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa una rutina de entrenamiento.
 * Una rutina contiene información general como el nombre,
 * descripción, imagen y usuario creador, además de la lista
 * de ejercicios que forman parte de ella.
 * Esta clase se utiliza para intercambiar datos entre
 * la API, los repositorios y las distintas pantallas
 * de la aplicación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Routine implements Serializable {

    /**
     * Identificador único de la rutina.
     */
    private int idRoutine;

    /**
     * Nombre de la rutina.
     * Ejemplo: "Rutina Full Body", "Pierna Avanzada", etc.
     */
    private String name;

    /**
     * Descripción o explicación de la rutina.
     */
    private String description;

    /**
     * URL de la imagen asociada a la rutina.
     */
    private String photoUrl;

    /**
     * Fecha de creación de la rutina.
     */
    private String creationDate;

    /**
     * Identificador del usuario que creó la rutina.
     */
    private int idUser;

    /**
     * Estado de la rutina.
     * 1 = Activa
     * 0 = Inactiva
     */
    private int active;

    private int isPublic;

    /**
     * Lista de ejercicios que forman parte de la rutina.
     */
    private List<RoutineExercise> exercises;

    /**
     * Devuelve la imagen que se mostrará en pantalla.
     * Si la rutina tiene una imagen asignada, devuelve esa URL.
     * En caso contrario, devuelve una imagen por defecto.
     *
     * @return URL de la imagen de la rutina.
     */
    public String getDisplayImage() {
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            return photoUrl;
        }

        return "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=500&auto=format&fit=crop";
    }
}