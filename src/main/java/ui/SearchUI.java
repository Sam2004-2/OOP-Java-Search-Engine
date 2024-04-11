package ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import core.Search;
import core.SpellChecker;

/**
 * SearchUI provides a graphical user interface for a file search tool.
 * It allows users to select a directory or file, enter a search term,
 * and view files that match the search term along with the number of occurrences.
 */
public class SearchUI extends JFrame {
    // Components
    private JTextField searchField;
    private JButton searchButton, chooseButton;
    private JList<String> resultList, searchHistoryList;
    private JTextArea chosenPathDisplay;
    private Search search;
    private Set<String> selectedFiles = new HashSet<>();
    private JTabbedPane searchTabs;
    private SpellChecker spellChecker;

    // Constructor
    public SearchUI(Search search, SpellChecker spellChecker) {
        this.search = search;
        this.spellChecker = spellChecker;
        initComponents(); // Initialize components
        setSize(1000, 600);
        setTitle("Search Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Create search button with ActionListener
    private JButton createSearchButton() {
        JButton button = new JButton("Search");
        button.addActionListener(e -> performSearch());
        return button;
    }

    // Initialize components of the UI
    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Set layout

        // Search field
        searchField = new JTextField(20);

        // Choose directory or file button
        chooseButton = new JButton("Choose Directory or File");

        // Tabs for different search types
        searchTabs = new JTabbedPane();

        // Panels for different search types
        JPanel exactSearchPanel = new JPanel(new BorderLayout());
        JPanel separateWordsSearchPanel = new JPanel(new BorderLayout());
        JPanel wildcardSearchPanel = new JPanel(new BorderLayout());

        // Labels and text fields for different search types
        exactSearchPanel.add(new JLabel("Enter exact phrase:"), BorderLayout.NORTH);
        exactSearchPanel.add(searchField, BorderLayout.CENTER);
        separateWordsSearchPanel.add(new JLabel("Enter words, separated by commas:"), BorderLayout.NORTH);
        separateWordsSearchPanel.add(new JTextField(20), BorderLayout.CENTER);
        wildcardSearchPanel.add(new JLabel("Enter search pattern with wildcards (*):"), BorderLayout.NORTH);
        wildcardSearchPanel.add(new JTextField(20), BorderLayout.CENTER);

        // Add panels to tabs
        searchTabs.addTab("Exact", exactSearchPanel);
        searchTabs.addTab("Separate Words", separateWordsSearchPanel);
        searchTabs.addTab("Wildcards", wildcardSearchPanel);

        // Result list
        resultList = new JList<>();
        JScrollPane listScrollPane = new JScrollPane(resultList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));

        // File selection panel
        JPanel fileSelectionPanel = new JPanel(new BorderLayout());
        chosenPathDisplay = new JTextArea(5, 20);
        chosenPathDisplay.setEditable(false);
        JScrollPane pathScrollPane = new JScrollPane(chosenPathDisplay);
        pathScrollPane.setBorder(BorderFactory.createTitledBorder("Chosen Path"));
        chooseButton.addActionListener(e -> chooseDirectoryOrFile());
        fileSelectionPanel.add(pathScrollPane, BorderLayout.CENTER);
        fileSelectionPanel.add(chooseButton, BorderLayout.SOUTH);

        // Add components to the frame
        add(searchTabs, BorderLayout.NORTH);
        add(listScrollPane, BorderLayout.CENTER);
        add(fileSelectionPanel, BorderLayout.EAST);

        // Add search button to each search type panel
        searchButton = createSearchButton();
        exactSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);
        separateWordsSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);
        wildcardSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);

        // Search history list with DefaultListModel
        DefaultListModel<String> searchHistoryListModel = new DefaultListModel<>();
        searchHistoryList = new JList<>(searchHistoryListModel);
        JScrollPane historyScrollPane = new JScrollPane(searchHistoryList);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Search History"));

        // Add search history display next to search results
        JPanel searchHistoryPanel = new JPanel(new BorderLayout());
        searchHistoryPanel.add(historyScrollPane, BorderLayout.CENTER);
        add(searchHistoryPanel, BorderLayout.WEST); // Adding to the left side of the layout

        pack(); // Pack components
    }

    // Method to handle choosing directory or file
    private void chooseDirectoryOrFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setAcceptAllFileFilterUsed(true);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected.isDirectory()) {
                displayFileSelectionCheckboxes(selected);
            } else {
                selectedFiles.add(selected.getAbsolutePath());
                updateChosenPathDisplay();
            }
        }
    }

    // Method to display checkboxes for file selection
    private void displayFileSelectionCheckboxes(File directory) {
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    JCheckBox checkBox = new JCheckBox(file.getName());
                    checkBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                selectedFiles.add(file.getAbsolutePath());
                            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                                selectedFiles.remove(file.getAbsolutePath());
                            }
                            updateChosenPathDisplay();
                        }
                    });
                    checkBoxPanel.add(checkBox);
                }
            }
        }
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        JOptionPane.showMessageDialog(this, scrollPane, "Select Files", JOptionPane.PLAIN_MESSAGE);
    }

    // Method to update chosen path display
    private void updateChosenPathDisplay() {
        chosenPathDisplay.setText(String.join("\n", selectedFiles));
        selectedFiles.stream()
                .map(Path::of)
                .map(Path::getParent)
                .distinct()
                .map(Path::toString)
                .forEach(path -> search.indexDirectory(path));
        searchButton.setEnabled(!selectedFiles.isEmpty());
    }

    // Method to update search results
    private void updateSearchResults(List<Map.Entry<String, Integer>> searchResults) {
        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for the query.", "No Results", JOptionPane.INFORMATION_MESSAGE);
        } else {
            DefaultListModel<String> listModel = new DefaultListModel<>();
            searchResults.forEach(entry -> {
                String displayText = String.format("File: %s - Occurrences: %d", entry.getKey(), entry.getValue());
                listModel.addElement(displayText);
            });
            resultList.setModel(listModel);
        }
    }

    // Method to update search history
    private void updateSearchHistory(String searchTerm, List<Map.Entry<String, Integer>> searchResults) {
        // Create a new DefaultListModel to store the search history entries
        DefaultListModel<String> listModel = (DefaultListModel<String>) searchHistoryList.getModel();

        // If the search results are not empty
        if (!searchResults.isEmpty()) {
            // Iterate over each search result
            searchResults.forEach(entry -> {
                // Format the search result information into a display text
                String displayText = String.format("Search term: %s - Occurrences %d", searchTerm, entry.getValue());
                // Add the display text to the search history list model
                listModel.addElement(displayText);
            });
        }

        // Set the new list model as the model for the search history JList
        searchHistoryList.setModel(listModel);
    }

    // Method to perform search
    private void performSearch() {
        String term = searchField.getText();
        List<Map.Entry<String, Integer>> results;

        switch (searchTabs.getSelectedIndex()) {
            case 0:
                results = search.performSearch(term);
                break;
            case 1:
                results = search.performCommaSeparatedSearch(term);
                break;
            case 2:
                results = search.performWildcardSearch(term);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + searchTabs.getSelectedIndex());
        }

        // Update search results
        updateSearchResults(results);

        // Update search history
        updateSearchHistory(term, results);

        List<String> suggestions = spellChecker.suggestCorrections(term);
        if (!suggestions.isEmpty()) {
            // Prompt user with suggestions
            String message = "Did you mean:\n" + String.join("\n", suggestions);
            int choice = JOptionPane.showConfirmDialog(this, message, "Spell Check", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // User selected a suggestion, update the search term
                String selectedSuggestion = (String) JOptionPane.showInputDialog(this,
                        "Select a suggestion:", "Suggested Corrections",
                        JOptionPane.PLAIN_MESSAGE, null, suggestions.toArray(), suggestions.get(0));
                if (selectedSuggestion != null) {
                    term = selectedSuggestion;
                }
            }
        }

        // Perform the search with the updated term
        switch (searchTabs.getSelectedIndex()) {
            case 0:
                results = search.performSearch(term);
                break;
            case 1:
                results = search.performCommaSeparatedSearch(term);
                break;
            case 2:
                results = search.performWildcardSearch(term);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + searchTabs.getSelectedIndex());
        }
        updateSearchResults(results);
    }
}

