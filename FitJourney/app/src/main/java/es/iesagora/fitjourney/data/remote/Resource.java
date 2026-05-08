package es.iesagora.fitjourney.data.remote;

/**
 * Clase genérica que representa el estado de una respuesta
 * en operaciones de red o base de datos.
 * Se utiliza para manejar de forma estructurada los estados:
 * carga, éxito y error, junto con los datos o mensajes asociados.
 *
 * @param <T> Tipo de dato contenido en la respuesta.
 */
public class Resource<T> {

    /**
     * Estados posibles de una operación.
     */
    public enum Status { SUCCESS, ERROR, LOADING }

    /**
     * Datos devueltos por la operación en caso de éxito.
     */
    public final T data;

    /**
     * Mensaje asociado a la respuesta (normalmente en caso de error).
     */
    public final String message;

    /**
     * Estado actual de la operación.
     */
    public final Status status;

    /**
     * Constructor privado para controlar la creación
     * de instancias mediante métodos estáticos.
     *
     * @param status  Estado de la operación.
     * @param data    Datos devueltos.
     * @param message Mensaje asociado.
     */
    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * Crea un Resource en estado de éxito.
     *
     * @param data Datos obtenidos correctamente.
     * @param <T>  Tipo de dato.
     * @return Resource con estado SUCCESS.
     */
    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    /**
     * Crea un Resource en estado de error.
     *
     * @param msg Mensaje de error.
     * @param <T> Tipo de dato.
     * @return Resource con estado ERROR.
     */
    public static <T> Resource<T> error(String msg) {
        return new Resource<>(Status.ERROR, null, msg);
    }

    /**
     * Crea un Resource en estado de carga.
     *
     * @param <T> Tipo de dato.
     * @return Resource con estado LOADING.
     */
    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }
}