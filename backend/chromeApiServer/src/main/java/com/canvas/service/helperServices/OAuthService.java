package com.canvas.service.helperServices;

import com.canvas.exceptions.CanvasAPIException;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Response;

/**
 * This class handles OAuth requests for generation, validation of access tokens
 */
public class OAuthService {

    private final CanvasClientService canvasClientService;
    //TODO: Add and fetch values from config
    private final String CLIENT_ID = "10000000000002";
    private final String CLIENT_SECRET = "xppoeCamrsQVsEdZi5g6ykNE1qEJort6Zdn8ojGTcTBzRO15fR0AEdeeCtqcLymI";
    public OAuthService(CanvasClientService canvasClientService){
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
            Response response = canvasClientService.fetchAccessTokenResponse(CLIENT_ID, CLIENT_SECRET, code, redirectUri);
            return parseAccessTokenResponse(response);

        } catch (Exception e) {
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
