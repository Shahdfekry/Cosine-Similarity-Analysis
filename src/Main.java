
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static Set<String> links = new HashSet<>();
    private static int counter = 0;
    private static final int MAX_URLS = 10;

    public static String preprocess(String word) {
        word = word.toLowerCase(Locale.ROOT);
        StringBuilder newWord = new StringBuilder();
        for (char ch : word.toCharArray()) {
            if ((ch >= 'a' && ch <= 'z')) {
                newWord.append(ch);
            }
        }
        return newWord.toString();
    }
//the function retrieves the file names present in a specified directory and returns them as a Set.
    public static Set<String> listFiles(String dir) {
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }

    public static ArrayList<String> findLines(String fileName) {
        ArrayList<String> words = new ArrayList<>();
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String word = scanner.next();
                word = preprocess(word);
                if (word.length() > 0) {
                    words.add(word);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: File not found.");
        }
        return words;
    }

    public static int parseDocId(String fileName) {
        String strId = "";
        boolean underScore = false;
        for (char ch : fileName.toCharArray()) {
            if (ch == '_') {
                underScore = true;
                continue;
            }
            if (ch == '.') {
                break;
            }
            if (underScore) {
                strId += ch;
            }
        }
        return Integer.parseInt(strId);
    }

    public static Set<String> getQuery() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter query words (separated by spaces): ");
        String input = scanner.nextLine();
        String[] words = input.split("\\s+");
        return new HashSet<>(Arrays.asList(words));
    }

//    public static double calculateTermFrequency(String term, Set<String> words) {
//        int termCount = Collections.frequency(words, term);
//        return (double) termCount / words.size();
//    }
//
//    public static double calculateInverseDocumentFrequency(String term, Set<String> files, HashMap<String, DictEntry> invertedIndex) {
//        int documentFrequency = invertedIndex.getOrDefault(term, new DictEntry()).getDocumentFrequency();
//        if (documentFrequency == 0) {
//            return 0.0; // handle division by zero case
//        }
//        return Math.log((double) files.size() / documentFrequency);
//    }
//
//    public static Map<String, Double> calculateTFIDFForQuery(Set<String> query, Set<String> files, HashMap<String, DictEntry> invertedIndex) {
//        Map<String, Double> tfidfMap = new HashMap<>();
//
//        for (String term : query) {
//            double inverseDocumentFrequency = calculateInverseDocumentFrequency(term, files, invertedIndex);
//
//            for (String fileName : files) {
//                ArrayList<String> words = findLines("Docs/" + fileName);
//                double termFrequency = calculateTermFrequency(term, words);
//                double tfidf = termFrequency * inverseDocumentFrequency;
//
//                // Add the TF-IDF value to the map
//                tfidfMap.put(term + " - " + fileName, tfidf);
//            }
//        }
//
//        return tfidfMap;
//    }

    public static double calculateCosineSimilarity(Map<String, Integer> queryVector, Map<String, Integer> fileVector) {
        double dotProduct = 0.0;
        double queryMagnitude = 0.0;
        double fileMagnitude = 0.0;

        for (Map.Entry<String, Integer> entry : queryVector.entrySet()) {
            String term = entry.getKey();
            int queryValue = entry.getValue();
            int fileValue = fileVector.getOrDefault(term, 0);

            dotProduct += queryValue * fileValue;
            queryMagnitude += Math.pow(queryValue, 2);
        }

        for (int fileValue : fileVector.values()) {
            fileMagnitude += Math.pow(fileValue, 2);
        }

        queryMagnitude = Math.sqrt(queryMagnitude);
        fileMagnitude = Math.sqrt(fileMagnitude);

        if (queryMagnitude == 0 || fileMagnitude == 0) {
            return 0.0; // handle division by zero case
        }

        return dotProduct / (queryMagnitude * fileMagnitude);
    }

    public static List<String> rankFilesByCosineSimilarity(Set<String> files, Set<String> query) {
        List<String> rankedFiles = new ArrayList<>();

        Map<String, Integer> queryVector = new HashMap<>();
        for (String term : query) {
            queryVector.put(term, Collections.frequency(query, term));
        }

        List<Map.Entry<String, Double>> cosineSimilarityEntries = new ArrayList<>();

        for (String fileName : files) {
            ArrayList<String> words = findLines("Docs/" + fileName);

            Map<String, Integer> fileVector = new HashMap<>();
            for (String word : words) {
                word = preprocess(word);
                fileVector.put(word, fileVector.getOrDefault(word, 0) + 1);
            }

            double cosineSimilarity = calculateCosineSimilarity(queryVector, fileVector);
            cosineSimilarityEntries.add(new AbstractMap.SimpleEntry<>(fileName, cosineSimilarity));
        }

        cosineSimilarityEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<String, Double> entry : cosineSimilarityEntries) {
            rankedFiles.add(entry.getKey() + " - Cosine Similarity: " + entry.getValue());
        }

        return rankedFiles;
    }

    public static void getPageLinks(String URL) {
        if (counter >= MAX_URLS) {
            return; // Stop crawling if maximum limit is reached
        }

        if (!links.contains(URL)) {
            try {
                if (links.add(URL)) {
                    System.out.println(URL);
                    counter++; // Increment the counter after adding a new URL
                }

                Document document = Jsoup.connect(URL).get();
                Elements linksOnPage = document.select("a[href]");

                for (Element page : linksOnPage) {
                    if (counter >= MAX_URLS) {
                        return; // Stop crawling if maximum limit is reached
                    }
                    getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {


        String initialURL = "https://en.wikipedia.org/wiki/Computer_science";
        getPageLinks(initialURL);
        Set<String> files = listFiles("Docs");
        HashMap<String, DictEntry> invertedIndex = new HashMap<>();

        for (String fileName : files) {
            int docId = parseDocId(fileName);
            ArrayList<String> words = findLines("Docs/" + fileName);

            for (String word : words) {
                word = preprocess(word);
                DictEntry entry = invertedIndex.getOrDefault(word, new DictEntry());
                entry.add(docId);
                invertedIndex.put(word, entry);
            }
        }

        Set<String> query = getQuery();
        List<String> rankedFiles = rankFilesByCosineSimilarity(files, query);
//        Map<String, Double> queryTFIDF = calculateTFIDFForQuery( query, files, invertedIndex);
//
//        System.out.println("TF-IDF for Query:");
//        for (Map.Entry<String, Double> entry : queryTFIDF.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
//        System.out.println();

        try (PrintWriter printer = new PrintWriter(new FileOutputStream("CosineSimilarityAnalysis.txt", false))) {
            for (String rankedFile : rankedFiles) {
                printer.println(rankedFile);
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }


}


