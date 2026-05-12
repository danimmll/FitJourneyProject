package es.iesagora.fitjourney.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.AccessResponse;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.data.repositories.UserRepository;
import lombok.Getter;

/**
 * ViewModel principal encargado de la gestión de autenticación
 * y datos del usuario en la aplicación.
 * Coordina las operaciones del UserRepository y almacena el
 * estado en LiveData para que la UI pueda observar cambios
 * como login, registro y perfil de usuario.
 */
public class MainViewModel extends AndroidViewModel {

    /**
     * Repositorio de usuarios para operaciones de red.
     */
    private final UserRepository repository;

    /**
     * Repositorio de preferencias locales (token, tema, etc.).
     */
    private final PreferencesRepository preferencesRepository;

    /**
     * Estado del login del usuario.
     */
    @Getter
    private final MutableLiveData<Resource<AccessResponse>> loginState =
            new MutableLiveData<>();

    /**
     * Estado del registro del usuario.
     */
    @Getter
    private final MutableLiveData<Resource<AccessResponse>> registerState =
            new MutableLiveData<>();

    /**
     * Estado del perfil del usuario.
     */
    @Getter
    private final MutableLiveData<Resource<User>> userProfileState =
            new MutableLiveData<>();

    /**
     * Constructor del ViewModel.
     *
     * @param application Aplicación.
     */
    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application);
        this.preferencesRepository = new PreferencesRepository(application);
    }

    /**
     * Inicia sesión del usuario.
     *
     * @param email Email del usuario.
     * @param password Contraseña.
     */
    public void login(String email, String password) {

        loginState.postValue(Resource.loading());

        repository.login(email, password, result -> {

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                saveSession(result.data);
            }

            loginState.postValue(result);
        });
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param user Datos del usuario.
     */
    public void register(User user) {

        registerState.postValue(Resource.loading());

        repository.registerUser(user, result -> {

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                saveSession(result.data);
            }

            registerState.postValue(result);
        });
    }

    /**
     * Reactiva una cuenta de usuario.
     *
     * @param email Email.
     * @param password Contraseña.
     */
    public void reactivate(String email, String password) {

        registerState.postValue(Resource.loading());

        repository.reactivateAccount(email, password, result -> {

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                saveSession(result.data);
            }

            registerState.postValue(result);
        });
    }

    /**
     * Elimina físicamente la cuenta del usuario.
     *
     * @param email Email.
     * @param password Contraseña.
     */
    public void deletePhysical(String email, String password) {

        registerState.postValue(Resource.loading());

        repository.deleteAccountPhysical(email, password, result -> {

            if (result.status == Resource.Status.SUCCESS) {
                registerState.postValue(
                        Resource.error("Cuenta eliminada definitivamente. Ya puedes registrarte de nuevo.")
                );
            } else {
                registerState.postValue(
                        Resource.error("Error al borrar: " + result.message)
                );
            }
        });
    }

    /**
     * Obtiene el perfil del usuario.
     *
     * @param token Token de autenticación.
     */
    public void getProfile(String token) {

        repository.getUser(token, result -> {

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                userProfileState.postValue(
                        Resource.success(result.data.getUser())
                );

            } else if (result.status == Resource.Status.ERROR) {
                userProfileState.postValue(Resource.error(result.message));

            } else {
                userProfileState.postValue(Resource.loading());
            }
        });
    }

    /**
     * Guarda la sesión del usuario en preferencias.
     *
     * @param data Respuesta de autenticación.
     */
    private void saveSession(AccessResponse data) {

        if (data.getToken() != null) {
            preferencesRepository.setToken(data.getToken());
        }

        if (data.getUser() != null) {
            preferencesRepository.setRole(data.getUser().getRole());
            preferencesRepository.setUserId(
                    String.valueOf(data.getUser().getIdUser())
            );
        }
    }

    /**
     * Obtiene el token guardado.
     */
    public String getToken() {
        return preferencesRepository.getToken();
    }

    /**
     * Indica si hay un usuario logueado.
     */
    public boolean isUserLoggedIn() {
        return preferencesRepository.getToken() != null;
    }

    /**
     * Obtiene el rol del usuario.
     */
    public String getRole() {
        return preferencesRepository.getRole();
    }

    /**
     * Cambia el tema de la aplicación.
     *
     * @param mode Modo de tema.
     */
    public void setTheme(int mode) {
        preferencesRepository.setTheme(mode);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /**
     * Obtiene el tema actual.
     */
    public int getTheme() {
        return preferencesRepository.getTheme();
    }

    /**
     * Activa o desactiva notificaciones.
     */
    public void setNotifications(boolean enabled) {
        preferencesRepository.setNotifications(enabled);
    }

    /**
     * Obtiene el estado de notificaciones.
     */
    public boolean getNotifications() {
        return preferencesRepository.getNotifications();
    }
}