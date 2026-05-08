package es.iesagora.fitjourney.data.remote;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Modelo utilizado para enviar una petición
 * relacionada con favoritos a la API.
 * Contiene el identificador del ejercicio
 * que se quiere añadir o eliminar de favoritos.
 */
@Data
@AllArgsConstructor
public class FavoriteRequest {

    /**
     * Identificador del ejercicio favorito.
     */
    private int id_exercise;
}