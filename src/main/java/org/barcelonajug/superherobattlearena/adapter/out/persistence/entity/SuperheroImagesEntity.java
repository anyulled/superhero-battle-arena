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
@Table(name = "superhero_images")
public class SuperheroImagesEntity {

  @Id
  @Column(name = "superhero_id")
  @SuppressWarnings("NullAway.Init")
  private Integer superheroId;

  @Column(name = "xs_url")
  private @Nullable String xsUrl;

  @Column(name = "sm_url")
  private @Nullable String smUrl;

  @Column(name = "md_url")
  private @Nullable String mdUrl;

  @Column(name = "lg_url")
  private @Nullable String lgUrl;

  @OneToOne
  @MapsId
  @JoinColumn(name = "superhero_id")
  @SuppressWarnings("NullAway.Init")
  private SuperheroEntity superhero;

  public SuperheroImagesEntity() {}

  public Integer getSuperheroId() {
    return superheroId;
  }

  public void setSuperheroId(Integer superheroId) {
    this.superheroId = superheroId;
  }

  public @Nullable String getXsUrl() {
    return xsUrl;
  }

  public void setXsUrl(@Nullable String xsUrl) {
    this.xsUrl = xsUrl;
  }

  public @Nullable String getSmUrl() {
    return smUrl;
  }

  public void setSmUrl(@Nullable String smUrl) {
    this.smUrl = smUrl;
  }

  public @Nullable String getMdUrl() {
    return mdUrl;
  }

  public void setMdUrl(@Nullable String mdUrl) {
    this.mdUrl = mdUrl;
  }

  public @Nullable String getLgUrl() {
    return lgUrl;
  }

  public void setLgUrl(@Nullable String lgUrl) {
    this.lgUrl = lgUrl;
  }

  public SuperheroEntity getSuperhero() {
    return superhero;
  }

  public void setSuperhero(SuperheroEntity superhero) {
    this.superhero = superhero;
  }
}
