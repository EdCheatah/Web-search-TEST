import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
public class DemosPlaywright {
    public static void main(String[] args) {
        //Role, Context and goal
        //Adding new commits
        //trying something
        System.out.println("Hello");
        int newVector[] = {1,2,3,4};
        System.out.println(newVector[0]);

        //Lets try to add now the requested libraries
        Document doc = Jsoup.connect("https://example.com").get();
        System.out.println(doc.title());
    }
}
