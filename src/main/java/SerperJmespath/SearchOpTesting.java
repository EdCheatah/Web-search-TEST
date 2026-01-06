package SerperJmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SearchOpTesting {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");

        // Prueba diferentes operadores
        probarOperador(apiKey, "site:instagram.com willyrex");
        probarOperador(apiKey, "filetype:pdf Java");
        probarOperador(apiKey, "intitle:tutorial Python");
        probarOperador(apiKey, "site:github.com intext:machine learning");
    }

    private static void probarOperador(String apiKey, String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"q\":\"" + query + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://google.serper.dev/search"))
                .header("X-API-KEY", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());

        JsonNode resultados = jmespath.compile("organic[0:3].{titulo: title, url: link}").search(json);

        System.out.println("\n=== Query: " + query + " ===");
        System.out.println(resultados.toPrettyString());
    }
}