package es.iesagora.fitjourney.data.remote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Modelo utilizado para enviar las credenciales
 * de acceso del usuario a la API.
 * Se utiliza en procesos como login,
 * reactivación de cuenta o eliminación de cuenta.
 */
@Setter
@Getter
@AllArgsConstructor
public class LoginRequest {

    /**
     * Correo electrónico del usuario.
     */
    private String email;

    /**
     * Contraseña del usuario.
     */
    private String password;
}