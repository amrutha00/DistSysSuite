package com.distributedsystems.logquerier;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RegexPatternTest {
    private static final List<Process> serverProcesses = new ArrayList<>();

    @BeforeAll
    public static void setUp() throws Exception {
        startServers();
    }

    private static void startServers() throws InterruptedException {
        int[] ports = { 55551, 55552, 55553 };
        String classPath = "target/classes";
        for (int port : ports) {
            try {
                serverProcesses.add(new ProcessBuilder("java", "-cp", classPath,
                        "com.distributedsystems.logquerier.Server", "localhost", String.valueOf(port))
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Thread.sleep(5000);
    }

    @AfterAll
    public static void tearDown() {
        for (Process process : serverProcesses) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    public void testErrorFlag() throws IOException, InterruptedException {
        executeGrepTest("ERROR", "output-ERROR.txt");
    }

    @Test
    public void testErrorCountFlag() throws IOException, InterruptedException {
        executeGrepTest("ERROR -c", "output-ERROR-c.txt");
    }

    @Test
    public void testErrorListFlag() throws IOException, InterruptedException {
        executeGrepTest("ERROR -l", "output-ERROR-l.txt");
    }

    @Test
    public void testErrorWordFlag() throws IOException, InterruptedException {
        executeGrepTest("ERROR -w", "output-ERROR-w.txt");
    }

    @Test
    public void testErrorInvertFlag() throws IOException, InterruptedException {
        executeGrepTest("ERROR -v", "output-ERROR-v.txt");
    }

    @Test
    public void testInfoListFlag() throws IOException, InterruptedException {
        executeGrepTest("INFO -L", "output-INFO-L.txt");
    }

    @Test
    public void testCombinedListAndCountFlags() throws IOException, InterruptedException {
        executeGrepTest("ERROR -l -c", "output-ERROR-l-c.txt");
    }

    @Test
    public void testCombinedWordAndInvertFlags() throws IOException, InterruptedException {
        executeGrepTest("ERROR -w -v", "output-ERROR-w-v.txt");
    }

    @Test
    public void testRegexAnyContent() throws IOException, InterruptedException {
        executeGrepTest(".*", "output--.txt");
    }

    @Test
    public void testSpecificRegexPattern() throws IOException, InterruptedException {
        executeGrepTest("2023-05-21 .* ERROR", "output-2023-05-21-ERROR.txt");
    }

    private void executeGrepTest(String pattern, String filename) throws IOException, InterruptedException {
        Client client = new Client(getHosts(), getPorts());
        client.query(pattern);

        File expectedFile = new File("src/test/resources/expected_outputs/" + filename);
        File actualFile = new File("target/output/" + filename);

        boolean result = FileUtils.contentEqualsIgnoreEOL(expectedFile, actualFile, "UTF-8");
        if (!result) {
            System.out.println("Expected File Content for pattern: " + pattern);
            printFileContent(expectedFile);

            System.out.println("Actual File Content for pattern: " + pattern);
            printFileContent(actualFile);

        }
        assertTrue(result, "Mismatch between expected and actual output for pattern: " + pattern);
    }

    private void printFileContent(File file) throws IOException {
        List<String> lines = FileUtils.readLines(file, "UTF-8");
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private List<String> getHosts() {
        return List.of("localhost", "localhost", "localhost");
    }

    private List<Integer> getPorts() {
        return List.of(55551, 55552, 55553);
    }
}
