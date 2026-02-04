package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

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

    public User() {
    }

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

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public String getRegisterDate() { return registerDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}