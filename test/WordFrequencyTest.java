import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Plain Java tests: run with java -cp build WordFrequencyTest. */
public final class WordFrequencyTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        check(WordFrequency.countWords("Hello, HELLO! Java 123.")
                .equals(Map.of("hello", 2, "java", 1)), "case, punctuation, and numbers");
        check(WordFrequency.countWords("Café cafe\u0301 naïve 你好")
                .equals(Map.of("café", 2, "naïve", 1, "你好", 1)), "Unicode letters and normalization");
        check(WordFrequency.countWords("Don't don’t 'quoted' rock'n'roll")
                .equals(Map.of("don't", 2, "quoted", 1, "rock'n'roll", 1)), "internal apostrophes");
        check(WordFrequency.countWords("123 -- !!!").isEmpty(), "input without words");

        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            check(WordFrequency.countWords("INPUT input").equals(Map.of("input", 2)),
                    "lowercase is independent of system locale");
        } finally {
            Locale.setDefault(original);
        }

        List<String> ranked = WordFrequency.ranked(Map.of("beta", 2, "alpha", 2, "most", 3), 2)
                .stream().map(Map.Entry::getKey).toList();
        check(ranked.equals(List.of("most", "alpha")), "ties and top limit");

        Path file = Files.createTempFile("word-frequency-test-", ".txt");
        try {
            Files.writeString(file, "Java java café", StandardCharsets.UTF_8);
            Result success = run(file.toString(), "2");
            check(success.code == 0 && success.out.equals("WORD\tCOUNT\njava\t2\ncafé\t1\n")
                    && success.err.isEmpty(), "UTF-8 file CLI output");
            Files.writeString(file, "", StandardCharsets.UTF_8);
            check(run(file.toString()).out.equals("WORD\tCOUNT\n"), "empty file output");
            Files.write(file, new byte[] {(byte) 0xC3, (byte) 0x28});
            check(run(file.toString()).code == 1, "malformed UTF-8 is reported");
        } finally {
            Files.deleteIfExists(file);
        }
        check(run(file.toString()).code == 1, "missing file exit code");
        check(run().code == 2, "missing arguments");
        check(run("file", "1", "extra").code == 2, "extra arguments");
        for (String invalid : List.of("0", "-1", "abc", "2147483648")) {
            Result result = run("file", invalid);
            check(result.code == 2 && result.err.contains("positive integer") && result.out.isEmpty(),
                    "invalid top: " + invalid);
        }
        Result help = run("--help");
        check(help.code == 0 && help.out.contains("Usage:") && help.err.isEmpty(), "help output");
        System.out.println("Passed " + checks + " checks.");
    }

    private static Result run(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int code = WordFrequency.run(args, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Result(code, out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"),
                err.toString(StandardCharsets.UTF_8));
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("Failed: " + name);
        }
        checks++;
    }

    private record Result(int code, String out, String err) {}
}
