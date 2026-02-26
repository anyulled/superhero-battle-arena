package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import java.time.OffsetDateTime;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "superheroes")
public class SuperheroEntity {

  @Id
  @SuppressWarnings("NullAway.Init")
  private Integer id;

  @SuppressWarnings("NullAway.Init")
  @Column(nullable = false)
  private String name;

  @SuppressWarnings("NullAway.Init")
  @Column(nullable = false, unique = true)
  private String slug;

  @SuppressWarnings("NullAway.Init")
  private String alignment;

  @SuppressWarnings("NullAway.Init")
  private String publisher;

  @SuppressWarnings("NullAway.Init")
  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @SuppressWarnings("NullAway.Init")
  @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  private @Nullable SuperheroPowerStatsEntity powerStats;

  @SuppressWarnings("NullAway.Init")
  @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  private @Nullable SuperheroAppearanceEntity appearance;

  @SuppressWarnings("NullAway.Init")
  @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  private @Nullable SuperheroBiographyEntity biography;

  @SuppressWarnings("NullAway.Init")
  @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  private @Nullable SuperheroImagesEntity images;

  public SuperheroEntity() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getAlignment() {
    return alignment;
  }

  public void setAlignment(String alignment) {
    this.alignment = alignment;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public SuperheroPowerStatsEntity getPowerStats() {
    return powerStats;
  }

  public void setPowerStats(@Nullable SuperheroPowerStatsEntity powerStats) {
    this.powerStats = powerStats;
    if (powerStats != null) {
      powerStats.setSuperhero(this);
    }
  }

  public SuperheroAppearanceEntity getAppearance() {
    return appearance;
  }

  public void setAppearance(@Nullable SuperheroAppearanceEntity appearance) {
    this.appearance = appearance;
    if (appearance != null) {
      appearance.setSuperhero(this);
    }
  }

  public SuperheroBiographyEntity getBiography() {
    return biography;
  }

  public void setBiography(@Nullable SuperheroBiographyEntity biography) {
    this.biography = biography;
    if (biography != null) {
      biography.setSuperhero(this);
    }
  }

  public SuperheroImagesEntity getImages() {
    return images;
  }

  public void setImages(@Nullable SuperheroImagesEntity images) {
    this.images = images;
    if (images != null) {
      images.setSuperhero(this);
    }
  }
}
