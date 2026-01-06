package Jmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;

public class JmespathTest {
    public static void main(String[] args) {
        try {
            // 1. JSON de ejemplo (como String)
            String jsonTexto = """
                {
                    "personas": [
                        {"nombre": "Juan", "edad": 25, "ciudad": "Madrid"},
                        {"nombre": "Ana", "edad": 30, "ciudad": "Barcelona"},
                        {"nombre": "Pedro", "edad": 22, "ciudad": "Madrid"}
                    ]
                }
                """;

            // 2. Configurar JMESPath
            JmesPath<JsonNode> jmespath = new JacksonRuntime();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonTexto);

            // 3. Probar queries
            System.out.println("=== PRUEBAS JMESPATH ===\n");

            // Todos los nombres
            Expression<JsonNode> expr1 = jmespath.compile("personas[*].nombre");
            JsonNode nombres = expr1.search(json);
            System.out.println("Nombres:");
            System.out.println(nombres);

            // Primer persona
            Expression<JsonNode> expr2 = jmespath.compile("personas[0]");
            JsonNode primera = expr2.search(json);
            System.out.println("\nPrimera persona:");
            System.out.println(primera.toPrettyString());

            // Personas de Madrid
            Expression<JsonNode> expr3 = jmespath.compile("personas[?ciudad=='Madrid']");
            JsonNode madrid = expr3.search(json);
            System.out.println("\nPersonas de Madrid:");
            System.out.println(madrid.toPrettyString());

            System.out.println("\nJMESPath funciona!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}