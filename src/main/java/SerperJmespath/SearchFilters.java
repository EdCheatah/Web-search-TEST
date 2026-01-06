package SerperJmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

/**
 * SearchFilters:
 * - cleanQuery(originalQuery) -> devuelve la query "safe" para enviar a Serper (quita intext:, allintext:, comillas problemáticas, pero conserva site: y filetype:)
 * - filterResults(organic, originalQuery) -> filtra el array 'organic' devuelto por Serper, aplicando estrictamente la intención original.
 */
public class SearchFilters {
    private static final ObjectMapper M = new ObjectMapper();

    // Patterns
    private static final Pattern INTEXT_QUOTED = Pattern.compile("(?i)intext:\\s*\"([^\"]+)\"");
    private static final Pattern INTEXT_SIMPLE = Pattern.compile("(?i)intext:\\s*([^\\s\"]+)");
    private static final Pattern ALLINTEXT_QUOTED = Pattern.compile("(?i)allintext:\\s*\"([^\"]+)\"");
    private static final Pattern ALLINTEXT_SIMPLE = Pattern.compile("(?i)allintext:\\s*([^\\s\"]+)");
    private static final Pattern QUOTED_PHRASE = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern SITE = Pattern.compile("(?i)site:([^\\s]+)");
    private static final Pattern FILETYPE = Pattern.compile("(?i)filetype:([^\\s]+)");

