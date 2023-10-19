package com.fowlart.main.dos_check;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Affinity Cookie from Azure App Service is used to route requests to the same instance.
 * This snippet shows how to check if the cookie is used correctly.
 * Number of affinity cookies should be equal to the number of instances.
 *  */
public class DDosCheckApplication {

    public static void main(String[] args) {

        var url = "https://dzmilcatalog.azurewebsites.net/pdp";
        HttpClient client = HttpClient.newHttpClient();

        int totalRequests = 1000;
        int maxThreadPoolSize = 100;
        int requestDelayMillis = 30; // Milliseconds to wait between requests

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreadPoolSize);
        Set<String> azureSameSites = new HashSet<>();

        // Submit 10000 tasks to the thread pool
        for (int i = 0; i < totalRequests; i++) {
            final int count = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("Request " + count + " completed with status code " + response.statusCode());
                    //read headers
                    HttpHeaders headers = response.headers();
                    azureSameSites.add(headers.allValues("set-cookie").get(1));
                } catch (Exception e) {
                    // Log any errors, but don't catch them to avoid breaking the loop
                    e.printStackTrace();
                }
            }, threadPoolExecutor);

            // Optionally, handle the completion of the tasks
            future.thenAccept(result -> {
                // Do something with the result if needed
                System.out.println("Request " + count + " completed");
                System.out.println("Affinity Cookie size " + azureSameSites.size());
            });

            // Introduce a small delay between requests
            try {
                Thread.sleep(requestDelayMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Gracefully shutdown the thread pool after all tasks are completed
        threadPoolExecutor.shutdown();
        try {
            threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
