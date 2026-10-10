# Word Frequency CLI

A small Java command-line tool that finds the most common words in a UTF-8 text file. Use it to inspect repeated terms in notes, drafts, or sample text.

The project practices file I/O, regular expressions, `HashMap` counting, and sorting with Java's standard library. It has no runtime dependencies or build-tool requirement.

## Features

- Case-insensitive counts with predictable behavior across system locales.
- Unicode letters, including accented words; composed and decomposed accents are normalized.
- Internal apostrophes such as `don't`, including curly apostrophes.
- Results sorted by count descending, then token ascending when counts tie.
- Optional result limit, clear input errors, and meaningful exit codes.
- A plain Java test harness and GitHub Actions checks on Java 17 and 21.

## Quick start

Install a **JDK 17 or newer** and confirm both `java -version` and `javac -version` work. From the repository directory:

```sh
mkdir -p build
javac --release 17 -encoding UTF-8 -d build src/*.java
java -cp build WordFrequency examples/notes.txt 5
```

Expected output (columns are separated by tabs):

```text
WORD    COUNT
a       2
build   2
easier  2
little  2
make    2
```

## Usage

```sh
java -cp build WordFrequency <utf8-file> [top]
java -cp build WordFrequency "my notes.txt" 20
java -cp build WordFrequency --help
```

`top` defaults to **10** and must be a positive integer. If the file has fewer unique words than the limit, all words are shown. An empty file prints only the column header.

| Exit code | Meaning |
| --- | --- |
| `0` | Successful output or help |
| `1` | The file could not be read as UTF-8 |
| `2` | Invalid command-line arguments |

## Tests

```sh
mkdir -p build
javac --release 17 -encoding UTF-8 -d build src/*.java test/*.java
java -cp build WordFrequencyTest
```

The harness checks Unicode normalization, apostrophes, locale independence, sorting and limits, real file output, malformed UTF-8, missing files, and invalid arguments. It exits with an error if a check fails; no test framework is needed.

## How it works

1. Read the supplied file as UTF-8.
2. Normalize the text to NFC, lowercase with `Locale.ROOT`, and normalize curly apostrophes.
3. Match letter sequences with optional internal apostrophes and count them in a map.
4. Sort entries and print the requested number of results.

## Limits

This is a word counter, not a language-aware tokenizer. Hyphens separate words, numbers are ignored, and adjacent Chinese or Japanese letters are treated as one token without dictionary segmentation. Combining marks that remain separate after NFC normalization are treated as separators. Sorting uses Java string order rather than a language-specific dictionary order.

The whole file and frequency map are held in memory, so the tool is intended for small text files. It does not read PDF/Word formats or standard input. Counts use Java integers.

## Project layout

```text
src/WordFrequency.java       CLI and counting logic
test/WordFrequencyTest.java  Dependency-free checks
examples/notes.txt           Sample text
.github/workflows/test.yml   Java 17/21 CI
```

## License

[MIT](LICENSE)
