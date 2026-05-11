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
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    private ImageUtils() { /* utility class */ }

    /**
     * Carga una imagen en un ImageView detectando automáticamente si es Base64 o URL HTTP.
     * Centraliza la lógica que antes estaba duplicada en 3 sitios distintos.
     *
     * @param context   contexto de la aplicación
     * @param url       cadena con la URL o datos Base64 de la imagen
     * @param imageView vista donde se cargará la imagen
     * @param placeholder drawable de placeholder mientras carga
     */
    public static void loadInto(Context context, String url, ImageView imageView, int placeholder) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(placeholder);
            return;
        }

        if (url.length() > 200 || !url.startsWith("http")) {
            try {
                byte[] imageBytes = Base64.decode(url, Base64.DEFAULT);
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
        } else {
            Glide.with(context)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .centerCrop()
                    .into(imageView);
        }
    }

    /**
     * Convierte una imagen desde Uri a cadena Base64.
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

    /**
     * Redimensiona un Bitmap manteniendo su proporción original (máximo 800px).
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