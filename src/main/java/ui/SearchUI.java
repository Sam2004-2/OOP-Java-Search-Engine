package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

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
    private JFrame pieChartFrame;

    /**
     * Constructor for SearchUI class.
     * Initializes the components and sets up the frame.
     * @param search The search object used for performing searches.
     * @param spellChecker The spell checker object used for suggesting corrections.
     */
    public SearchUI(Search search, SpellChecker spellChecker) {
        this.search = search;
        this.spellChecker = spellChecker;
        initComponents(); // Initialize components
        setSize(1000, 600);
        setTitle("Search Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        // Initialize PieChart frame
        pieChartFrame = new JFrame("Pie Chart");
        pieChartFrame.setSize(400, 400);
        pieChartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only the pie chart frame
        pieChartFrame.setVisible(true);
        
        // Initialize PieChart
        PieChart pieChart = new PieChart();
        pieChartFrame.add(pieChart); // Add the PieChart to the pie chart frame
    }

    // Create search button with ActionListener
    private JButton createSearchButton() {
        JButton button = new JButton("Search");
        button.addActionListener(e -> performSearch());
        return button;
    }

    /**
     * Initializes the components of the UI including layout, panels, and event listeners.
     */
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

<<<<<<< Updated upstream
        // Add search history display next to search results
        JPanel searchHistoryPanel = new JPanel(new BorderLayout());
        searchHistoryPanel.add(historyScrollPane, BorderLayout.CENTER);
        add(searchHistoryPanel, BorderLayout.WEST); // Adding to the left side of the layout
=======
        // Add search history panel
        add(historyScrollPane, BorderLayout.WEST);
>>>>>>> Stashed changes

        pack(); // Pack components
    }

    /**
     * Opens a file chooser to select a directory or file for indexing and searching.
     */
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

    /**
     * Displays a dialog with checkboxes for selecting individual files within a directory.
     * @param directory The directory from which to display files.
     */
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

    /**
     * Updates the display of chosen paths and re-indexes the selected directories or files.
     */
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

    /**
     * Updates the search results list based on the search results.
     * @param searchResults The list of search results to display.
     */
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

    /**
     * Updates the search history list based on the search term and results.
     * @param searchTerm The search term used in the search.
     * @param searchResults The list of search results.
     */
    private void updateSearchHistory(String searchTerm, List<Map.Entry<String, Integer>> searchResults) {
        DefaultListModel<String> listModel = (DefaultListModel<String>) searchHistoryList.getModel();
        if (!searchResults.isEmpty()) {
            searchResults.forEach(entry -> {
                String displayText = String.format("Search term: %s - Occurrences %d", searchTerm, entry.getValue());
                listModel.addElement(displayText);
            });
        }
        searchHistoryList.setModel(listModel);
        String[] history = new String[listModel.size()];
        listModel.copyInto(history);
        pieChartFrame.getContentPane().removeAll(); // Clear previous chart
        PieChart pieChart = new PieChart();
        pieChart.updateChart(history);
        pieChartFrame.add(pieChart); // Add the updated PieChart to the pie chart frame
        pieChartFrame.revalidate(); // Refresh the frame
    }

    /**
     * Performs a search based on the text entered into the searchField and updates the resultList with the search results.
     */
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

        updateSearchResults(results);
        updateSearchHistory(term, results);

        List<String> suggestions = spellChecker.suggestCorrections(term);
        if (!suggestions.isEmpty()) {
            String message = "Did you mean:\n" + String.join("\n", suggestions);
            int choice = JOptionPane.showConfirmDialog(this, message, "Spell Check", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String selectedSuggestion = (String) JOptionPane.showInputDialog(this,
                        "Select a suggestion:", "Suggested Corrections",
                        JOptionPane.PLAIN_MESSAGE, null, suggestions.toArray(), suggestions.get(0));
                if (selectedSuggestion != null) {
                    term = selectedSuggestion;
                }
            }
        }

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

    /**
     * Converts the array of search history strings into a map.
     * @param searchHistory The array of search history strings.
     * @return A map containing search terms and their occurrences.
     */
    private Map<String, Integer> convertToMap(String[] searchHistory) {
        Map<String, Integer> map = new HashMap<>();
        for (String entry : searchHistory) {
            String[] parts = entry.split(" - ");
            String searchTerm = parts[0].split(": ")[1];
            int occurrences = Integer.parseInt(parts[1].split(" ")[1]);
            map.put(searchTerm, occurrences);
        }
        return map;
    }
}

