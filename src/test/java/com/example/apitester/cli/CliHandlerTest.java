package com.example.apitester.cli;

import com.example.apitester.exception.CliException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliHandlerTest {

    @Test
    void parsesFilePath() {
        CliOptions options = CliHandler.parse(new String[]{"tests.json"});

        assertEquals(Path.of("tests.json"), options.file());
        assertFalse(options.helpRequested());
    }

    @Test
    void noColorFlagDisablesColorInAnyPosition() {
        assertFalse(CliHandler.parse(new String[]{"--no-color", "t.json"}).color());
        assertFalse(CliHandler.parse(new String[]{"t.json", "--no-color"}).color());
    }

    @Test
    void helpDoesNotRequireAFile() {
        assertTrue(CliHandler.parse(new String[]{"--help"}).helpRequested());
        assertTrue(CliHandler.parse(new String[]{"-h"}).helpRequested());
    }

    @Test
    void missingFileIsRejected() {
        CliException e = assertThrows(CliException.class, () -> CliHandler.parse(new String[]{}));
        assertEquals("Missing test file", e.getMessage());
    }

    @Test
    void unknownOptionIsRejected() {
        CliException e = assertThrows(CliException.class,
                () -> CliHandler.parse(new String[]{"--bogus", "t.json"}));
        assertTrue(e.getMessage().contains("--bogus"));
    }

    @Test
    void twoFilesAreRejected() {
        assertThrows(CliException.class, () -> CliHandler.parse(new String[]{"a.json", "b.json"}));
    }
}
