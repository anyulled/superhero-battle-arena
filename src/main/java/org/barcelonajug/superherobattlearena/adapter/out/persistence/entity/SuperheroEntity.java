package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "superheroes")
public class SuperheroEntity {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String alignment;

    private String publisher;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private SuperheroPowerStatsEntity powerStats;

    @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private SuperheroAppearanceEntity appearance;

    @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private SuperheroBiographyEntity biography;

    @OneToOne(mappedBy = "superhero", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private SuperheroImagesEntity images;

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

    public void setPowerStats(SuperheroPowerStatsEntity powerStats) {
        this.powerStats = powerStats;
        if (powerStats != null) {
            powerStats.setSuperhero(this);
        }
    }

    public SuperheroAppearanceEntity getAppearance() {
        return appearance;
    }

    public void setAppearance(SuperheroAppearanceEntity appearance) {
        this.appearance = appearance;
        if (appearance != null) {
            appearance.setSuperhero(this);
        }
    }

    public SuperheroBiographyEntity getBiography() {
        return biography;
    }

    public void setBiography(SuperheroBiographyEntity biography) {
        this.biography = biography;
        if (biography != null) {
            biography.setSuperhero(this);
        }
    }

    public SuperheroImagesEntity getImages() {
        return images;
    }

    public void setImages(SuperheroImagesEntity images) {
        this.images = images;
        if (images != null) {
            images.setSuperhero(this);
        }
    }
}
