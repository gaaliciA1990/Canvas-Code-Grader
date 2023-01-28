package com.canvas.service.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OAuthRequestTest {

    private OAuthRequest oAuthRequest;

    @BeforeEach
    public void before() {
        oAuthRequest = new OAuthRequest(
                "fooClientID",
                "fooClientSecret",
                "fooCode",
                "fooRedirect_URI",
                "fooGrantType"
        );
    }

    @Test
    public void testGetters() {
        Assertions.assertEquals("fooClientID", oAuthRequest.getClientId());
        Assertions.assertEquals("fooClientSecret", oAuthRequest.getClientSecret());
        Assertions.assertEquals("fooCode", oAuthRequest.getCode());
        Assertions.assertEquals("fooRedirect_URI", oAuthRequest.getRedirect_uri());
        Assertions.assertEquals("fooGrantType", oAuthRequest.getGrant_type());
    }

    @Test
    public void testSetters() {
        // Given
        oAuthRequest.setClientId("fooClientID");
        oAuthRequest.setClientSecret("fooClientSecret");
        oAuthRequest.setCode("fooCode");
        oAuthRequest.setRedirect_uri("fooRedirect_URI");
        oAuthRequest.setGrant_type("fooGrantType");

        // Then
        Assertions.assertEquals("fooClientID", oAuthRequest.getClientId());
        Assertions.assertEquals("fooClientSecret", oAuthRequest.getClientSecret());
        Assertions.assertEquals("fooCode", oAuthRequest.getCode());
        Assertions.assertEquals("fooRedirect_URI", oAuthRequest.getRedirect_uri());
        Assertions.assertEquals("fooGrantType", oAuthRequest.getGrant_type());
    }

    @Test
    public void testBuilder() {
        OAuthRequest oAuthRequestBuilder = new OAuthRequest.OAuthRequestBuilder()
                .clientId("fooClientID")
                .clientSecret("fooClientSecret")
                .code("fooCode")
                .redirect_uri("fooRedirect_URI")
                .grant_type("fooGrantType").build();

        //then
        Assertions.assertEquals("fooClientID", oAuthRequestBuilder.getClientId());
        Assertions.assertEquals("fooClientSecret", oAuthRequestBuilder.getClientSecret());
        Assertions.assertEquals("fooCode", oAuthRequestBuilder.getCode());
        Assertions.assertEquals("fooRedirect_URI", oAuthRequestBuilder.getRedirect_uri());
        Assertions.assertEquals("fooGrantType", oAuthRequestBuilder.getGrant_type());
    }
}

