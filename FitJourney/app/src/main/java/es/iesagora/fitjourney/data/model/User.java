package es.iesagora.fitjourney.data.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private int id_user;
    private String email;
    private String name;
    private String password;
    private String gender;
    private String goal;
    private Double height;
    private Double weight;
    private String birth_date;
    private String photo_url;
    private String role;
    private int active;
    private String register_date;

    public User(String idUser, String name, String email, String pass, String birthDate, String gender, String goal, int height, float weight) {
    }
}