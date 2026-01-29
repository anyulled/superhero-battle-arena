package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;

@Entity
@Table(name = "superhero_biography")
public class SuperheroBiographyEntity {

    @Id
    @Column(name = "superhero_id")
    private Integer superheroId;

    @Column(name = "full_name")
    private String fullName;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> aliases;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "first_appearance")
    private String firstAppearance;

    @OneToOne
    @MapsId
    @JoinColumn(name = "superhero_id")
    private SuperheroEntity superhero;

    public SuperheroBiographyEntity() {
    }

    public Integer getSuperheroId() {
        return superheroId;
    }

    public void setSuperheroId(Integer superheroId) {
        this.superheroId = superheroId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getFirstAppearance() {
        return firstAppearance;
    }

    public void setFirstAppearance(String firstAppearance) {
        this.firstAppearance = firstAppearance;
    }

    public SuperheroEntity getSuperhero() {
        return superhero;
    }

    public void setSuperhero(SuperheroEntity superhero) {
        this.superhero = superhero;
    }
}
