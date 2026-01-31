package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "superhero_appearance")
public class SuperheroAppearanceEntity {

  @Id
  @Column(name = "superhero_id")
  @SuppressWarnings("NullAway.Init")
  private Integer superheroId;

  private @Nullable String gender;
  private @Nullable String race;

  @Column(name = "height_cm")
  private @Nullable Integer heightCm;

  @Column(name = "weight_kg")
  private @Nullable Integer weightKg;

  @Column(name = "eye_color")
  private @Nullable String eyeColor;

  @Column(name = "hair_color")
  private @Nullable String hairColor;

  @OneToOne
  @MapsId
  @JoinColumn(name = "superhero_id")
  @SuppressWarnings("NullAway.Init")
  private SuperheroEntity superhero;

  public SuperheroAppearanceEntity() {}

  public Integer getSuperheroId() {
    return superheroId;
  }

  public void setSuperheroId(Integer superheroId) {
    this.superheroId = superheroId;
  }

  public @Nullable String getGender() {
    return gender;
  }

  public void setGender(@Nullable String gender) {
    this.gender = gender;
  }

  public @Nullable String getRace() {
    return race;
  }

  public void setRace(@Nullable String race) {
    this.race = race;
  }

  public @Nullable Integer getHeightCm() {
    return heightCm;
  }

  public void setHeightCm(@Nullable Integer heightCm) {
    this.heightCm = heightCm;
  }

  public @Nullable Integer getWeightKg() {
    return weightKg;
  }

  public void setWeightKg(@Nullable Integer weightKg) {
    this.weightKg = weightKg;
  }

  public @Nullable String getEyeColor() {
    return eyeColor;
  }

  public void setEyeColor(@Nullable String eyeColor) {
    this.eyeColor = eyeColor;
  }

  public @Nullable String getHairColor() {
    return hairColor;
  }

  public void setHairColor(@Nullable String hairColor) {
    this.hairColor = hairColor;
  }

  public SuperheroEntity getSuperhero() {
    return superhero;
  }

  public void setSuperhero(SuperheroEntity superhero) {
    this.superhero = superhero;
  }
}
