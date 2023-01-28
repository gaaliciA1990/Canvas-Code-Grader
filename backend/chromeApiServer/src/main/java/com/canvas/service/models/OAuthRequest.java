package com.canvas.service.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * OAuth request Java  POJO class to build OAuth request object to make call to canvas
 */
@Getter
@Setter
@Builder
public class OAuthRequest {
    String clientId;
    String clientSecret;
    String code;
    String redirect_uri;
    String grant_type;
}
