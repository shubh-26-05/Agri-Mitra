package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.AuthResponseDto;
import com.agm.agrimitra.dto.FarmerResponseDto;
import com.agm.agrimitra.dto.LoginRequestDto;
import com.agm.agrimitra.dto.RegisterAdminRequestDto;
import com.agm.agrimitra.dto.RegisterFarmerRequestDto;
import com.agm.agrimitra.entity.Address;
import com.agm.agrimitra.entity.Farmer;
import com.agm.agrimitra.entity.Role;
import com.agm.agrimitra.entity.User;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.FarmerMapper;
import com.agm.agrimitra.repository.FarmerRepository;
import com.agm.agrimitra.repository.UserRepository;
import com.agm.agrimitra.security.JwtService;
import com.agm.agrimitra.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final FarmerMapper farmerMapper;

    @Override
    public AuthResponseDto registerFarmer(RegisterFarmerRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("User with email '" + requestDto.getEmail() + "' already exists");
        }
        if (farmerRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new IllegalArgumentException("Farmer with phone number '" + requestDto.getPhoneNumber() + "' already exists");
        }

        // 1. Create and persist User with FARMER role
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .roles(new HashSet<>(Set.of(Role.FARMER)))
                .build();
        User savedUser = userRepository.save(user);

        // 2. Create and persist linked Farmer profile
        Address address = null;
        if (requestDto.getLocation() != null) {
            address = Address.builder()
                    .village(requestDto.getLocation().getVillage())
                    .district(requestDto.getLocation().getDistrict())
                    .state(requestDto.getLocation().getState())
                    .build();
        }

        Farmer farmer = Farmer.builder()
                .user(savedUser)
                .name(requestDto.getName())
                .phoneNumber(requestDto.getPhoneNumber())
                .email(requestDto.getEmail())
                .location(address)
                .build();
        Farmer savedFarmer = farmerRepository.save(farmer);
        savedUser.setFarmer(savedFarmer);

        // 3. Generate JWT and map response
        String token = jwtService.generateToken(savedUser);
        FarmerResponseDto farmerProfile = farmerMapper.toResponseDto(savedFarmer);

        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .farmerProfile(farmerProfile)
                .build();
    }

    @Override
    public AuthResponseDto registerAdmin(RegisterAdminRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("User with email '" + requestDto.getEmail() + "' already exists");
        }

        // Create User with ADMIN role (no linked Farmer profile)
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .roles(new HashSet<>(Set.of(Role.ADMIN)))
                .build();
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .farmerProfile(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto requestDto) {
        // Authenticate credentials against UserDetailsService and PasswordEncoder
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                )
        );

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", requestDto.getEmail()));

        String token = jwtService.generateToken(user);
        FarmerResponseDto farmerProfile = user.getFarmer() != null
                ? farmerMapper.toResponseDto(user.getFarmer())
                : null;

        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .farmerProfile(farmerProfile)
                .build();
    }
}
