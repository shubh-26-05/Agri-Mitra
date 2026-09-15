package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.AuthResponseDto;
import com.agm.agrimitra.dto.LoginRequestDto;
import com.agm.agrimitra.dto.RegisterAdminRequestDto;
import com.agm.agrimitra.dto.RegisterFarmerRequestDto;

public interface AuthService {

    AuthResponseDto registerFarmer(RegisterFarmerRequestDto requestDto);

    AuthResponseDto registerAdmin(RegisterAdminRequestDto requestDto);

    AuthResponseDto login(LoginRequestDto requestDto);
}
