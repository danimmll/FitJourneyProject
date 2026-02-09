package es.iesagora.fitjourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Exercise implements Serializable {
    @SerializedName("id_exercise")
    private int id_exercise;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;

    @SerializedName("muscle_group")
    private String muscleGroup;

    @SerializedName("equipment")
    private String equipment;
    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("video_url")
    private String videoUrl;

    public int getId_exercise() {
        return id_exercise;
    }

    public void setId_exercise(int id_exercise) {
        this.id_exercise = id_exercise;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}