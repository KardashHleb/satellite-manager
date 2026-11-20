package com.satellite.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.satellite.app.model.Satellite;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "satellite_state")
public class SatelliteState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "satellite_id", nullable = false, unique = true)
    @JsonIgnore
    private Satellite satellite;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public SatelliteState(Satellite satellite, boolean active) {
        this.satellite = satellite;
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
