package com.canvas.service.helperServices;

import com.canvas.exceptions.CanvasAPIException;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * This class handles OAuth requests for generation, validation of access tokens
 */
@Service
public class OAuthService {

    private final CanvasClientService canvasClientService;
    private String clientSecret;
    private String clientId;

    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);


    @Autowired
    public OAuthService(Environment env, CanvasClientService canvasClientService) {
        this.clientId = env.getProperty("canvas.clientId");
        this.clientSecret = env.getProperty("canvas.clientSecret");
        this.canvasClientService = canvasClientService;
    }

    /**
     * Fetch access token response by making call to canvas
     * @param code code passed as param to the API
     * @param redirectUri redirect URI registered with the client
     * @return response from the canvas server
     * @throws CanvasAPIException
     */
    public String fetchAccessTokenResponse(String code, String redirectUri) throws CanvasAPIException {
        try {
            Response response = canvasClientService.fetchAccessTokenResponse(clientId, clientSecret, redirectUri, code);
            return parseAccessTokenResponse(response);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new CanvasAPIException(e.getMessage());
        }
    }

    /**
     * parseAccessToken or throws an exception based on response
     * @param response Response object from oauth request
     * @return access token
     * @throws Exception
     */
    private String parseAccessTokenResponse(Response response) throws Exception {
        switch (response.code()) {
            case 401 -> throw new Exception("401 Unauthorized");
            case 200 -> {
                JsonNode node = CanvasClientService.parseResponseToJsonNode(response);
                return node.get("access_token").asText();
            }
            default ->
                // Unhandled status code no access token throw error
                    throw new Exception("Unhandled exception");
        }
    }
}
