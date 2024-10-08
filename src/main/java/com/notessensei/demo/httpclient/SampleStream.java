package com.notessensei.demo.httpclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;

public class SampleStream {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting SampleStream");
        SampleStream sampleStream = new SampleStream();
        sampleStream.run();
    }

    Consumer<String> dataSink = Utils.getLineConsumer();

    private void run() throws Exception {

        SSLContext sslContext = Utils.dontDoThisInProduction();
        // Create HTTP client as a loose canon
        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
        String token = Utils.getToken(client);

        runTheChunk(client, token, dataSink);

        System.out.println("done");
    }

    private void runTheChunk(HttpClient client, String token, Consumer<String> consumer) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Utils.BASE_URL() + Utils.QUERY_URL()))
                    .header("Authorization", token)
                    .GET()
                    .build();

            HttpResponse<Stream<String>> response =
                    client.send(request, HttpResponse.BodyHandlers.ofLines());

            response.body().forEach(consumer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
