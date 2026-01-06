package SerperJmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AllEndpoints {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");

        JmesPath<JsonNode> jmespath = new JacksonRuntime();
        ObjectMapper mapper = new ObjectMapper();

        // 1. SEARCH
        System.out.println("=== 1. SEARCH ===");
        String searchResult = buscar(apiKey, "search", "Java programming");
        JsonNode searchJson = mapper.readTree(searchResult);
        JsonNode searchTitulos = jmespath.compile("organic[0:3].title").search(searchJson);
        System.out.println(searchTitulos.toPrettyString());

        // 2. IMAGES
        System.out.println("\n=== 2. IMAGES ===");
        String imagesResult = buscar(apiKey, "images", "golden retriever");
        JsonNode imagesJson = mapper.readTree(imagesResult);
        JsonNode imageUrls = jmespath.compile("images[0:3].{titulo: title, imagen: imageUrl}").search(imagesJson);
        System.out.println(imageUrls.toPrettyString());

        // 3. NEWS
        System.out.println("\n=== 3. NEWS ===");
        String newsResult = buscar(apiKey, "news", "artificial intelligence");
        JsonNode newsJson = mapper.readTree(newsResult);
        JsonNode newsTitulos = jmespath.compile("news[0:3].{titulo: title, fuente: source, fecha: date}").search(newsJson);
        System.out.println(newsTitulos.toPrettyString());

        // 4. VIDEOS
        System.out.println("\n=== 4. VIDEOS ===");
        String videosResult = buscar(apiKey, "videos", "tutorial Java");
        JsonNode videosJson = mapper.readTree(videosResult);
        JsonNode videosTitulos = jmespath.compile("videos[0:3].{titulo: title, canal: channel, duracion: duration}").search(videosJson);
        System.out.println(videosTitulos.toPrettyString());

        // 5. PLACES
        System.out.println("\n=== 5. PLACES ===");
        String placesResult = buscar(apiKey, "places", "restaurants in New York");
        JsonNode placesJson = mapper.readTree(placesResult);
        JsonNode placesTitulos = jmespath.compile("places[0:3].{nombre: title, direccion: address, rating: rating}").search(placesJson);
        System.out.println(placesTitulos.toPrettyString());

        // 6. SHOPPING
        System.out.println("\n=== 6. SHOPPING ===");
        String shoppingResult = buscar(apiKey, "shopping", "mechanical keyboard");
        JsonNode shoppingJson = mapper.readTree(shoppingResult);
        JsonNode shoppingProductos = jmespath.compile("shopping[0:3].{producto: title, precio: price, vendedor: source}").search(shoppingJson);
        System.out.println(shoppingProductos.toPrettyString());
    }

    private static String buscar(String apiKey, String endpoint, String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"q\":\"" + query + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://google.serper.dev/" + endpoint))
                .header("X-API-KEY", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}