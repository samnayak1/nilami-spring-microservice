package com.nilami.bidservice.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.nilami.bidservice.dto.GetBidsOfUserAlongWithHighestBidForItemResponseBody;
import com.nilami.bidservice.models.Bid;

import jakarta.persistence.LockModeType;


@Repository
public interface BidRepository extends JpaRepository<Bid,UUID>{

    Optional<Bid> findTopByItemIdOrderByCreatedDesc(@NonNull UUID itemId);

     @Lock(LockModeType.PESSIMISTIC_READ)
     @NonNull
     Optional<Bid> findById(@NonNull UUID id);

    List<Bid> findByItemIdOrderByCreatedDesc(UUID itemId);


    @Query(value="""
            SELECT 
            t.id as id,
            t.created_at as createdAt,
            t.item_id as itemId,
            t.creator_id as creatorId,
            t.price as price,
            CASE WHEN t.price = t.global_max THEN TRUE ElSE false END as isHighestBid
            FROM ( SELECT 
              b.id,
              b.created_at,
              b.item_id,
              b.creator_id,
              b.price,
              MAX(b.price) OVER(PARTITION BY b.item_id) as global_max
              FROM bids b
               ) t
             WHERE t.creator_id = :userId
            """, nativeQuery = true)
    List<GetBidsOfUserAlongWithHighestBidForItemResponseBody> getBidsOfUserAlongWithHighestBidForItemRepositoryQuery(UUID userId);


}
