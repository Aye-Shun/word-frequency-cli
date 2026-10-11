# Word Frequency CLI

A Java program that counts words in a text file and prints the most common ones. The main pieces are a regular expression for finding words, a `HashMap` for counting, and a comparator for sorting the results.

## Run it

Use a JDK 17 or newer. There are no dependencies to install.

```sh
mkdir -p build
javac --release 17 -encoding UTF-8 -d build src/*.java
java -cp build WordFrequency examples/notes.txt 5
```

Output from the included sample (the columns are separated by tabs):

```text
WORD    COUNT
a       2
build   2
easier  2
little  2
make    2
```

Pass a UTF-8 file and, optionally, a positive result limit:

```sh
java -cp build WordFrequency "my notes.txt" 20
java -cp build WordFrequency --help
```

The default limit is 10. Counts ignore case, keep internal apostrophes such as `don't`, and support Unicode letters. Accented text is normalized before counting. Ties are sorted by Java string order. Empty files print just the header.

Exit codes are `0` for success or help, `1` for a file error, and `2` for invalid arguments.

## Checks

```sh
mkdir -p build
javac --release 17 -encoding UTF-8 -d build src/*.java test/*.java
java -cp build WordFrequencyTest
```

The test harness checks counting, accents, apostrophes, sorting, locale behavior, and file/argument errors. GitHub Actions runs it on Java 17 and 21.

## Limitations

The program reads the whole file into memory, so small text files are the intended use. Numbers are ignored and hyphens split words. It does not segment Chinese or Japanese text into dictionary words; combining marks that remain separate after normalization also split words. PDF/Word files and standard input are not supported. Counts use Java integers.

[MIT license](LICENSE).
