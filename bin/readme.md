# File Search Application

This application allows you to search for a term or phrase in a set of files. It displays the files that contain the search term, along with the number of occurrences in each file.

## Structure

The application is divided into three main components:

- **Main**: This is the entry point of the application. It creates an `Indexer` and a `SearchUI`, and makes the `SearchUI` visible.

- **Indexer**: This class is responsible for indexing the files. It has a `search` method that returns a map with the file paths and the number of occurrences of a search term in each file.

- **SearchUI**: This class provides a user interface for performing searches. It has a text field for entering the search term and a list for displaying the results.

## How to Run

1. Compile the application: `javac app/Main.java`
2. Run the application: `java app/Main`

## How to Use

1. Enter a search term in the text field.
2. Press the "Search" button.
3. The application displays the files that contain the search term, along with the number of occurrences in each file.