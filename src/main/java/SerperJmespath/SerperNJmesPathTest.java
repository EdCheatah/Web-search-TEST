package SerperJmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class SerperNJmesPathTest {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");

        // Construir JSON con más parámetros
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("q", "site:github.com Java");
        requestBody.put("gl", "us");  // Geolocalización
        requestBody.put("hl", "en");  // Idioma
        requestBody.put("num", 10);   // Número de resultados

        String jsonBody = mapper.writeValueAsString(requestBody);

        System.out.println("Request JSON: " + jsonBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://google.serper.dev/search"))
                .header("X-API-KEY", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());

        // Procesar con JMESPath
        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        JsonNode json = mapper.readTree(response.body());

        // Ver respuesta completa (opcional, para debug)
        // System.out.println("\nRespuesta completa:");
        // System.out.println(json.toPrettyString());

        // Extraer títulos
        JsonNode titulos = jmespath.compile("organic[*].title").search(json);
        System.out.println("\nTitles:");
        System.out.println(titulos);

        // Extraer top 3 con snippet
        JsonNode top3 = jmespath.compile("organic[0:3].{Title: title, Link: link, Snippet: snippet, Position: position}").search(json);
        System.out.println("\nTop 3:");
        System.out.println(top3.toPrettyString());

        // Verificar total de resultados
        JsonNode totalResults = jmespath.compile("searchInformation.totalResults").search(json);
        System.out.println("\nTotal results: " + totalResults);
    }
}