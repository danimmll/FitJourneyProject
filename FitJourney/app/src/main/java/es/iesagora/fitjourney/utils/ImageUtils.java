package es.iesagora.fitjourney.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Clase utilitaria para la conversión y carga de imágenes.
 * Centraliza toda la lógica de detección Base64, URL HTTP y URI de contenido
 * para evitar duplicación en adapters y fragments.
 *
 * CORRECCIONES:
 * - Se añade soporte para URIs content:// y file:// (imágenes de galería).
 * - Se añade soporte para Data URIs del tipo data:image/...;base64,...
 *   eliminando el prefijo antes de decodificar (antes se intentaba decodificar
 *   el prefijo completo, lo que producía un error silencioso).
 * - La lógica de detección ya no depende solo de longitud (heurística frágil).
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    private ImageUtils() { /* utility class */ }

    /**
     * Carga una imagen en un ImageView detectando automáticamente el tipo de fuente:
     * - URLs HTTP/HTTPS: Glide carga directamente
     * - URIs content:// o file:// (galería): Glide carga como Uri
     * - Data URIs (data:image/...;base64,...): se extrae el Base64 y se decodifica
     * - Base64 puro: se decodifica directamente
     *
     * @param context     contexto de la aplicación
     * @param url         cadena con la URL, URI o datos Base64/Data URI
     * @param imageView   vista donde se cargará la imagen
     * @param placeholder drawable de placeholder y error
     */
    public static void loadInto(Context context, String url, ImageView imageView, int placeholder) {
        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(placeholder);
            return;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            Glide.with(context)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        if (url.startsWith("content://") || url.startsWith("file://")) {
            Glide.with(context)
                    .load(Uri.parse(url))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        if (url.startsWith("data:")) {
            int commaIndex = url.indexOf(',');
            if (commaIndex != -1) {
                String base64Data = url.substring(commaIndex + 1);
                loadBase64(context, base64Data, imageView, placeholder);
            } else {
                imageView.setImageResource(placeholder);
            }
            return;
        }

        loadBase64(context, url, imageView, placeholder);
    }

    /**
     * Carga una imagen circular en un ImageView detectando automáticamente el tipo de fuente.
     *
     * @param context     contexto de la aplicación
     * @param url         cadena con la URL, URI o datos Base64/Data URI
     * @param imageView   vista donde se cargará la imagen
     * @param placeholder drawable de placeholder y error
     */
    public static void loadCircle(Context context, String url, ImageView imageView, int placeholder) {
        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(placeholder);
            return;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            Glide.with(context)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .circleCrop()
                    .into(imageView);
            return;
        }

        if (url.startsWith("content://") || url.startsWith("file://")) {
            Glide.with(context)
                    .load(Uri.parse(url))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .circleCrop()
                    .into(imageView);
            return;
        }

        if (url.startsWith("data:")) {
            int commaIndex = url.indexOf(',');
            if (commaIndex != -1) {
                String base64Data = url.substring(commaIndex + 1);
                loadBase64Circle(context, base64Data, imageView, placeholder);
            } else {
                imageView.setImageResource(placeholder);
            }
            return;
        }

        loadBase64Circle(context, url, imageView, placeholder);
    }

    /** Decodifica Base64 puro y lo carga centrado en el ImageView. */
    private static void loadBase64(Context context, String base64, ImageView imageView, int placeholder) {
        try {
            byte[] imageBytes = Base64.decode(base64.trim(), Base64.DEFAULT);
            Glide.with(context)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .centerCrop()
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error al decodificar Base64", e);
            imageView.setImageResource(placeholder);
        }
    }

    /** Decodifica Base64 puro y lo carga circular en el ImageView. */
    private static void loadBase64Circle(Context context, String base64, ImageView imageView, int placeholder) {
        try {
            byte[] imageBytes = Base64.decode(base64.trim(), Base64.DEFAULT);
            Glide.with(context)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .circleCrop()
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error al decodificar Base64 (círculo)", e);
            imageView.setImageResource(placeholder);
        }
    }

    /**
     * Convierte una imagen desde Uri a cadena Base64.
     * Redimensiona a un máximo de 800px manteniendo proporción
     * y comprime a JPEG con calidad del 70%.
     *
     * @param resolver ContentResolver para acceder al Uri
     * @param uri      Uri de la imagen
     * @return cadena Base64 o null si ocurre un error
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
            Log.e(TAG, "Error al convertir imagen a Base64", e);
            return null;
        }
    }

    /** Redimensiona un Bitmap manteniendo su proporción original (máximo 800px). */
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
     * Convierte una cadena Base64 en un Bitmap.
     *
     * @param base64Str cadena Base64
     * @return Bitmap decodificado o null si ocurre un error
     */
    public static Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir Base64 a Bitmap", e);
            return null;
        }
    }
}