package com.agm.agrimitra.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "soil_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoilData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    @JsonBackReference
    private Field field;

    // Macro-nutrients (Primary)
    @Column(name = "nitrogen_level")
    private Double nitrogenLevel;

    @Column(name = "phosphorus_level")
    private Double phosphorusLevel;

    @Column(name = "potassium_level")
    private Double potassiumLevel;

    // Physical / Chemical Parameters
    @Column(name = "ph_level")
    private Double phLevel;

    @Column(name = "electrical_conductivity")
    private Double electricalConductivity;

    @Column(name = "organic_carbon")
    private Double organicCarbon;

    @Column(name = "moisture_level")
    private Double moistureLevel;

    // Secondary Nutrient
    @Column(name = "sulphur_level")
    private Double sulphurLevel;

    // Micro-nutrients
    @Column(name = "zinc_level")
    private Double zincLevel;

    @Column(name = "iron_level")
    private Double ironLevel;

    @Column(name = "copper_level")
    private Double copperLevel;

    @Column(name = "manganese_level")
    private Double manganeseLevel;

    @Column(name = "boron_level")
    private Double boronLevel;

    // Soil test date
    @Column(name = "tested_date")
    private LocalDate testedDate;

    // Image & source metadata
    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source")
    private SoilDataSource dataSource;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
