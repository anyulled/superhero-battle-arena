package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "superhero_biography")
public class SuperheroBiographyEntity {

  @Id
  @Column(name = "superhero_id")
  @SuppressWarnings("NullAway.Init")
  private Integer superheroId;

  @Column(name = "full_name")
  private @Nullable String fullName;

  @JdbcTypeCode(SqlTypes.JSON)
  private @Nullable List<String> aliases;

  @Column(name = "place_of_birth")
  private @Nullable String placeOfBirth;

  @Column(name = "first_appearance")
  private @Nullable String firstAppearance;

  @OneToOne
  @MapsId
  @JoinColumn(name = "superhero_id")
  @SuppressWarnings("NullAway.Init")
  private SuperheroEntity superhero;

  public SuperheroBiographyEntity() {}

  public Integer getSuperheroId() {
    return superheroId;
  }

  public void setSuperheroId(Integer superheroId) {
    this.superheroId = superheroId;
  }

  public @Nullable String getFullName() {
    return fullName;
  }

  public void setFullName(@Nullable String fullName) {
    this.fullName = fullName;
  }

  public @Nullable List<String> getAliases() {
    return aliases;
  }

  public void setAliases(@Nullable List<String> aliases) {
    this.aliases = aliases;
  }

  public @Nullable String getPlaceOfBirth() {
    return placeOfBirth;
  }

  public void setPlaceOfBirth(@Nullable String placeOfBirth) {
    this.placeOfBirth = placeOfBirth;
  }

  public @Nullable String getFirstAppearance() {
    return firstAppearance;
  }

  public void setFirstAppearance(@Nullable String firstAppearance) {
    this.firstAppearance = firstAppearance;
  }

  public SuperheroEntity getSuperhero() {
    return superhero;
  }

  public void setSuperhero(SuperheroEntity superhero) {
    this.superhero = superhero;
  }
}
