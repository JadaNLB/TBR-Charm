package com.exampletbrcharm.tbrcharm;

import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class BookShuffleController {

    @PostMapping("/shuffle")
    public ResponseEntity<?> shuffleBook(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity
                    .badRequest()
                    .body("Please upload a non-empty .csv file.");
        }

        try (Reader reader = new InputStreamReader(file.getInputStream())) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();
            
            CSVParser parser = new CSVParser(reader, format);

            List<BookDTO> books = new ArrayList<>();

            for (CSVRecord record : parser) {
                String shelf = record.get("Exclusive Shelf").trim();
                if (shelf.equalsIgnoreCase("to-read")) {
                    String title = record.get("Title").trim();
                    String author = record.get("Author").trim();

                    books.add(new BookDTO(title, author));
                }
            }

            if (books.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("No books found on 'to-read' shelf.");
            }

            Random rand = new Random();
            BookDTO selectedBook = books.get(rand.nextInt(books.size()));

            return ResponseEntity.ok(selectedBook);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }
}
