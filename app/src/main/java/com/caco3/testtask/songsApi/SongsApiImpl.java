package com.caco3.testtask.songsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link SongsApi} implementation
 */

/* package */ final class SongsApiImpl implements SongsApi {
    private static final String SONGS_API_URL = "http://tomcat.kilograpp.com/songs/api/songs";

    /**
     * 40 seconds connection timeout
     */
    private static final int CONNECTION_TIMEOUT
            = (int) TimeUnit.SECONDS.toMillis(40);

    /**
     * Sends GET request to {@link SongsApiImpl#SONGS_API_URL}, receives response, parses it
     * and returns list of {@link Song} received
     * @return list of songs
     * @throws SongsApiException if network problems occurred or server returned invalid json
     */
    @Override
    public List<Song> getSongs() throws SongsApiException {
        HttpURLConnection connection = null;
        BufferedReader br = null;

        try {
            URL url = new URL(SONGS_API_URL);
            connection = (HttpURLConnection)url.openConnection();
            // prepare connection
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setDoOutput(true);

            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null){
                sb.append(line);
            }

            return processSongsJsonResponse(sb.toString());
        } catch (IOException e){
            throw new SongsApiException("Unable to get response from server", e);
        } catch (JSONException e){
            throw new SongsApiException("Unable to parse response", e);
        }finally {
            if (connection != null){
                connection.disconnect();
            }

            if (br != null){
                try {
                    br.close();
                } catch (IOException e){
                    System.err.println("Unable to close br while retrieving response from the server");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Parses response from the server and returns list of {@link Song}s
     * @param response to parse to
     * @return list of {@link Song}
     * @throws JSONException provided string is invalid json
     */
    private static List<Song> processSongsJsonResponse(String response) throws JSONException{
        JSONArray songsJsonArray = new JSONArray(response);
        List<Song> result = new ArrayList<>();

        int length = songsJsonArray.length();
        for(int i = 0; i < length; i++){
            JSONObject songJson = songsJsonArray.getJSONObject(i);
            result.add(SongImpl.fromJson(songJson));
        }

        return result;
    }
}
