package es.iesagora.fitjourney.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.LoginResponse;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.PreferencesRepository;
import es.iesagora.fitjourney.data.repositories.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final PreferencesRepository preferencesRepository;

    private final MutableLiveData<Resource<LoginResponse>> loginState = new MutableLiveData<>();
    private final MutableLiveData<Resource<LoginResponse>> registerState = new MutableLiveData<>();
    private final MutableLiveData<Resource<User>> userProfileState = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application);
        this.preferencesRepository = new PreferencesRepository(application);
    }

    public MutableLiveData<Resource<LoginResponse>> getLoginState() {
        return loginState;
    }

    public MutableLiveData<Resource<LoginResponse>> getRegisterState() {
        return registerState;
    }

    public MutableLiveData<Resource<User>> getUserProfileState() {
        return userProfileState;
    }

    public void login(String email, String password) {
        loginState.postValue(Resource.loading());

        repository.login(email, password, result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                saveSession(result.data);
            }
            loginState.postValue(result);
        });
    }

    public void register(User user) {
        registerState.postValue(Resource.loading());

        repository.registerUser(user, result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                saveSession(result.data);
            }
            registerState.postValue(result);
        });
    }

    public void fetchUser(String token) {
        repository.getUser(token, result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                userProfileState.postValue(Resource.success(result.data.getUser()));
            } else if (result.status == Resource.Status.ERROR) {
                userProfileState.postValue(Resource.error(result.message));
            } else {
                userProfileState.postValue(Resource.loading());
            }
        });
    }

    private void saveSession(LoginResponse data) {
        if (data.getToken() != null) {
            preferencesRepository.setToken(data.getToken());
        }
        if (data.getUser() != null) {
            preferencesRepository.setRole(data.getUser().getRole());
            preferencesRepository.setUserId(data.getUser().getIdUser());
        }
    }

    public String getToken() {
        return preferencesRepository.getToken();
    }
    public void logout() {
        preferencesRepository.clearSession();
    }

    public boolean isUserLoggedIn() {
        return preferencesRepository.getToken() != null;
    }

    public String getRole() {
        return preferencesRepository.getRole();
    }

    public void setTheme(int mode) {
        preferencesRepository.setTheme(mode);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public int getTheme() {
        return preferencesRepository.getTheme();
    }

    public void setNotifications(boolean enabled) {
        preferencesRepository.setNotifications(enabled);
    }
    public boolean getNotifications() {
        return preferencesRepository.getNotifications();
    }
}