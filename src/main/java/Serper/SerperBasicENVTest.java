package Serper;

//import io.github.cdimascio.dotenv.Dotenv;
public class SerperBasicENVTest {
    public static void main(String[] args) {
      //  Dotenv dotenv = Dotenv.load();
        //String apiKey = dotenv.get("SERPER_SEARCH_API_KEY");
        // Código simple, sin librerías extra
        String apiKey = System.getenv("SERPER_SEARCH_API_KEY");
        //System.out.println("API Key: " + apiKey);
        if(apiKey != null)
        {
            System.out.println("No problems.");
        }else{
            System.out.println("problems.");
        }



   }

}
