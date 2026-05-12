package es.iesagora.fitjourney.data.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa un ejercicio dentro de la aplicación.
 * Guarda toda la información necesaria de un ejercicio físico:
 * nombre, grupo muscular, dificultad, descripción, imágenes,
 * vídeos, equipamiento y estado.
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
    private int idExercise;

    /**
     * Nombre del ejercicio.
     * Ejemplo: "Press Banca", "Sentadilla", etc.
     */
    private String name;

    /**
     * Grupo muscular principal que trabaja el ejercicio.
     * Ejemplo: "Pecho", "Piernas", "Espalda", etc.
     */
    private String muscleGroup;

    /**
     * Nivel de dificultad del ejercicio.
     * Puede ser: Fácil, Medio o Difícil.
     */
    private String difficulty;

    /**
     * Explicación o instrucciones del ejercicio.
     */
    private String description;

    /**
     * URL de la imagen asociada al ejercicio.
     */
    private String imageUrl;

    /**
     * URL del vídeo demostrativo del ejercicio.
     */
    private String videoUrl;

    /**
     * Equipamiento necesario para realizar el ejercicio.
     * Ejemplo: Mancuernas, barra, máquina, etc.
     */
    private String equipment;

    /**
     * Fecha de creación del ejercicio.
     */
    private String creationDate;

    /**
     * Estado del ejercicio.
     * 1 = Activo
     * 0 = Inactivo
     */
    private int active;
}