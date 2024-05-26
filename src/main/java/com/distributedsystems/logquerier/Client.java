package com.distributedsystems.logquerier;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    private final List<String> hosts;
    private final List<Integer> ports;

    public Client(List<String> hosts, List<Integer> ports) {
        this.hosts = hosts;
        this.ports = ports;
    }

    public void cleanTempFiles() {
        File directory = new File(System.getProperty("user.dir"));
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".temp"));
        if (files != null) {
            for (File file : files) {
                System.out.println("[INFO] Old temp file " + file.getName() + " is found and cleaned.");
                file.delete();
            }
        }
    }

    public static File getOutputFile(String filename) {
        File directory = new File("target/output");
        if (!directory.exists()) {
            directory.mkdirs(); // Make the directory if it doesn't exist
        }
        return new File(directory, filename);
    }

    public void query(String pattern) {
        cleanTempFiles();
        String fileName = "output-" + pattern.replaceAll("\\W+", "-") + ".txt";
        File outputFile = getOutputFile(fileName);
        long timeStart = System.currentTimeMillis();
        long timeEnd;

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        // List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < hosts.size(); i++) {
            String host = hosts.get(i);
            int port = ports.get(i);
            // Thread workerThread = Thread.ofVirtual().start(new QueryThread(pattern, host,
            // port));
            // threads.add(workerThread);
            executor.execute(new QueryThread(pattern, host, port));
        }

        // for (Thread thread : threads) {
        // try {
        // thread.join();
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        // List<Thread> threads = new ArrayList<>();
        // for (String host : hosts) {
        // Thread workerThread = new Thread(new QueryThread(pattern, host, port));
        // threads.add(workerThread);
        // workerThread.start();
        // }

        // for (Thread thread : threads) {
        // try {
        // thread.join();
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // }

        timeEnd = System.currentTimeMillis();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, false))) {
            System.out.println("===== STAT =====");
            int totalLines = 0;
            for (int i = 0; i < hosts.size(); i++) {
                String host = hosts.get(i);
                int port = ports.get(i);

                File file = new File(host + "-" + port + ".temp");
                int cnt = 0;
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        writer.println(line);
                        cnt++;
                    }
                    // file.delete();
                } catch (IOException e) {
                    System.out.println("No matching files found on this server");
                    // e.printStackTrace();
                }
                totalLines += cnt;
                System.out.printf("From %s, %d lines matched, used %.4f secs.\n", host, cnt,
                        1.0 * (timeEnd - timeStart) / 1000);
            }
            System.out.printf("Total %d line, used %.4f secs.\n", totalLines, 1.0 * (timeEnd - timeStart) / 1000);
            writer.flush();
        } catch (IOException e) {
            System.err.println("[ERROR]: Unable to write to output file: " + outputFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<String> hosts = new ArrayList<>();
        List<Integer> ports = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            hosts.add("localhost");
            ports.add(55550 + i); // Using ports 55551 to 55560 for local testing
        }
        Client client = new Client(hosts, ports);
        if (args.length != 1) {
            System.out.println("[ERROR]: Input arg should be 1 for regex.");
        } else {
            client.query(args[0]);
        }
    }
}

class QueryThread implements Runnable {
    private final String pattern;
    private final String host;
    private final int port;

    public QueryThread(String pattern, String host, int port) {
        this.pattern = pattern;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        List<String> logs = new ArrayList<>();
        try (Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(pattern);

            String line;
            while ((line = in.readLine()) != null) {
                logs.add(line);
            }

            if (!logs.isEmpty()) {
                File file = new File(host + "-" + port + ".temp");
                try (PrintWriter writer = new PrintWriter(file)) {
                    for (String log : logs) {
                        writer.println(log);
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("[ERROR]: Unknown host: " + host);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[ERROR]: I/O error with host: " + host);
            e.printStackTrace();
        }
    }
}
