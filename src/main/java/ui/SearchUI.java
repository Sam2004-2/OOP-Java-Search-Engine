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
import java.util.logging.Logger;
import java.util.logging.Level;


import core.Search;
import core.SpellChecker;

/**
 * SearchUI provides a graphical user interface for a file search tool.
 * It allows users to select a directory or file, enter a search term,
 * and view files that match the search term along with the number of occurrences.
 */
public class SearchUI extends JFrame {
    // Components
    private JButton searchButton, chooseButton;
    private JList<String> resultList, searchHistoryList;
    private JTextArea chosenPathDisplay;
    private Search search;
    private Set<String> selectedFiles = new HashSet<>();
    private JTabbedPane searchTabs;
    private SpellChecker spellChecker;
    private JFrame pieChartFrame;
    private static final Logger LOGGER = Logger.getLogger(SearchUI.class.getName());
    private JTextField exactSearchField, separateWordsSearchField, wildcardSearchField;


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
        setLayout(new BorderLayout(10, 10)); // Set the layout for the frame
    
        // Initialize search fields for each tab
        exactSearchField = new JTextField(20);
        separateWordsSearchField = new JTextField(20);
        wildcardSearchField = new JTextField(20);
    
        // Create panels for each type of search
        JPanel exactSearchPanel = new JPanel(new BorderLayout());
        JPanel separateWordsSearchPanel = new JPanel(new BorderLayout());
        JPanel wildcardSearchPanel = new JPanel(new BorderLayout());
    
        // Setup exact search panel
        exactSearchPanel.add(new JLabel("Enter exact phrase:"), BorderLayout.NORTH);
        exactSearchPanel.add(exactSearchField, BorderLayout.CENTER);
        exactSearchPanel.add(createSearchButton(), BorderLayout.SOUTH); // Add search button to panel
    
        // Setup separate words search panel
        separateWordsSearchPanel.add(new JLabel("Enter words, separated by commas:"), BorderLayout.NORTH);
        separateWordsSearchPanel.add(separateWordsSearchField, BorderLayout.CENTER);
        separateWordsSearchPanel.add(createSearchButton(), BorderLayout.SOUTH); // Add search button to panel
    
        // Setup wildcard search panel
        wildcardSearchPanel.add(new JLabel("Enter search pattern with wildcards (*):"), BorderLayout.NORTH);
        wildcardSearchPanel.add(wildcardSearchField, BorderLayout.CENTER);
        wildcardSearchPanel.add(createSearchButton(), BorderLayout.SOUTH); // Add search button to panel
    
        // Initialize the tabbed pane and add the panels as tabs
        searchTabs = new JTabbedPane();
        searchTabs.addTab("Exact", exactSearchPanel);
        searchTabs.addTab("Separate Words", separateWordsSearchPanel);
        searchTabs.addTab("Wildcards", wildcardSearchPanel);
    
        // Result display area
        resultList = new JList<>();
        JScrollPane listScrollPane = new JScrollPane(resultList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));
    
        // File selection and display panel
        JPanel fileSelectionPanel = new JPanel(new BorderLayout());
        chosenPathDisplay = new JTextArea(5, 20);
        chosenPathDisplay.setEditable(false);
        JScrollPane pathScrollPane = new JScrollPane(chosenPathDisplay);
        pathScrollPane.setBorder(BorderFactory.createTitledBorder("Chosen Path"));
        chooseButton = new JButton("Choose Directory or File");
        chooseButton.addActionListener(e -> chooseDirectoryOrFile());
        fileSelectionPanel.add(pathScrollPane, BorderLayout.CENTER);
        fileSelectionPanel.add(chooseButton, BorderLayout.SOUTH);
    
        // Add components to the main frame
        add(searchTabs, BorderLayout.NORTH);
        add(listScrollPane, BorderLayout.CENTER);
        add(fileSelectionPanel, BorderLayout.EAST);
    
        // Search history setup
        DefaultListModel<String> searchHistoryListModel = new DefaultListModel<>();
        searchHistoryList = new JList<>(searchHistoryListModel);
        JScrollPane historyScrollPane = new JScrollPane(searchHistoryList);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Search History"));
        add(historyScrollPane, BorderLayout.WEST);
    
        pack(); // Pack the components neatly
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
        selectedFiles.forEach(file -> search.indexDirectory(file));
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
        String term;
        int tabIndex = searchTabs.getSelectedIndex();
    
        // Determine the appropriate search term based on the selected tab
        switch (tabIndex) {
            case 0:
                term = exactSearchField.getText().trim();
                break;
            case 1:
                term = separateWordsSearchField.getText().trim();
                break;
            case 2:
                term = wildcardSearchField.getText().trim();
                break;
            default:
                LOGGER.log(Level.SEVERE, "Unexpected tab selection");
                JOptionPane.showMessageDialog(this, "Unexpected tab selection.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }
    
        if (term.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.", "Empty Search Term", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        // Perform initial search with the current term
        List<Map.Entry<String, Integer>> results = performSearchBasedOnTab(term, tabIndex);
        if (results.isEmpty()) {
            LOGGER.log(Level.INFO, "No results found, initiating spell check for term: {0}", term);
            List<String> suggestions = spellChecker.suggestCorrections(term);
            if (!suggestions.isEmpty() && !suggestions.contains(term.toLowerCase())) {
                suggestions.add("Continue with '" + term + "'");
                String chosenSuggestion = (String) JOptionPane.showInputDialog(this,
                        "Did you mean:",
                        "Spell Check Suggestion",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        suggestions.toArray(),
                        suggestions.get(0));
                if (chosenSuggestion != null && !chosenSuggestion.equals("Continue with '" + term + "'")) {
                    term = chosenSuggestion; // User chose to use a suggested correction
                    // Re-search with the new term
                    results = performSearchBasedOnTab(term, tabIndex);
                }
            }
        }
    
        updateSearchResults(results);
        updateSearchHistory(term, results);
        LOGGER.log(Level.INFO, "Search completed with term: {0}", term);
    }
    
    private List<Map.Entry<String, Integer>> performSearchBasedOnTab(String term, int tabIndex) {
        switch (tabIndex) {
            case 0:
                LOGGER.log(Level.FINE, "Performing exact search for: {0}", term);
                return search.performSearch(term);
            case 1:
                LOGGER.log(Level.FINE, "Performing comma-separated search for: {0}", term);
                return search.performCommaSeparatedSearch(term);
            case 2:
                LOGGER.log(Level.FINE, "Performing wildcard search for: {0}", term);
                return search.performWildcardSearch(term);
            default:
                LOGGER.log(Level.SEVERE, "Unexpected tab index: {0}", tabIndex);
                throw new IllegalStateException("Unexpected tab index: " + tabIndex);
        }
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
