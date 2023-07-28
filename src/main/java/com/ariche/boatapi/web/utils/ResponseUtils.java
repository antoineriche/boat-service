package com.ariche.boatapi.web.utils;

import org.springframework.http.ResponseEntity;

import java.util.Optional;

public final class ResponseUtils {

    /**
     * Build a response entity with status-code OK of the given optional is present,
     * NOT_FOUND if the optional is empty
     * @param optBody the optional body
     * @return a 200-Response if the optional is empty, 404-Response otherwise
     * @param <T> generic type of the body to be returned
     */
    public static <T> ResponseEntity<T> wrapOrNotFound(final Optional<T> optBody) {
        return optBody.map(t -> ResponseEntity.ok(optBody.get()))
            .orElse(ResponseEntity.notFound().build());
    }

    private ResponseUtils() {
    }
}
