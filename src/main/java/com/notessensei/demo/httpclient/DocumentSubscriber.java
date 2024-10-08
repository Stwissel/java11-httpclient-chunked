package com.notessensei.demo.httpclient;

import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class DocumentSubscriber implements BodySubscriber<Integer> {

    public DocumentSubscriber(Consumer<String> dataSink) {
        this.dataSink = dataSink;
    }

    final Consumer<String> dataSink;
    final CompletableFuture<Integer> result = new CompletableFuture<>();
    Flow.Subscription subscription;
    int totalsize = 0;
    String current = "";
    int count = 0;

    public int getCount() {
        return count;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);

    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
        int size = buffers.stream().mapToInt(ByteBuffer::remaining).sum();
        byte[] bytes = new byte[size];
        int offset = 0;
        for (ByteBuffer buffer : buffers) {
            int remaining = buffer.remaining();
            buffer.get(bytes, offset, remaining);
            offset += remaining;
            // record size
            totalsize += remaining;
        }
        String chunk = this.current + new String(bytes, StandardCharsets.UTF_8);
        String[] lines = chunk.split("\n");
        var lineCounter = lines.length - 1;
        // capture the last line for the next chunk
        this.current = lines[lineCounter];
        for (int i = 0; i < lineCounter; i++) {
            var oneLine = lines[i];
            if (!"[".equals(oneLine) && !"]".equals(oneLine)) {
                count++;
            }
            dataSink.accept(oneLine);
        }
        this.subscription.request(1);

    }

    @Override
    public void onError(Throwable throwable) {
        result.completeExceptionally(throwable);

    }

    @Override
    public void onComplete() {
        dataSink.accept(current);
        result.complete(totalsize);
    }

    @Override
    public CompletionStage<Integer> getBody() {
        return result;
    }

}
