package es.iesagora.fitjourney.data.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa a un usuario de la aplicación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private int id_user;
    private String email;
    private String name;
    private String password;
    private String gender;
    private String goal;
    private Double height;
    private Double weight;
    private String birth_date;
    private String photo_url;
    private String role;
    private int active;
    private String register_date;

    /**
     * Constructor personalizado — CORREGIDO: antes el cuerpo estaba vacío
     * y todos los campos llegaban a null al servidor.
     */
    public User(String idUser, String name, String email, String pass,
                String birthDate, String gender, String goal,
                int height, float weight) {
        this.name       = name;
        this.email      = email;
        this.password   = pass;
        this.birth_date = birthDate;
        this.gender     = gender;
        this.goal       = goal;
        this.height     = (double) height;
        this.weight     = (double) weight;
    }

    /**
     * Devuelve la imagen de perfil que se mostrará en pantalla.
     * Si el usuario tiene una foto personalizada, se devuelve esa.
     * En caso contrario, se genera automáticamente un avatar con DiceBear.
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