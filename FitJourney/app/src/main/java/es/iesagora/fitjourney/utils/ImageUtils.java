package es.iesagora.fitjourney.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Clase utilitaria para la conversión y manipulación de imágenes.
 * Proporciona métodos para:
 * - Convertir imágenes desde Uri a Base64
 * - Reducir el tamaño de imágenes manteniendo proporción
 * - Convertir cadenas Base64 de vuelta a Bitmap
 * Esta clase se utiliza principalmente para preparar imágenes
 * antes de ser enviadas o almacenadas, optimizando su tamaño
 * y formato.
 */
public class ImageUtils {

    /**
     * Convierte una imagen localizada en un Uri a una cadena Base64.
     * El proceso incluye:
     * - Lectura de la imagen desde el ContentResolver
     * - Decodificación a Bitmap
     * - Redimensionado para optimizar tamaño
     * - Compresión en formato JPEG con calidad del 70%
     * - Codificación final en Base64
     * Este método es útil para enviar imágenes a servidores o APIs.
     *
     * @param resolver ContentResolver para acceder al Uri
     * @param uri Uri de la imagen a convertir
     * @return cadena Base64 de la imagen o null si ocurre un error
     */
    public static String uriToBase64(ContentResolver resolver, Uri uri) {
        try {
            InputStream inputStream = resolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            bitmap = getResizedBitmap(bitmap);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Redimensiona un Bitmap manteniendo su proporción original.
     * El tamaño máximo se ajusta al valor indicado, escalando
     * el otro lado proporcionalmente para evitar deformaciones.
     *
     * @param image imagen original
     * @return bitmap redimensionado
     */
    private static Bitmap getResizedBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = 800;
            height = (int) (width / bitmapRatio);
        } else {
            height = 800;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * Convierte una cadena Base64 en un objeto Bitmap.
     * Útil para reconstruir imágenes almacenadas o recibidas desde API.
     *
     * @param base64Str cadena Base64 de la imagen
     * @return Bitmap decodificado o null si ocurre un error
     */
    public static Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}