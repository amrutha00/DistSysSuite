package com.distributedsystems.logquerier;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Server {
    private final String host;
    private final int port;
    private final List<String> logFiles;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        this.logFiles = getLogFiles(port);
    }

    private List<String> getLogFiles(int port) {
        List<String> logFiles = new ArrayList<>();
        // String root = System.getProperty("user.dir"); // Use current directory for
        // logs
        String baseDir = System.getProperty("log.base.dir", "src/main/resources/manual-logs");
        // System.out.println("baseDir "+baseDir);
        String logDirName = baseDir + "/log-dir-" + (port - 55550); // Adjust the base port number if necessary

        // System.out.println("logDir" +logDirName);
        Path logDirPath = Paths.get(logDirName);
        if (Files.exists(logDirPath) && Files.isDirectory(logDirPath)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDirPath, "*.log")) {
                for (Path file : directoryStream) {
                    logFiles.add(file.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("[ERROR]: Log directory " + logDirName + " not found in directory " + baseDir);
        }
        return logFiles;
    }

    public void run() {
        if (logFiles.isEmpty()) {
            System.err.println("[ERROR]: No log files found. Server shutting down.");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[INFO]: Server started at " + host + ":" + port + " with log files " + logFiles);
            while (true) {
                System.out.println("[INFO]: Waiting for connection ...");
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("[INFO]: Connected to " + clientSocket.getInetAddress());
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.out.println("[ERROR]: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String patternWithFlags = in.readLine();
            if (patternWithFlags != null) {
                String[] parts = patternWithFlags.split(" ", 2);
                String pattern = parts[0];
                int flags = 0;
                boolean invertMatch = false;
                boolean countOnly = false;
                boolean listFiles = false;
                boolean listFilesWithoutMatch = false;

                if (parts.length > 1) {
                    String flagString = parts[1];
                    if (flagString.contains("i")) {
                        flags |= Pattern.CASE_INSENSITIVE;
                    }
                    if (flagString.contains("w")) {
                        pattern = "\\b" + pattern + "\\b"; // Whole word match
                    }
                    if (flagString.contains("v")) {
                        invertMatch = true; // Invert match
                    }
                    if (flagString.contains("c")) {
                        countOnly = true; // Count matches
                    }
                    if (flagString.contains("l")) {
                        listFiles = true; // List files with matches
                    }
                    if (flagString.contains("L")) {
                        listFilesWithoutMatch = true; // List files without matches
                    }
                }

                Pattern regex = Pattern.compile(pattern, flags);
                List<String> buffer = new ArrayList<>();
                Set<String> filesWithMatches = new HashSet<>();
                Set<String> filesWithoutMatches = new HashSet<>();

                for (String logFile : logFiles) {
                    boolean fileHasMatches = false;
                    int matchCount = 0;
                    try (BufferedReader fileReader = Files.newBufferedReader(Paths.get(logFile))) {
                        String line;
                        int lineNumber = 0;
                        while ((line = fileReader.readLine()) != null) {
                            lineNumber++;
                            boolean matches = regex.matcher(line).find();
                            if (invertMatch) {
                                matches = !matches;
                            }
                            if (matches) {
                                fileHasMatches = true;
                                matchCount++;
                                if (!countOnly && !listFiles && !listFilesWithoutMatch) {
                                    buffer.add("{\"log-path\":\"" + logFile + "\",\"host\":\"" + host +
                                            "\",\"port\":\"" + port + "\",\"line_number\":\"" + lineNumber +
                                            "\",\"content\":\"" + line + "\"}");
                                }

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (fileHasMatches) {
                        filesWithMatches.add(logFile);
                        if (countOnly) {
                            buffer.add("{\"log-dir-path\":\"" + logFile + "\",\"host\":\"" + host +
                                    "\",\"port\":\"" + port + "\",\"count\":\"" + matchCount +
                                    "\"}");
                        }
                    } else {
                        filesWithoutMatches.add(logFile);
                    }
                }

                if (listFiles) {
                    buffer.clear();
                    for (String fileName : filesWithMatches) {
                        buffer.add(String.format("{\"host\":\"%s\",\"port\":%d,\"log_path\":\"%s\"}", host, port,
                                fileName));
                    }
                } else if (listFilesWithoutMatch) {
                    buffer.clear();
                    for (String fileName : filesWithoutMatches) {
                        buffer.add(String.format("{\"host\":\"%s\",\"port\":%d,\"log_path\":\"%s\"}", host, port,
                                fileName));
                    }
                }

                if (!buffer.isEmpty()) {
                    out.println(String.join("\n", buffer));
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Server <host> <port>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        Server server = new Server(host, port);
        server.run();
    }
}
