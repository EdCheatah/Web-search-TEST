package SerperJmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

/**
 * SerperAllOPTest:
 * - Llama a SearchFilters.cleanQuery(...) para obtener la safeQuery.
 * - Llama a la API de Serper con la safeQuery.
 * - Una vez obtenido el JSON crudo, delega a SearchFilters.filterResults(...) usando la query ORIGINAL para filtrar estrictamente.
 */
public class SerperAllOPTest {
    private static final ObjectMapper M = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR: exporta SERPER_SEARCH_API_KEY antes de ejecutar.");
            System.exit(1);
        }

        // Si pasas una query por args usa esa una sola, sino usa ejemplos
        String[] ejemplos = {
                "site:github.com Java",
                "intext:tutorial Python",
                "allintext:machine learning basics",
                "\"artificial intelligence\"",
                "site:github.com intext:\"machine learning\" filetype:md",
                "intext:willyrex"
        };

        String[] queries = (args != null && args.length > 0) ? new String[]{args[0]} : ejemplos;

        for (String originalQuery : queries) {
            System.out.println("\n---- QUERY ORIGINAL: " + originalQuery + " ----");

            // 1) Clean the query (remove broken operators for Serper)
            String safeQuery = SearchFilters.cleanQuery(originalQuery);

            // 2) Call Serper with the safeQuery
            JsonNode root = callSerper(apiKey, safeQuery);
            if (root == null) {
                System.err.println("No se obtuvo JSON válido de Serper para: " + safeQuery);
                continue;
            }

            JsonNode organic = root.path("organic");
            if (!organic.isArray()) {
                System.err.println("WARN: Serper no devolvió 'organic' como array. Imprimiendo nodo root:");
                System.err.println(M.writerWithDefaultPrettyPrinter().writeValueAsString(root));
                continue;
            }

            // 3) Filtrar manualmente según la INTENCIÓN original (originalQuery)
            ArrayNode finalFiltered = SearchFilters.filterResults(organic, originalQuery);

            // 4) Imprimir JSON final filtrado
            System.out.println("RESULTADOS FILTRADOS (cumplen intención original):");
            System.out.println(M.writerWithDefaultPrettyPrinter().writeValueAsString(finalFiltered));
        }
    }

    private static JsonNode callSerper(String apiKey, String query) throws Exception {
        String jsonBody = "{\"q\":\"" + escapeJson(query) + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://google.serper.dev/search"))
                .header("X-API-KEY", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP Status: " + response.statusCode());

        if (response.statusCode() != 200) {
            System.err.println("Respuesta NO 200 de Serper. Body:");
            System.err.println(response.body());
            return null;
        }

        String body = response.body();
        if (body == null || body.trim().startsWith("<")) {
            System.err.println("Respuesta HTML / bloqueada por Serper:");
            System.err.println(body);
            return null;
        }

        return M.readTree(body);
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
