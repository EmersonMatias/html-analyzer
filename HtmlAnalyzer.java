import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Stack;

public class HtmlAnalyzer {
    private static final String URL_CONNECTION_ERROR_MESSAGE = "URL connection error";

    public static void main(String[] args) {
        if (args.length != 1) {
            return;
        }

        String urlString = args[0];

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                String deepestText = findDeepestText(reader);

                connection.disconnect();
                inputStream.close();
                inputStreamReader.close();
                
                System.out.println(deepestText);

            } else {
                System.out.println(URL_CONNECTION_ERROR_MESSAGE);
            }
        } catch (IOException e) {
            System.out.println(URL_CONNECTION_ERROR_MESSAGE);
        }

    }

    private static String findDeepestText(BufferedReader reader) throws IOException {
        String line;
        String deepestText = null;
        int maxDepth = 0;
        int currentDepth = 0;
        Stack<String> tagStack = new Stack<>();

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (!line.isEmpty()) {
                if (line.startsWith("</")) {
                    currentDepth--;

                    malformedHtml(line, tagStack);

                } else if (line.startsWith("<")) {
                    currentDepth++;
                    tagStack.push(line);

                } else if (maxDepth == 0 || currentDepth > maxDepth) {
                    maxDepth = currentDepth;
                    deepestText = line;
                }
            }
        }

        if (!tagStack.isEmpty()) {
            deepestText = "malformed HTML";
        }

        reader.close();

        return deepestText;
    }

    private static String extractTagText(String tag) {
        int lastIndex = tag.length() - 1;
        String tagText = null;

        if (tag != null) {
            if (tag.startsWith("</")) {
                tagText = tag.substring(2, lastIndex);
            } else if (tag.startsWith("<")) {
                tagText = tag.substring(1, lastIndex);
            }
        }

        return tagText;
    }

    private static void malformedHtml(String line, Stack<String> tagStack) {
        String closeTagText = extractTagText(line);
        String openTagText = extractTagText(tagStack.lastElement());

        if (openTagText != null && openTagText.equals(closeTagText)) {
            tagStack.pop();
        } else {
            tagStack.push(line);
        }
    }
}