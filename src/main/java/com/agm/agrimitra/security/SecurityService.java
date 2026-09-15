package com.agm.agrimitra.security;

import com.agm.agrimitra.entity.Role;
import com.agm.agrimitra.entity.User;
import com.agm.agrimitra.repository.CropRecommendationRepository;
import com.agm.agrimitra.repository.FertilizerUsageRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.repository.SoilDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final FieldRepository fieldRepository;
    private final SoilDataRepository soilDataRepository;
    private final CropRecommendationRepository cropRecommendationRepository;
    private final FertilizerUsageRepository fertilizerUsageRepository;

    /**
     * Resolves the currently authenticated User entity from the Spring SecurityContext.
     */
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User)) {
            return null;
        }
        return (User) auth.getPrincipal();
    }

    /**
     * Resolves the linked Farmer ID for the currently authenticated User.
     * Returns null if no user is authenticated or if the user has no Farmer profile (e.g. pure Admin).
     */
    public Long getAuthenticatedFarmerId() {
        User user = getAuthenticatedUser();
        if (user == null || user.getFarmer() == null) {
            return null;
        }
        return user.getFarmer().getId();
    }

    /**
     * Checks if the authenticated user is an ADMIN or owns the specified farmer profile.
     */
    public boolean isFarmer(Long farmerId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            return true;
        }
        return user.getFarmer() != null && user.getFarmer().getId().equals(farmerId);
    }

    /**
     * Checks if the authenticated user is an ADMIN or owns the specified field.
     */
    public boolean isFieldOwner(Long fieldId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            return true;
        }
        if (user.getFarmer() == null) {
            return false;
        }
        return fieldRepository.findById(fieldId)
                .map(field -> field.getFarmer().getId().equals(user.getFarmer().getId()))
                .orElse(false);
    }

    /**
     * Checks if the authenticated user is an ADMIN or owns the soil data record.
     */
    public boolean isSoilDataOwner(Long soilDataId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            return true;
        }
        if (user.getFarmer() == null) {
            return false;
        }
        return soilDataRepository.findById(soilDataId)
                .map(soil -> soil.getField().getFarmer().getId().equals(user.getFarmer().getId()))
                .orElse(false);
    }

    /**
     * Checks if the authenticated user is an ADMIN or owns the crop recommendation record.
     */
    public boolean isRecommendationOwner(Long recommendationId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            return true;
        }
        if (user.getFarmer() == null) {
            return false;
        }
        return cropRecommendationRepository.findById(recommendationId)
                .map(rec -> rec.getField().getFarmer().getId().equals(user.getFarmer().getId()))
                .orElse(false);
    }

    /**
     * Checks if the authenticated user is an ADMIN or owns the fertilizer usage record.
     */
    public boolean isFertilizerUsageOwner(Long fertilizerUsageId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            return true;
        }
        if (user.getFarmer() == null) {
            return false;
        }
        return fertilizerUsageRepository.findById(fertilizerUsageId)
                .map(usage -> usage.getField().getFarmer().getId().equals(user.getFarmer().getId()))
                .orElse(false);
    }
}
