package SerperJmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SerperAllOPTest {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");

        String[] ejemplos = {
                // SITE
                "site:github.com Java",

                // INTEXT
                "intext:tutorial Python",
                "allintext:machine learning basics",

                // INTITLE
                "intitle:programming guide",
                "allintitle:Java tutorial beginner",

                // INURL
                "inurl:blog programming",
                "allinurl:python tutorial",

                // INANCHOR
                "inanchor:download",
                "allinanchor:click here",

                // FILETYPE
                "filetype:pdf machine learning",
                "ext:docx resume template",

                // DEFINE
                "define:algorithm",

                // RELATED
                "related:github.com",

                // INFO
                "info:wikipedia.org",

                // FRASE EXACTA
                "\"artificial intelligence\"",

                // EXCLUIR
                "Java -JavaScript",
                "tutorial -video",

                // OR
                "Python OR Java tutorial",
                "programming | coding",

                // RANGO
                "smartphone 2020..2024",

                // COMBINADOS
                "site:stackoverflow.com intitle:Java filetype:html",
                "site:github.com intext:\"machine learning\" filetype:md",
                "intitle:tutorial -site:youtube.com Python"
        };

        for (String query : ejemplos) {
            probar(apiKey, query);
        }
    }

    private static void probar(String apiKey, String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"q\":\"" + query + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://google.serper.dev/search"))
                .header("X-API-KEY", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("\nQuery: " + query);
        System.out.println("HTTP Status: " + response.statusCode());

        // SI NO ES 200, NO PARSEES
        if (response.statusCode() != 200) {
            System.out.println("Respuesta NO JSON:");
            System.out.println(response.body());
            return;
        }

        // Si empieza con "<", es HTML
        if (response.body().trim().startsWith("<")) {
            System.out.println("Respuesta HTML (bloqueada):");
            System.out.println(response.body());
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());

        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        JsonNode resultados =
                jmespath.compile("organic[0:2].title").search(json);

        System.out.println("Resultados: " + resultados);
    }

}