    /**
     * Devuelve una query "safe" para enviar a Serper: elimina los prefijos problemáticos (intext:, allintext:, comillas),
     * pero mantiene site: y filetype: si están.
     */
    public static String cleanQuery(String originalQuery) {
        if (originalQuery == null) return "";

        String safe = originalQuery;

        // Reemplazar intext:"frase" -> frase
        Matcher m = INTEXT_QUOTED.matcher(safe);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1)));
        }
        m.appendTail(sb);
        safe = sb.toString();

        // intext:token -> token
        safe = INTEXT_SIMPLE.matcher(safe).replaceAll("$1");

        // allintext:"frase" -> frase
        m = ALLINTEXT_QUOTED.matcher(safe);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1)));
        }
        m.appendTail(sb);
        safe = sb.toString();

        // allintext:token -> token
        safe = ALLINTEXT_SIMPLE.matcher(safe).replaceAll("$1");

        // quitar comillas de frases restantes (dejamos las palabras para que Serper no bloquee)
        m = QUOTED_PHRASE.matcher(safe);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1)));
        }
        m.appendTail(sb);
        safe = sb.toString();

        // compactar espacios
        safe = safe.replaceAll("\\s+", " ").trim();

        // fallback: si queda vacío, usar palabras alfanuméricas de la original
        if (safe.isEmpty()) {
            safe = originalQuery.replaceAll("[\"']", " ").replaceAll("\\s+", " ").trim();
        }

        System.out.println("-> safeQuery enviada a Serper: \"" + safe + "\"");
        return safe;
    }

    /**
     * Filtra estrictamente el array 'organic' según la intención expresada en originalQuery.
     * Devuelve ArrayNode con {title, link, snippet, date} de los que PASARON todos los filtros.
     */
    public static ArrayNode filterResults(JsonNode organic, String originalQuery) {
        ArrayNode out = M.createArrayNode();
        if (organic == null || !organic.isArray()) return out;

        String q = originalQuery == null ? "" : originalQuery;

        // extraer requisitos desde la query original
        List<String> intextTerms = extractIntextTerms(q);
        List<String> allintextTerms = extractAllIntextTerms(q);
        List<String> quotedPhrases = extractQuotedPhrases(q);
        String siteToken = extractSingle(SITE, q);
        String fileType = extractSingle(FILETYPE, q);

        for (JsonNode item : organic) {
            String title = item.path("title").asText("");
            String snippet = item.path("snippet").asText("");
            String link = item.path("link").asText("");
            String date = item.path("date").asText("");

            if (passesAllFilters(title, snippet, link, intextTerms, allintextTerms, quotedPhrases, siteToken, fileType)) {
                ObjectNode obj = M.createObjectNode();
                obj.put("title", title);
                obj.put("link", link);
                obj.put("snippet", snippet);
                obj.put("date", date);
                out.add(obj);
            }
        }

        return out;
    }

    // ---------------------- extractors ----------------------
    private static List<String> extractIntextTerms(String q) {
        List<String> res = new ArrayList<>();
        Matcher m = INTEXT_QUOTED.matcher(q);
        while (m.find()) {
            String phrase = m.group(1).trim();
            if (!phrase.isEmpty()) res.add(phrase.toLowerCase(Locale.ROOT));
        }
        m = INTEXT_SIMPLE.matcher(q);
        while (m.find()) {
            String token = m.group(1).trim();
            if (!token.isEmpty()) res.add(token.toLowerCase(Locale.ROOT));
        }
        // Broad capture: intext: followed by multiple words (untagged)
        Pattern broad = Pattern.compile("(?i)intext:\\s*([^\\n]+)");
        m = broad.matcher(q);
        if (m.find()) {
            String tail = m.group(1).trim();
            tail = tail.split("\\s+(site:|filetype:|intitle:|inurl:|-|\\|)")[0];
            String[] ws = tail.split("\\s+");
            for (String w : ws) if (!w.isBlank()) res.add(w.toLowerCase(Locale.ROOT));
        }
        return res;
    }

    private static List<String> extractAllIntextTerms(String q) {
        List<String> res = new ArrayList<>();
        Matcher m = ALLINTEXT_QUOTED.matcher(q);
        while (m.find()) {
            String phrase = m.group(1).trim();
            if (!phrase.isEmpty()) {
                for (String w : phrase.split("\\s+")) if (!w.isBlank()) res.add(w.toLowerCase(Locale.ROOT));
            }
        }
        m = ALLINTEXT_SIMPLE.matcher(q);
        while (m.find()) {
            String token = m.group(1).trim();
            if (!token.isEmpty()) res.add(token.toLowerCase(Locale.ROOT));
        }
        Pattern broad = Pattern.compile("(?i)allintext:\\s*([^\\n]+)");
        m = broad.matcher(q);
        if (m.find()) {
            String tail = m.group(1).trim();
            tail = tail.split("\\s+(site:|filetype:|intitle:|inurl:|-|\\|)")[0];
            for (String w : tail.split("\\s+")) if (!w.isBlank()) res.add(w.toLowerCase(Locale.ROOT));
        }
        return res;
    }

    private static List<String> extractQuotedPhrases(String q) {
        List<String> res = new ArrayList<>();
        Matcher m = QUOTED_PHRASE.matcher(q);
        while (m.find()) {
            String phrase = m.group(1).trim();
            if (!phrase.isEmpty()) res.add(phrase.toLowerCase(Locale.ROOT));
        }
        return res;
    }

    private static String extractSingle(Pattern p, String q) {
        Matcher m = p.matcher(q);
        if (m.find()) return m.group(1).toLowerCase(Locale.ROOT);
        return null;
    }

    // ---------------------- filtering ----------------------
    private static boolean passesAllFilters(String title, String snippet, String link,
                                            List<String> intextTerms,
                                            List<String> allintextTerms,
                                            List<String> quotedPhrases,
                                            String siteToken,
                                            String fileType) {
        String tLc = title == null ? "" : title.toLowerCase(Locale.ROOT);
        String sLc = snippet == null ? "" : snippet.toLowerCase(Locale.ROOT);
        String lLc = link == null ? "" : link.toLowerCase(Locale.ROOT);

        // intext: each term/phrase must appear in title or snippet
        for (String term : intextTerms) {
            if (term.contains(" ")) {
                if (!(tLc.contains(term) || sLc.contains(term))) return false;
            } else {
                if (!(tLc.contains(term) || sLc.contains(term))) return false;
            }
        }

        // allintext: each word must appear in title or snippet
        for (String w : allintextTerms) {
            if (!(tLc.contains(w) || sLc.contains(w))) return false;
        }

        // quoted phrase exact matches
        for (String phrase : quotedPhrases) {
            if (!(tLc.contains(phrase) || sLc.contains(phrase))) return false;
        }

        // site:
        if (siteToken != null && !siteToken.isBlank()) {
            if (!lLc.contains(siteToken)) return false;
        }

        // filetype:
        if (fileType != null && !fileType.isBlank()) {
            String ext = fileType.startsWith(".") ? fileType.substring(1) : fileType;
            if (!lLc.endsWith("." + ext)) return false;
        }

        return true;
    }
}
