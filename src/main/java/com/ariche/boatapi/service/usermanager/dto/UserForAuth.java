package com.ariche.boatapi.service.usermanager.dto;

import java.util.Collections;
import java.util.List;

public record UserForAuth(String login, String password, List<String> authorities) {
    public UserForAuth(String login, String password, String authority) {
        this(login, password, Collections.singletonList(authority));
    }
}
