package com.nilami.bidservice.services.bidServiceImplementations;

import java.math.BigDecimal;

import java.util.List;

import java.util.Optional;

import com.nilami.bidservice.controllers.requestTypes.BalanceRequest;
import com.nilami.bidservice.dto.ApiResponse;
import com.nilami.bidservice.dto.BidDTO;
import com.nilami.bidservice.dto.ItemDTO;
import com.nilami.bidservice.dto.UserDTO;
import com.nilami.bidservice.exceptions.InvalidBidException;
import com.nilami.bidservice.exceptions.PaymentException;
import com.nilami.bidservice.models.Bid;
import com.nilami.bidservice.repositories.BidRepository;
import com.nilami.bidservice.services.BidService;
import com.nilami.bidservice.services.externalClients.ItemClient;
import com.nilami.bidservice.services.externalClients.UserClient;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BidServiceImplementation implements BidService {

    private BidRepository bidRepository;

    private UserClient userClient;

    private ItemClient itemClient;

    @Override
    public Optional<BidDTO> getLastBid(String itemId) {

        Optional<Bid> lastBid = bidRepository.findTopByOrderByCreatedDesc();

        if (lastBid.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertToBidDTO(lastBid.get()));
    }

    @Override
    public BidDTO placeBid(String itemId, BigDecimal price, String userId, String idempotentKey) throws Exception {

        try {

            // check for last bid and that bid should not exceed previous bid
            Optional<BidDTO> lastBid = this.getLastBid(itemId);

            if (lastBid.isPresent()) {
                BigDecimal lastBidPrice = lastBid.get().getPrice();
                if (price.compareTo(lastBidPrice) <= 0) {
                    throw new InvalidBidException("Bid price must be higher than the last bid: " + lastBidPrice);
                }
            }

            // subtract bank balance from user
            BalanceRequest request = new BalanceRequest(userId, price);

            ApiResponse balanceSubtracted = userClient.subtractBankBalanceFromUser(request);

            if (!(Boolean) balanceSubtracted.getData()) {
                throw new PaymentException("Failed to subtract balance from user", userId, price);
            }

            // place bid
            Bid placedBidEntity = Bid.builder()
                    .itemId(itemId)
                    .price(price)
                    .creatorId(userId)
                    .build();

            Bid placedBid = bidRepository.save(placedBidEntity);

            return this.convertToBidDTO(placedBid);

            // catch statement, add back bank balance to user

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("ERROR: Bid failed to get placed" + e.getMessage());
            return null;
        }

    }

    @Override
    public String setIdempotentKey() {
        return null;
        // TODO: idempotent key
    }

    @Override
    public Boolean checkIfIdempotentKeyExists(String key) {
        return null;
        // TODO: check idempotent key
    }

    @Override
    public List<BidDTO> getBidsOfItems(String itemId) {

        throw new UnsupportedOperationException("Unimplemented method 'getBidsOfItems'");
    }

    public BidDTO convertToBidDTO(Bid bid) {

        String userId = bid.getCreatorId();

        String itemId = bid.getItemId();

        ApiResponse userResponse = userClient.getUserDetails(userId);

        UserDTO user = (UserDTO) userResponse.getData();

        String userName = user.getName();

        ItemDTO item = itemClient.getItem(itemId);

        String itemName = item.getTitle();

        return BidDTO.builder().id(bid.getId().toString())
                .itemId(itemId)
                .itemName(itemName)
                .created(bid.getCreated())
                .price(bid.getPrice())
                .creatorName(userName)
                .build();

    }

}
