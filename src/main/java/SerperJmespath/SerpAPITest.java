package SerperJmespath;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SerpAPITest {

    public static void main(String[] args) {
        try {
            String apiKey = "52334228785fa51597755f1e6d5a1c6b897943af6324e4f753072fbc9fdb4a28";
            String query = "ingenieria de sistemas";

            String url = "https://serpapi.com/search.json"
                    + "?engine=yandex"
                    + "&text=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&yandex_domain=yandex.ru"
                    + "&api_key=" + apiKey;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println(response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
