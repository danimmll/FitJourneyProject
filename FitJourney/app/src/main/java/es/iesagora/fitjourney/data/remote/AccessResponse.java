package es.iesagora.fitjourney.data.remote;

import es.iesagora.fitjourney.data.model.User;
import lombok.Getter;
import lombok.Setter;

/**
 * Modelo que representa la respuesta recibida
 * tras un acceso o autenticación en la aplicación.
 * Contiene la información del usuario autenticado
 * y el token de acceso necesario para realizar
 * peticiones seguras a la API.
 */
@Setter
@Getter
public class AccessResponse {

    /**
     * Información del usuario autenticado.
     */
    private User user;

    /**
     * Token de autenticación generado por la API.
     * Se utiliza para identificar y autorizar
     * al usuario en futuras peticiones.
     */
    private String token;
}