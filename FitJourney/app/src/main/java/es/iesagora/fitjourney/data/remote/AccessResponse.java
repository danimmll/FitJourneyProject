package es.iesagora.fitjourney.data.remote;

import es.iesagora.fitjourney.data.model.User;

public class AccessResponse {
    private User user;
    private String token;

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}

