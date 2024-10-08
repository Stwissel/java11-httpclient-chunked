package com.notessensei.demo.httpclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.net.ssl.SSLContext;

public class SampleStream2 {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting SampleStream");
        SampleStream2 sampleStream = new SampleStream2();
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

            DocumentSubscriber subscriber = new DocumentSubscriber(consumer);

            CompletableFuture<Integer> response =
                    client.sendAsync(request, responseInfo -> subscriber)
                            .whenComplete(
                                    (r, t) -> System.out.println("Response: " + r.statusCode()))
                            .thenApply(HttpResponse::body);

            System.out.printf("Total response size %s, count: %s%n", response.get(),
                    subscriber.getCount());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
