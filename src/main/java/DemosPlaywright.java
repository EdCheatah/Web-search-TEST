import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class DemosPlaywright {
    public static void main(String[] args) {
        //Role, Context and goal
        //Adding new commits
        //trying something
        System.out.println("Hello");
        int newVector[] = {1,2,3,4};
        System.out.println(newVector[0]);

        //Lets try to add now the requested libraries
        // JSoup con manejo de excepciones
        try {
            Document doc = Jsoup.connect("https://example.com").get();
            System.out.println("Título de la página: " + doc.title());
        } catch (IOException e) {
            System.out.println("Error al conectar: " + e.getMessage());
        }
    }
}
