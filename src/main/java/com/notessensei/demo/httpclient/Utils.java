package com.notessensei.demo.httpclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.function.Consumer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;

public class Utils {

    static Dotenv dotenv = Dotenv.load();

    private Utils() {
        // Utility class, static only
    }

    public static String BASE_URL() {
        return dotenv.get("BASEURL");
    }

    public static String QUERY_URL() {
        return dotenv.get("QUERY_URL");
    }

    public static Consumer<String> getLineConsumer() {
        return new Consumer<String>() {
            int count = 0;

            @Override
            public void accept(String t) {
                // SKip the first and last line
                if ("]".equals(t) || "[".equals(t)) {
                    return;
                }
                String actual = t.endsWith(",")
                        ? t.substring(0, t.length() - 1)
                        : t;
                JsonObject json =
                        JsonParser.parseString(actual).getAsJsonObject().get("@meta")
                                .getAsJsonObject();

                // This is where the action happens
                System.out.println(json.get("unid").getAsString());
                count++;
                System.out.println("Count: " + count);
            }
        };
    }

    public static SSLContext dontDoThisInProduction()
            throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        /* Sample code, don't use it in production */
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        /* Sample code, don't use it in production */
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

    public static String getToken(HttpClient client) {
        // Read credentials from .env file
        String username = dotenv.get("USERNAME");
        String password = dotenv.get("PASSWORD");

        try {
            // Create JSON payload
            String jsonPayload =
                    String.format("{\"password\":\"%s\",\"username\":\"%s\"}", password, username);

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Utils.BASE_URL() + "/api/v1/auth"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send HTTP request and get response
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            String token = jsonResponse.get("bearer").getAsString();
            return String.format("Bearer %s", token);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
