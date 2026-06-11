package com.cabank.service.impl;

import com.cabank.dto.request.BeneficiaryRequest;
import com.cabank.dto.response.BeneficiaryResponse;
import com.cabank.entity.Beneficiary;
import com.cabank.entity.Message;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.BeneficiaryRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public List<BeneficiaryResponse> getBeneficiaries(String email) {
        User user = getUser(email);
        return beneficiaryRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BeneficiaryResponse addBeneficiary(String email, BeneficiaryRequest req) {
        User user = getUser(email);
        Beneficiary b = Beneficiary.builder()
                .name(req.getName())
                .accountNumber(req.getAccountNumber())
                .bankName(req.getBankName())
                .user(user)
                .build();

        Beneficiary saved = beneficiaryRepository.save(b);

        messageService.createMessage(
                user,
                "CaBank",
                saved.getName() + " (" + saved.getAccountNumber() + ", " + saved.getBankName()
                        + ") has been added to your beneficiaries.",
                "New beneficiary added",
                Message.MessageType.NOTIFICATION
        );

        return toResponse(saved);
    }

    @Transactional
    public void deleteBeneficiary(String id, String email) {
        Beneficiary b = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", id));
        beneficiaryRepository.delete(b);
    }

    private BeneficiaryResponse toResponse(Beneficiary b) {
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .accountNumber(b.getAccountNumber())
                .bankName(b.getBankName())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}