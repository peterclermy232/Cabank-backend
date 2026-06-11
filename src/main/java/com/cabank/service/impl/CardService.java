package com.cabank.service.impl;

import com.cabank.dto.request.AddCardRequest;
import com.cabank.dto.response.CardResponse;
import com.cabank.entity.Card;
import com.cabank.entity.Message;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.CardRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public List<CardResponse> getCards(String email) {
        User user = getUser(email);

        return cardRepository.findByUserIdAndActiveTrue(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardResponse addCard(String email, AddCardRequest req) {

        User user = getUser(email);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");

        YearMonth validFromYm =
                YearMonth.parse(req.getValidFrom(), formatter);

        YearMonth goodThruYm =
                YearMonth.parse(req.getGoodThru(), formatter);

        String randomLast4 =
                String.format("%04d", (int) (Math.random() * 10000));

        Card card = Card.builder()
                .cardNumber("4756 •••• •••• " + randomLast4)
                .last4(randomLast4)
                .holderName(req.getHolderName())
                .brand(req.getBrand())
                .cardType(req.getCardType())
                .validFrom(validFromYm.atDay(1))
                .goodThru(goodThruYm.atEndOfMonth())
                .balance(BigDecimal.ZERO)
                .creditLimit(BigDecimal.valueOf(100000))
                .cvv(String.format("%03d", (int) (Math.random() * 1000)))
                .color(Card.CardColor.PRIMARY)
                .active(true)
                .user(user)
                .build();

        cardRepository.save(card);

        messageService.createMessage(
                user,
                "CaBank",
                "Your new " + card.getBrand() + " " + card.getCardType()
                        + " card ending in " + card.getLast4() + " has been added to your account.",
                "New card added",
                Message.MessageType.NOTIFICATION
        );

        return toResponse(card);
    }

    @Transactional
    public void deleteCard(String cardId, String email) {

        User user = getUser(email);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Card",
                                "id",
                                cardId
                        ));

        card.setActive(false);

        cardRepository.save(card);

        messageService.createMessage(
                user,
                "CaBank",
                "Your card ending in " + card.getLast4() + " has been removed from your account.",
                "Card removed",
                Message.MessageType.NOTIFICATION
        );
    }

    public CardResponse getCard(String cardId, String email) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Card",
                                "id",
                                cardId
                        ));

        return toResponse(card);
    }

    private CardResponse toResponse(Card card) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MM/yy");

        return CardResponse.builder()
                .id(card.getId())
                .last4(card.getLast4())
                .holderName(card.getHolderName())
                .brand(card.getBrand())
                .cardType(card.getCardType())
                .balance(card.getBalance())
                .creditLimit(card.getCreditLimit())
                .validFrom(card.getValidFrom().format(formatter))
                .goodThru(card.getGoodThru().format(formatter))
                .color(card.getColor().name())
                .active(card.isActive())
                .build();
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "email",
                                email
                        ));
    }
}