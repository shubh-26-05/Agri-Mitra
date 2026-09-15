package com.agm.agrimitra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoilExtractionResult {

    private Double nitrogenLevel;
    private Double phosphorusLevel;
    private Double potassiumLevel;
    private Double phLevel;
    private Double electricalConductivity;
    private Double organicCarbon;
    private Double sulphurLevel;
    private Double zincLevel;
    private Double ironLevel;
    private Double copperLevel;
    private Double manganeseLevel;
    private Double boronLevel;
    private Boolean extractionSuccessful;
}
