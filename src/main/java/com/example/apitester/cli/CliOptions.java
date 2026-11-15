package com.example.apitester.cli;

import java.nio.file.Path;

public record CliOptions(Path file, boolean helpRequested, boolean color) {
}
