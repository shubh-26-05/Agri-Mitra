package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.FertilizerUsageRequestDto;
import com.agm.agrimitra.dto.FertilizerUsageResponseDto;
import com.agm.agrimitra.entity.FertilizerUsage;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.Role;
import com.agm.agrimitra.entity.User;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.FertilizerUsageMapper;
import com.agm.agrimitra.repository.FertilizerUsageRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.repository.UserRepository;
import com.agm.agrimitra.service.FertilizerUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FertilizerUsageServiceImpl implements FertilizerUsageService {

    private final FertilizerUsageRepository fertilizerUsageRepository;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final FertilizerUsageMapper fertilizerUsageMapper;

    @Override
    public FertilizerUsageResponseDto createFertilizerUsage(Long fieldId, FertilizerUsageRequestDto dto, Long authenticatedUserId) {
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));

        checkFieldAccess(field, authenticatedUserId);

        FertilizerUsage fertilizerUsage = fertilizerUsageMapper.toEntity(dto, field);
        FertilizerUsage saved = fertilizerUsageRepository.save(fertilizerUsage);
        return fertilizerUsageMapper.toResponseDto(saved);
    }

    @Override
    public FertilizerUsageResponseDto createFertilizerUsage(Long fieldId, FertilizerUsageRequestDto dto) {
        return createFertilizerUsage(fieldId, dto, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FertilizerUsageResponseDto> getFertilizerUsageHistory(Long fieldId, Pageable pageable, Long authenticatedUserId) {
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));

        checkFieldAccess(field, authenticatedUserId);

        return fertilizerUsageRepository.findByFieldId(fieldId, pageable)
                .map(fertilizerUsageMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FertilizerUsageResponseDto> getFertilizerUsageByFieldId(Long fieldId, Pageable pageable) {
        return getFertilizerUsageHistory(fieldId, pageable, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FertilizerUsageResponseDto> getFertilizerUsageByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir) {
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "applicationDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));
        return getFertilizerUsageByFieldId(fieldId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FertilizerUsageResponseDto getFertilizerUsageById(Long id) {
        FertilizerUsage usage = fertilizerUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FertilizerUsage", "id", id));
        return fertilizerUsageMapper.toResponseDto(usage);
    }

    @Override
    public void deleteFertilizerUsage(Long id, Long authenticatedUserId) {
        FertilizerUsage usage = fertilizerUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FertilizerUsage", "id", id));

        checkFertilizerUsageAccess(usage, authenticatedUserId);

        fertilizerUsageRepository.delete(usage);
    }

    @Override
    public void deleteFertilizerUsage(Long id) {
        deleteFertilizerUsage(id, null);
    }

    private void checkFieldAccess(Field field, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            return;
        }
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authenticatedUserId));

        boolean isAdmin = user.getRoles() != null && user.getRoles().contains(Role.ADMIN);
        if (!isAdmin) {
            if (user.getFarmer() == null || field.getFarmer() == null ||
                    !field.getFarmer().getId().equals(user.getFarmer().getId())) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }
        }
    }

    private void checkFertilizerUsageAccess(FertilizerUsage usage, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            return;
        }
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authenticatedUserId));

        boolean isAdmin = user.getRoles() != null && user.getRoles().contains(Role.ADMIN);
        if (!isAdmin) {
            if (user.getFarmer() == null || usage.getField() == null || usage.getField().getFarmer() == null ||
                    !usage.getField().getFarmer().getId().equals(user.getFarmer().getId())) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }
        }
    }
}
