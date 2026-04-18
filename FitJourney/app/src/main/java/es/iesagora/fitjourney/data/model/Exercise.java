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
public class Exercise implements Serializable {
    @SerializedName("id_exercise")
    private int id;
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


}