package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "superhero_images")
public class SuperheroImagesEntity {

    @Id
    @Column(name = "superhero_id")
    private Integer superheroId;

    @Column(name = "xs_url")
    private String xsUrl;

    @Column(name = "sm_url")
    private String smUrl;

    @Column(name = "md_url")
    private String mdUrl;

    @Column(name = "lg_url")
    private String lgUrl;

    @OneToOne
    @MapsId
    @JoinColumn(name = "superhero_id")
    private SuperheroEntity superhero;

    public SuperheroImagesEntity() {
    }

    public Integer getSuperheroId() {
        return superheroId;
    }

    public void setSuperheroId(Integer superheroId) {
        this.superheroId = superheroId;
    }

    public String getXsUrl() {
        return xsUrl;
    }

    public void setXsUrl(String xsUrl) {
        this.xsUrl = xsUrl;
    }

    public String getSmUrl() {
        return smUrl;
    }

    public void setSmUrl(String smUrl) {
        this.smUrl = smUrl;
    }

    public String getMdUrl() {
        return mdUrl;
    }

    public void setMdUrl(String mdUrl) {
        this.mdUrl = mdUrl;
    }

    public String getLgUrl() {
        return lgUrl;
    }

    public void setLgUrl(String lgUrl) {
        this.lgUrl = lgUrl;
    }

    public SuperheroEntity getSuperhero() {
        return superhero;
    }

    public void setSuperhero(SuperheroEntity superhero) {
        this.superhero = superhero;
    }
}
