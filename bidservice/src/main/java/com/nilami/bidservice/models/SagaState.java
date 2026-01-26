package com.nilami.bidservice.models;

public enum SagaState {
   STARTED,
   FUNDS_RESERVED,
   BID_PLACED,
   FUNDS_COMMITED,
   COMPLETED,
   
   
   REJECTED,
   FAILED_TECHNICAL
}
