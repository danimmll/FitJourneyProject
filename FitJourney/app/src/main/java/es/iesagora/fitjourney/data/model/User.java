package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa a un usuario de la aplicación.
 * Contiene la información personal, física y de acceso
 * necesaria para gestionar la cuenta del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    /**
     * Identificador único del usuario.
     */
    @SerializedName("id_user")
    private Integer idUser;

    /**
     * Correo electrónico del usuario.
     */
    @SerializedName("email")
    private String email;

    /**
     * Nombre completo del usuario.
     */
    @SerializedName("name")
    private String name;

    /**
     * Contraseña del usuario.
     */
    @SerializedName("password")
    private String password;

    /**
     * Género del usuario.
     */
    @SerializedName("gender")
    private String gender;

    /**
     * Objetivo físico del usuario.
     */
    @SerializedName("goal")
    private String goal;

    /**
     * Altura del usuario en centímetros.
     */
    @SerializedName("height")
    private Double height;

    /**
     * Peso del usuario en kilogramos.
     */
    @SerializedName("weight")
    private Double weight;

    /**
     * Fecha de nacimiento del usuario.
     */
    @SerializedName("birth_date")
    private String birthDate;

    /**
     * URL de la foto de perfil.
     */
    @SerializedName("photo_url")
    private String photoUrl;

    /**
     * Rol del usuario dentro de la aplicación.
     */
    @SerializedName("role")
    private String role;

    /**
     * Estado del usuario.
     */
    @SerializedName("active")
    private Integer active;

    /**
     * Fecha de registro del usuario.
     */
    @SerializedName("register_date")
    private String registerDate;

    /**
     * Constructor personalizado para crear usuarios
     * con los datos principales de registro.
     */
    public User(Integer idUser, String name, String email, String pass,
                String birthDate, String gender, String goal,
                Double height, Double weight) {
        this.idUser     = idUser;
        this.name       = name;
        this.email      = email;
        this.password   = pass;
        this.birthDate  = birthDate;
        this.gender     = gender;
        this.goal       = goal;
        this.height     = height;
        this.weight     = weight;
    }

    /**
     * Devuelve la imagen de perfil que se mostrará del usuario.
     * Si no existe una foto personalizada, genera un avatar por defecto.
     *
     * @return URL de la imagen de perfil.
     */
    public String getDisplayAvatar() {
        if (this.photoUrl != null && !this.photoUrl.trim().isEmpty()) {
            return this.photoUrl;
        }

        String safeName = (this.name != null)
                ? this.name.replace(" ", "%20")
                : "Usuario";

        return "https://api.dicebear.com/7.x/avataaars/png?seed="
                + safeName
                + "&backgroundColor=b6e3f4,c0aede,d1d4f9";
    }
}