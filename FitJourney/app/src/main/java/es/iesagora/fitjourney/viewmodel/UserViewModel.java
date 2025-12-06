package es.iesagora.fitjourney.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import es.iesagora.fitjourney.data.model.User;
import es.iesagora.fitjourney.data.remote.LoginResponse;
import es.iesagora.fitjourney.data.remote.Resource;
import es.iesagora.fitjourney.data.repositories.UserRepository;

public class UserViewModel extends ViewModel {

    private final UserRepository repository;

    private final MutableLiveData<Resource<LoginResponse>> loginState = new MutableLiveData<>();
    private final MutableLiveData<Resource<LoginResponse>> registerState = new MutableLiveData<>();

    public UserViewModel() {
        this.repository = new UserRepository();
    }

    public MutableLiveData<Resource<LoginResponse>> getLoginState() {
        return loginState;
    }

    public MutableLiveData<Resource<LoginResponse>> getRegisterState() {
        return registerState;
    }

    public void register(User user) {
        repository.registerUser(user, new UserRepository.RegisterCallback() {
            @Override
            public void onResult(Resource<LoginResponse> result) {
                registerState.postValue(result);
            }
        });
    }

    public void login(String email, String password) {
        repository.login(email, password, new UserRepository.LoginCallBack() {
            @Override
            public void onResult(Resource<LoginResponse> result) {
                loginState.postValue(result);
            }
        });
    }

    public void fetchUser(String token) {
        repository.getUser(token, new UserRepository.LoginCallBack() {
            @Override
            public void onResult(Resource<LoginResponse> result) {
                loginState.postValue(result);
            }
        });
    }

}
