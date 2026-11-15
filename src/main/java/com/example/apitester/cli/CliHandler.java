package com.example.apitester.cli;

import com.example.apitester.exception.CliException;

import java.nio.file.Path;

public final class CliHandler {

    public static final String USAGE = """
            Usage: java -jar api-tester.jar [options] <tests.json>

            Options:
              -h, --help     Show this help and exit
                  --no-color Disable colored output

            Exit codes:
              0  all tests passed
              1  at least one test failed
              2  invalid usage or invalid test file
            """;

    private CliHandler() {
    }

    public static CliOptions parse(String[] args) {
        Path file = null;
        boolean help = false;
        boolean color = colorSupportedByDefault();

        for (String arg : args) {
            switch (arg) {
                case "-h", "--help" -> help = true;
                case "--no-color" -> color = false;
                default -> {
                    if (arg.startsWith("-")) {
                        throw new CliException("Unknown option: " + arg);
                    }
                    if (file != null) {
                        throw new CliException("Only one test file can be given");
                    }
                    file = Path.of(arg);
                }
            }
        }

        if (!help && file == null) {
            throw new CliException("Missing test file");
        }
        return new CliOptions(file, help, color);
    }

    /** Color only when attached to a terminal and the NO_COLOR convention isn't set. */
    private static boolean colorSupportedByDefault() {
        return System.getenv("NO_COLOR") == null && System.console() != null;
    }
}
