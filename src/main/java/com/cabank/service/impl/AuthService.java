package com.cabank.service.impl;

import com.cabank.dto.request.*;
import com.cabank.dto.response.AuthResponse;
import com.cabank.dto.response.UserResponse;
import com.cabank.entity.Message;
import com.cabank.entity.Otp;
import com.cabank.entity.User;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.OtpRepository;
import com.cabank.repository.UserRepository;
import com.cabank.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final MessageService messageService;
    private final OtpRepository otpRepository;

    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public AuthResponse signUp(SignUpRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (req.getPhone() != null && userRepository.existsByPhone(req.getPhone())) {
            throw new BadRequestException("Phone number is already registered");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        user = userRepository.save(user);

        messageService.createMessage(
                user,
                "CaBank",
                "Welcome to CaBank, " + user.getName() + "! Your account has been created successfully.",
                "Welcome to CaBank!",
                Message.MessageType.NOTIFICATION
        );

        String accessToken = jwtUtils.generateTokenFromEmail(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    public AuthResponse signIn(SignInRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", req.getEmail()));

        String accessToken = jwtUtils.generateToken(auth);
        String refreshToken = jwtUtils.generateRefreshToken(req.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String email = jwtUtils.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email));

        String newAccessToken = jwtUtils.generateTokenFromEmail(email);
        String newRefreshToken = jwtUtils.generateRefreshToken(email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email));

        if (!passwordEncoder.matches(
                req.getCurrentPassword(),
                user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        messageService.createMessage(
                user,
                "CaBank Security",
                "Your password was changed successfully. If you did not make this change, contact support immediately.",
                "Your password was changed",
                Message.MessageType.ALERT
        );
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email));

        if (req.getName() != null) {
            user.setName(req.getName());
        }

        if (req.getPhone() != null) {
            if (userRepository.existsByPhone(req.getPhone())
                    && !req.getPhone().equals(user.getPhone())) {
                throw new BadRequestException("Phone number already in use");
            }
            user.setPhone(req.getPhone());
        }

        return toUserResponse(userRepository.save(user));
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email));

        return toUserResponse(user);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Forgot password / OTP flow
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Step 1: user submits their phone number.
     * Generates an OTP, stores it, and "sends" it via the in-app message system.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", req.getPhone()));

        String code = generateOtpCode();

        Otp otp = Otp.builder()
                .phone(req.getPhone())
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .purpose(Otp.OtpPurpose.PASSWORD_RESET)
                .verified(false)
                .used(false)
                .build();

        otpRepository.save(otp);

        messageService.createMessage(
                user,
                "CaBank",
                "Your password reset verification code is " + code
                        + ". It expires in " + OTP_EXPIRY_MINUTES + " minutes. Do not share this code with anyone.",
                "Your CaBank verification code",
                Message.MessageType.OTP
        );
    }

    /**
     * Step 2: user submits the code they received.
     * Marks the OTP as verified so step 3 (reset password) can proceed.
     */
    @Transactional
    public void verifyOtp(VerifyOtpRequest req) {
        Otp otp = otpRepository
                .findTopByPhoneAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        req.getPhone(), Otp.OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("No verification code found, please request a new one"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired, please request a new one");
        }

        if (!otp.getCode().equals(req.getCode())) {
            throw new BadRequestException("Invalid verification code");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
    }

    /**
     * Step 3: user submits new password.
     * Requires a previously verified, unused, unexpired OTP for this phone.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        Otp otp = otpRepository
                .findTopByPhoneAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        req.getPhone(), Otp.OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("Please verify your code before resetting password"));

        if (!otp.isVerified()) {
            throw new BadRequestException("Please verify your code before resetting password");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired, please request a new one");
        }

        User user = userRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", req.getPhone()));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);

        messageService.createMessage(
                user,
                "CaBank Security",
                "Your password has been reset successfully. If you did not perform this action, contact support immediately.",
                "Password reset successful",
                Message.MessageType.ALERT
        );
    }

    private String generateOtpCode() {
        return String.format("%05d", (int) (Math.random() * 100000));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}