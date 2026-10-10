import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Counts words in a UTF-8 text file without external libraries. */
public final class WordFrequency {
    private static final int DEFAULT_TOP = 10;
    private static final Pattern WORD = Pattern.compile("\\p{L}+(?:['’]\\p{L}+)*");
    private static final String USAGE = "Usage: java -cp build WordFrequency <utf8-file> [top]";

    private WordFrequency() {}

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 1 && (args[0].equals("--help") || args[0].equals("-h"))) {
            out.println(USAGE);
            out.println("Print the most frequent words, defaulting to 10 results.");
            return 0;
        }
        if (args.length < 1 || args.length > 2) {
            err.println(USAGE);
            return 2;
        }

        int top = DEFAULT_TOP;
        if (args.length == 2) {
            try {
                top = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                err.println("Error: top must be a positive integer.");
                return 2;
            }
            if (top < 1) {
                err.println("Error: top must be a positive integer.");
                return 2;
            }
        }

        try {
            String input = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
            out.println("WORD\tCOUNT");
            for (Map.Entry<String, Integer> entry : ranked(countWords(input), top)) {
                out.println(entry.getKey() + "\t" + entry.getValue());
            }
            return 0;
        } catch (IOException | InvalidPathException e) {
            err.println("Error: could not read UTF-8 file '" + args[0] + "': " + e.getMessage());
            return 1;
        }
    }

    static Map<String, Integer> countWords(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT).replace('’', '\'');
        Matcher matcher = WORD.matcher(normalized);
        Map<String, Integer> counts = new HashMap<>();
        while (matcher.find()) {
            counts.merge(matcher.group(), 1, Integer::sum);
        }
        return counts;
    }

    static List<Map.Entry<String, Integer>> ranked(Map<String, Integer> counts, int top) {
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                        .reversed().thenComparing(Map.Entry::getKey))
                .limit(top)
                .toList();
    }
}
