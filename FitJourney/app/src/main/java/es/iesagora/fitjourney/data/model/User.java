package es.iesagora.fitjourney.data.model;

import java.time.LocalDate;
import java.util.UUID;

public class User {
    private final String idUser;
    private String name;
    private String email;
    private String password;
    private LocalDate birthDate;
    private String gender;
    private String goal;
    private int height;
    private float weight;


    public User(String name, String email, String password, LocalDate birthDate, String gender, String goal, int height, float weight) {
        this.idUser = UUID.randomUUID().toString();
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
