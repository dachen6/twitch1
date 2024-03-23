package com.laioffer.jupiter.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.ResponseHandler;
import org.springframework.stereotype.Service;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.response.Game;
import java.util.Arrays;
import java.util.List;
@Service
public class GameService {
    private static final String TOKEN = "Bearer 9btlgr6v4hglqbebj9vws3pujiqv5v";
    private static final String CLIENT_ID = "qzddm7kv8zqjo7ucrpkdqup26s8m59";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;


    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            return String.format(url, limit);
        } else {
            try {
                // Encode special characters in URL, e.g. Rick Sun -> Rick%20Sun
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }
    // Similar to buildGameURL, build Search URL that will be used when calling Twitch API. e.g. https://api.twitch.tv/helix/clips?game_id=12924.
    private String buildSearchURL(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }

    // Send HTTP request to Twitch Backend based on the given URL, and returns the body of the HTTP response returned from Twitch backend.
    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // Define the response handler to parse and return HTTP response body returned from Twitch
        ResponseHandler<String> responseHandler = response -> {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new TwitchException("Get null result from Twitch API");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();
        };

        try {
            // Define the HTTP request, TOKEN and CLIENT_ID are used for user authentication on Twitch backend
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);
            return httpclient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Convert JSON format data returned from Twitch to an Arraylist of Game objects
    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    // Integrate search() and getGameList() together, returns the top x popular games from Twitch.
    public List<Game> topGames(int limit) throws TwitchException {
        if (limit == 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        String url = buildGameURL(TOP_GAME_URL, "", limit);
        String data = searchTwitch(url);
        return getGameList(data);
    }

    // Integrate search() and getGameList() together, returns the dedicated game based on the game name.
    public Game searchGame(String gameName) throws TwitchException {
        String url = buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0);
        String data = searchTwitch(url);
        List<Game> gameList = getGameList(data);
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }



}

