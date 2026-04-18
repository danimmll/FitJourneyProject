package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User implements Serializable {
    @SerializedName("id_user")
    private String idUser;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("birth_date")
    private String birthDate;

    @SerializedName("gender")
    private String gender;

    @SerializedName("goal")
    private String goal;

    @SerializedName("height")
    private int height;

    @SerializedName("weight")
    private float weight;

    @SerializedName("register_date")
    private String registerDate;

    @SerializedName("role")
    private String role;

    public User(String idUser, String name, String email, String password, String birthDate, String gender, String goal, int height, float weight) {
        this.idUser = idUser;
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.gender = gender;
        this.goal = goal;
        this.height = height;
        this.weight = weight;
    }
}

