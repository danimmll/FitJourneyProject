package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa un ejercicio físico dentro de la aplicación.
 * Contiene toda la información necesaria para mostrar y gestionar
 * ejercicios obtenidos desde la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exercise implements Serializable {

    /**
     * Identificador único del ejercicio.
     */
    @SerializedName("id_exercise")
    private Integer idExercise;

    /**
     * Nombre del ejercicio.
     */
    @SerializedName("name")
    private String name;

    /**
     * Grupo muscular principal trabajado.
     */
    @SerializedName("muscle_group")
    private String muscleGroup;

    /**
     * Nivel de dificultad del ejercicio.
     */
    @SerializedName("difficulty")
    private String difficulty;

    /**
     * Descripción o instrucciones del ejercicio.
     */
    @SerializedName("description")
    private String description;

    /**
     * URL de la imagen del ejercicio.
     */
    @SerializedName("image_url")
    private String imageUrl;

    /**
     * URL del vídeo demostrativo.
     */
    @SerializedName("video_url")
    private String videoUrl;

    /**
     * Equipamiento necesario para realizar el ejercicio.
     */
    @SerializedName("equipment")
    private String equipment;

    /**
     * Fecha de creación del ejercicio.
     */
    @SerializedName("creation_date")
    private String creationDate;

    /**
     * Tipo de ejercicio.
     */
    @SerializedName("exercise_type")
    private String exerciseType;

    /**
     * Devuelve la imagen que se mostrará del ejercicio.
     * Si no existe una imagen asignada, devuelve una por defecto.
     *
     * @return URL de la imagen a mostrar.
     */
    public String getDisplayImage() {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            return imageUrl;
        }

        return "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1200&auto=format&fit=crop";
    }
}