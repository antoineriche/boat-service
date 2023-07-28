package com.ariche.boatapi.config;

import com.ariche.boatapi.errors.EBoatAPIError;
import com.ariche.boatapi.web.errors.BoatAPIError;
import com.ariche.boatapi.web.errors.ErrorCodeGenerator;
import com.ariche.boatapi.web.errors.ProblemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyAuthenticationEntrypoint implements AuthenticationEntryPoint {

    private final ErrorCodeGenerator codeGenerator;

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                         AuthenticationException exception) {

        log.debug("Try to '[{}] {}' with un-authenticated remote.",
            httpServletRequest.getMethod(), httpServletRequest.getRequestURI());

        try (final ServletServerHttpResponse res = new ServletServerHttpResponse(httpServletResponse)) {
            final BoatAPIError error = new BoatAPIError(codeGenerator.generateErrorCode(), EBoatAPIError.UNAUTHORIZED_ERROR.getCategory(), Collections.singletonList(
                new ProblemDTO("You must be authenticated")));
            res.setStatusCode(HttpStatus.UNAUTHORIZED);
            res.getServletResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            res.getBody().write(new ObjectMapper().writeValueAsBytes(error));
        } catch (IOException e) {
            log.error("IOException while trying to write body to response.");
        }
    }

}
