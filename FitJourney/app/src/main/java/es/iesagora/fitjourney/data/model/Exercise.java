package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa un ejercicio dentro de la aplicación.
 * Guarda toda la información necesaria de un ejercicio físico:
 * nombre, grupo muscular, dificultad, descripción, imágenes,
 * vídeos, equipamiento, tipo y estado.
 * Esta clase se utiliza para recibir y enviar datos entre
 * la API, el repositorio y las pantallas de la app.
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
     * Ejemplo: "Press Banca", "Sentadilla", etc.
     */
    @SerializedName("name")
    private String name;

    /**
     * Grupo muscular principal que trabaja el ejercicio.
     * Ejemplo: "Pecho", "Piernas", "Espalda", etc.
     */
    @SerializedName("muscle_group")
    private String muscleGroup;

    /**
     * Nivel de dificultad del ejercicio.
     * Puede ser: Principiante, Intermedio o Experto.
     */
    @SerializedName("difficulty")
    private String difficulty;

    /**
     * Explicación o instrucciones del ejercicio.
     */
    @SerializedName("description")
    private String description;

    /**
     * URL de la imagen asociada al ejercicio.
     */
    @SerializedName("image_url")
    private String imageUrl;

    /**
     * URL del vídeo demostrativo del ejercicio.
     */
    @SerializedName("video_url")
    private String videoUrl;

    /**
     * Equipamiento necesario para realizar el ejercicio.
     * Ejemplo: Mancuernas, barra, máquina, etc.
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
     * Valores posibles:
     * - Hipertrofia
     * - Cardio
     * - Estiramiento
     */
    @SerializedName("exercise_type")
    private String exerciseType;
}