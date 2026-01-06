package SerperJmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SerperNJmesPathTest {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");

        // Buscar
        HttpClient client = HttpClient.newHttpClient();
        String query = "site:instagram.com intext:willyrex";
        String jsonBody = "{\"q\":\"" + query + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://google.serper.dev/search"))
                .header("X-API-KEY", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Procesar con JMESPath
        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());

        // Extraer títulos
        JsonNode titulos = jmespath.compile("organic[*].title").search(json);
        System.out.println("Tittles:");
        System.out.println(titulos);

        // Extraer top 3 con snippet
        JsonNode top3 = jmespath.compile("organic[0:3].{Tittle: title, link: link, snippet: snippet}").search(json);
        System.out.println("\nTop 3:");
        System.out.println(top3.toPrettyString());
    }
}