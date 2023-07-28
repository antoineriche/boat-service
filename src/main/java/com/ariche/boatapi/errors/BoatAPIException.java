package com.ariche.boatapi.errors;

import lombok.Getter;

@Getter
public class BoatAPIException extends RuntimeException {
    private final EBoatAPIError error;

    public BoatAPIException(EBoatAPIError error, String message) {
        super(message);
        this.error = error;
    }
}
