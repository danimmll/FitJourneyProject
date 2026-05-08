package es.iesagora.fitjourney.data.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa a un usuario de la aplicación.
 * Guarda toda la información personal y de configuración
 * relacionada con el usuario, como sus datos físicos,
 * objetivos, fotografía y rol dentro de la app.
 * Esta clase se utiliza para enviar y recibir información
 * entre la API, los repositorios y las pantallas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    /**
     * Identificador único del usuario.
     */
    private int id_user;

    /**
     * Correo electrónico del usuario.
     */
    private String email;

    /**
     * Nombre del usuario.
     */
    private String name;

    /**
     * Contraseña del usuario.
     */
    private String password;

    /**
     * Género del usuario.
     */
    private String gender;

    /**
     * Objetivo fitness del usuario.
     * Ejemplo:
     * - Ganar masa muscular
     * - Perder peso
     * - Mantenerse en forma
     */
    private String goal;

    /**
     * Altura del usuario en centímetros.
     */
    private Double height;

    /**
     * Peso del usuario en kilogramos.
     */
    private Double weight;

    /**
     * Fecha de nacimiento del usuario.
     */
    private String birth_date;

    /**
     * URL de la foto de perfil del usuario.
     */
    private String photo_url;

    /**
     * Rol del usuario dentro de la aplicación.
     * Ejemplo:
     * - user
     * - admin
     */
    private String role;

    /**
     * Estado del usuario.
     * 1 = Activo
     * 0 = Inactivo
     */
    private int active;

    /**
     * Fecha de registro del usuario.
     */
    private String register_date;

    /**
     * Constructor personalizado utilizado en algunos procesos
     * de registro o creación manual de usuarios.
     *
     * @param idUser     Identificador del usuario.
     * @param name       Nombre del usuario.
     * @param email      Correo electrónico.
     * @param pass       Contraseña.
     * @param birthDate  Fecha de nacimiento.
     * @param gender     Género del usuario.
     * @param goal       Objetivo fitness.
     * @param height     Altura en centímetros.
     * @param weight     Peso en kilogramos.
     */
    public User(String idUser, String name, String email, String pass,
                String birthDate, String gender, String goal,
                int height, float weight) {
    }

    /**
     * Devuelve la imagen de perfil que se mostrará en pantalla.
     * Si el usuario tiene una foto personalizada, se devuelve esa.
     * En caso contrario, se genera automáticamente un avatar
     * utilizando el nombre del usuario.
     * @return URL de la imagen de perfil.
     */
    public String getDisplayAvatar() {

        if (this.photo_url != null && !this.photo_url.trim().isEmpty()) {
            return this.photo_url;
        }

        String safeName = (this.name != null)
                ? this.name.replace(" ", "%20")
                : "Usuario";

        return "https://api.dicebear.com/7.x/avataaars/png?seed="
                + safeName
                + "&backgroundColor=b6e3f4,c0aede,d1d4f9";
    }
}