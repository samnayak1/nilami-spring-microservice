package com.nilami.bidservice.exceptions;


public class BidLessThanItemException extends RuntimeException {
public BidLessThanItemException(String message) {
        super(message);
    }
}
