package com.portfolio.jobprocessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The real unit of work: read a CSV of orders (orderId,item,quantity,price),
 * calculate total revenue, and write a summary file.
 *
 * This replaces the earlier fake Thread.sleep() simulation. Failures here
 * are genuine — a malformed row (e.g. "two" instead of a number) will
 * throw a real exception, which is exactly what the Worker's retry logic
 * is designed to catch and handle.
 */
public class CsvOrderTask implements Runnable {

    private final String inputPath;
    private final String outputDir;

    public CsvOrderTask(String inputPath, String outputDir) {
        this.inputPath = inputPath;
        this.outputDir = outputDir;
    }

    @Override
    public void run() {
        double totalRevenue = 0.0;
        int rowCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line = reader.readLine(); // skip header row
            if (line == null) {
                throw new IOException("File is empty: " + inputPath);
            }

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                // fields[0]=orderId, fields[1]=item, fields[2]=quantity, fields[3]=price

                int quantity = Integer.parseInt(fields[2].trim());
                double price = Double.parseDouble(fields[3].trim());

                totalRevenue += quantity * price;
                rowCount++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + inputPath + ": " + e.getMessage(), e);
        }

        writeSummary(rowCount, totalRevenue);
    }

    private void writeSummary(int rowCount, double totalRevenue) {
        String fileName = new java.io.File(inputPath).getName().replace(".csv", "-summary.txt");
        String outputPath = outputDir + "/" + fileName;

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("Source file: " + inputPath + "\n");
            writer.write("Rows processed: " + rowCount + "\n");
            writer.write(String.format("Total revenue: $%.2f%n", totalRevenue));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write summary for " + inputPath, e);
        }
    }
}