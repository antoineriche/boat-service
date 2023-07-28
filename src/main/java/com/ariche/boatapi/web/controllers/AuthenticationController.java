package com.ariche.boatapi.web.controllers;

import com.ariche.boatapi.security.JWTToken;
import com.ariche.boatapi.service.authentication.AuthenticationService;
import com.ariche.boatapi.service.authentication.CredentialsDTO;
import com.ariche.boatapi.web.errors.BoatAPIError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticateService;

    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Authenticate a user",
        description = "Generate an access token"
    )
    @PostMapping(
        path = "/token",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok",
        content = @Content(schema = @Schema(implementation = JWTToken.class)),
        headers = @Header(name = HttpHeaders.AUTHORIZATION, description = "Bearer token", schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad Request",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    public ResponseEntity<JWTToken> authenticateUser(@RequestBody @Valid CredentialsDTO credentials) {
        log.debug("[POST] authenticate user: {}", credentials.login());
        final JWTToken token = authenticateService.authenticateUser(credentials);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .body(token);
    }

}
