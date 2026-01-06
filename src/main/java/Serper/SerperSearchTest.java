package Serper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SerperSearchTest {
    public static void main(String[] args) {
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");

        try {
            // 1. Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // 2. Preparar la búsqueda
            String query = "Java programming tutorial";
            String jsonBody = "{\"q\":\"" + query + "\"}";

            // 3. Crear la petición
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://google.serper.dev/search"))
                    .header("X-API-KEY", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // 4. Enviar y recibir respuesta
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // 5. Mostrar resultados
            System.out.println("\n=== Búsqueda: " + query + " ===");
            System.out.println("Status: " + response.statusCode());
            System.out.println("\n=== Resultados JSON ===");
            System.out.println(response.body());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}