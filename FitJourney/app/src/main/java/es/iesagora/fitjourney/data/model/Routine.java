package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa una rutina de entrenamiento.
 * Contiene la información general de la rutina y
 * la lista de ejercicios asociados a ella.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Routine implements Serializable {

    /**
     * Identificador único de la rutina.
     */
    @SerializedName("id_routine")
    private int idRoutine;

    /**
     * Nombre de la rutina.
     */
    @SerializedName("name")
    private String name;

    /**
     * Descripción de la rutina.
     */
    @SerializedName("description")
    private String description;

    /**
     * URL de la imagen de la rutina.
     */
    @SerializedName("photo_url")
    private String photoUrl;

    /**
     * Fecha de creación de la rutina.
     */
    @SerializedName("creation_date")
    private String creationDate;

    /**
     * Identificador del usuario creador.
     */
    @SerializedName("id_user")
    private int idUser;

    /**
     * Indica si la rutina es pública o privada.
     */
    @SerializedName("is_public")
    private int isPublic;

    /**
     * Lista de ejercicios de la rutina.
     */
    private List<RoutineExercise> exercises;

    /**
     * Devuelve la imagen que se mostrará de la rutina.
     * Si no existe una imagen asignada, devuelve una por defecto.
     *
     * @return URL de la imagen a mostrar.
     */
    public String getDisplayImage() {
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            return photoUrl;
        }

        return "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=500&auto=format&fit=crop";
    }
}