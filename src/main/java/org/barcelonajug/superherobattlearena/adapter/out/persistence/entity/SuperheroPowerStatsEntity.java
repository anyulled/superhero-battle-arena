package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "superhero_powerstats")
public class SuperheroPowerStatsEntity {

  @Id
  @Column(name = "superhero_id")
  @SuppressWarnings("NullAway.Init")
  private Integer superheroId;

  @SuppressWarnings("NullAway.Init")
  private Integer intelligence;

  @SuppressWarnings("NullAway.Init")
  private Integer strength;

  @SuppressWarnings("NullAway.Init")
  private Integer speed;

  @SuppressWarnings("NullAway.Init")
  private Integer durability;

  @SuppressWarnings("NullAway.Init")
  private Integer power;

  @SuppressWarnings("NullAway.Init")
  private Integer combat;

  @SuppressWarnings("NullAway.Init")
  private Integer cost;

  @SuppressWarnings("NullAway.Init")
  @OneToOne
  @MapsId
  @JoinColumn(name = "superhero_id")
  private SuperheroEntity superhero;

  public SuperheroPowerStatsEntity() {}

  public Integer getSuperheroId() {
    return superheroId;
  }

  public void setSuperheroId(Integer superheroId) {
    this.superheroId = superheroId;
  }

  public Integer getIntelligence() {
    return intelligence;
  }

  public void setIntelligence(Integer intelligence) {
    this.intelligence = intelligence;
  }

  public Integer getStrength() {
    return strength;
  }

  public void setStrength(Integer strength) {
    this.strength = strength;
  }

  public Integer getSpeed() {
    return speed;
  }

  public void setSpeed(Integer speed) {
    this.speed = speed;
  }

  public Integer getDurability() {
    return durability;
  }

  public void setDurability(Integer durability) {
    this.durability = durability;
  }

  public Integer getPower() {
    return power;
  }

  public void setPower(Integer power) {
    this.power = power;
  }

  public Integer getCombat() {
    return combat;
  }

  public void setCombat(Integer combat) {
    this.combat = combat;
  }

  public Integer getCost() {
    return cost;
  }

  public void setCost(Integer cost) {
    this.cost = cost;
  }

  public SuperheroEntity getSuperhero() {
    return superhero;
  }

  public void setSuperhero(SuperheroEntity superhero) {
    this.superhero = superhero;
  }
}
