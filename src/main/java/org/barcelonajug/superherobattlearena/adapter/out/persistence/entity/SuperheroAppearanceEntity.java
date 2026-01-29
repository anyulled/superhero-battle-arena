package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "superhero_appearance")
public class SuperheroAppearanceEntity {

    @Id
    @Column(name = "superhero_id")
    private Integer superheroId;

    private String gender;
    private String race;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "eye_color")
    private String eyeColor;

    @Column(name = "hair_color")
    private String hairColor;

    @OneToOne
    @MapsId
    @JoinColumn(name = "superhero_id")
    private SuperheroEntity superhero;

    public SuperheroAppearanceEntity() {
    }

    public Integer getSuperheroId() {
        return superheroId;
    }

    public void setSuperheroId(Integer superheroId) {
        this.superheroId = superheroId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public Integer getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Integer weightKg) {
        this.weightKg = weightKg;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public SuperheroEntity getSuperhero() {
        return superhero;
    }

    public void setSuperhero(SuperheroEntity superhero) {
        this.superhero = superhero;
    }
}
