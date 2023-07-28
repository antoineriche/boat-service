package com.ariche.boatapi.web.controllers;

import com.ariche.boatapi.security.JWTToken;
import com.ariche.boatapi.service.authentication.AuthenticationService;
import com.ariche.boatapi.service.authentication.CredentialsDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class AuthenticationControllerTest extends AbstractRestControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void test_AuthenticateUser() throws Exception {
        final CredentialsDTO credentials = new CredentialsDTO("login", "password");

        when(authenticationService.authenticateUser(any(CredentialsDTO.class)))
            .thenReturn(new JWTToken(
                "eyJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6IlJPTEVfQURNSU4iLCJzdWIiOiJhcmljaGUiLCJpYXQiOjE2OTAzNzAyMjAsImV4cCI6MTY5MDM3MDI4MH0.jjauzl_nkbxyveTbAoso2YXDkmV-G2RKndmCBWVIjmw",
                60_000L,
                "Bearer"));

        final MvcResult result = super.restMock.perform(post(getEndpoint() + "/token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(credentials)))
            .andReturn();

        final ArgumentCaptor<CredentialsDTO> captor = ArgumentCaptor.forClass(CredentialsDTO.class);
        verify(authenticationService)
            .authenticateUser(captor.capture());

        assertEquals("login", captor.getValue().login());
        assertEquals("password", captor.getValue().password());

        final JWTToken token = readMvcResultAs(result, JWTToken.class);
        assertEquals("eyJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6IlJPTEVfQURNSU4iLCJzdWIiOiJhcmljaGUiLCJpYXQiOjE2OTAzNzAyMjAsImV4cCI6MTY5MDM3MDI4MH0.jjauzl_nkbxyveTbAoso2YXDkmV-G2RKndmCBWVIjmw", token.accessToken());
        assertEquals(60_000L, token.expiredIn());
        assertEquals("Bearer", token.tokenType());
    }

    @Override
    public Object buildResource() {
        return new AuthenticationController(authenticationService);
    }

    @Override
    public String getEndpoint() {
        return "/auth";
    }
}
