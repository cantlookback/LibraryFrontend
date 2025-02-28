package com.libraryfront.rcp.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";

    public static String get(String endpoint) throws IOException {
        URL url;
		try {
			url = new URI(BASE_URL + endpoint).toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException("MalformedURLException error: " + e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IOException("URISyntaxException error: " + e);
		}
		
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("HTTP error: " + responseCode);
        }
    }
    
   
    
    public static String post(String endpoint, String jsonBody) throws IOException {
    	URL url;
		try {
			url = new URI(BASE_URL + endpoint).toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException("MalformedURLException error: " + e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IOException("URISyntaxException error: " + e);
		}
		
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return response.toString();
        } else {
        	connection.disconnect();
            throw new IOException("HTTP error: " + responseCode);
            
        }
    }
    
    public static String put(String endpoint, String jsonBody) throws IOException {
    	URL url;
		try {
			url = new URI(BASE_URL + endpoint).toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException("MalformedURLException error: " + e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IOException("URISyntaxException error: " + e);
		}
		
    	System.out.println(jsonBody);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("HTTP error: " + responseCode);
        }
    }
    
    public static String delete(String endpoint, Long id) throws IOException {
    	URL url;
		try {
			url = new URI(BASE_URL + endpoint + "/" + id).toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException("MalformedURLException error: " + e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IOException("URISyntaxException error: " + e);
		}
		
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(false);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return "Успешно удалено";
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                throw new IOException("HTTP error: " + responseCode + ", Response: " + response.toString());
            }
        }
    }
    
    
}