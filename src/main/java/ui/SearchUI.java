package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

import core.Search;
import core.SpellChecker;

public class SearchUI extends JFrame {
    private JTextField exactSearchField, commaSeparatedSearchField, wildcardSearchField;
    private JButton searchButton, chooseButton;
    private JList<String> resultList, searchHistoryList;
    private JTextArea chosenPathDisplay;
    private Search search;
    private Set<String> selectedFiles = new HashSet<>();
    private JTabbedPane searchTabs;
    private SpellChecker spellChecker;
    private JFrame pieChartFrame;
    private PieChart pieChart;

    private static final Logger LOGGER = Logger.getLogger(SearchUI.class.getName());

    public SearchUI(Search search, SpellChecker spellChecker) {
        this.search = search;
        this.spellChecker = spellChecker;
        initComponents();
        setSize(1000, 600);
        setTitle("Search Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        LOGGER.log(Level.INFO, "SearchUI initialized and visible.");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
    
        exactSearchField = new JTextField(20);
        commaSeparatedSearchField = new JTextField(20);
        wildcardSearchField = new JTextField(20);
    
        chooseButton = new JButton("Choose Directory or File");
    
        searchTabs = new JTabbedPane();
    
        JPanel exactSearchPanel = new JPanel(new BorderLayout());
        JPanel separateWordsSearchPanel = new JPanel(new BorderLayout());
        JPanel wildcardSearchPanel = new JPanel(new BorderLayout());

        pieChart = new PieChart();
        pieChartFrame = new JFrame("Pie Chart");
        pieChartFrame.add(pieChart);
        pieChartFrame.setSize(500, 500);
        pieChartFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // Set to hide instead of dispose if you want to keep the state
        pieChartFrame.setVisible(true);
    
        exactSearchPanel.add(new JLabel("Enter exact phrase:"), BorderLayout.NORTH);
        exactSearchPanel.add(exactSearchField, BorderLayout.CENTER);
        exactSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);  // Add a new search button to this panel
    
        separateWordsSearchPanel.add(new JLabel("Enter words, separated by commas:"), BorderLayout.NORTH);
        separateWordsSearchPanel.add(commaSeparatedSearchField, BorderLayout.CENTER);
        separateWordsSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);  // Add a new search button to this panel
    
        wildcardSearchPanel.add(new JLabel("Enter search pattern with wildcards (*):"), BorderLayout.NORTH);
        wildcardSearchPanel.add(wildcardSearchField, BorderLayout.CENTER);
        wildcardSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);  // Add a new search button to this panel
    
        searchTabs.addTab("Exact", exactSearchPanel);
        searchTabs.addTab("Separate Words", separateWordsSearchPanel);
        searchTabs.addTab("Wildcards", wildcardSearchPanel);
    
        resultList = new JList<>();
        JScrollPane listScrollPane = new JScrollPane(resultList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));
    
        JPanel fileSelectionPanel = new JPanel(new BorderLayout());
        chosenPathDisplay = new JTextArea(5, 20);
        chosenPathDisplay.setEditable(false);
        JScrollPane pathScrollPane = new JScrollPane(chosenPathDisplay);
        pathScrollPane.setBorder(BorderFactory.createTitledBorder("Chosen Path"));
        chooseButton.addActionListener(e -> chooseDirectoryOrFile());
        fileSelectionPanel.add(pathScrollPane, BorderLayout.CENTER);
        fileSelectionPanel.add(chooseButton, BorderLayout.SOUTH);
    
        add(searchTabs, BorderLayout.NORTH);
        add(listScrollPane, BorderLayout.CENTER);
        add(fileSelectionPanel, BorderLayout.EAST);
    
        DefaultListModel<String> searchHistoryListModel = new DefaultListModel<>();
        searchHistoryList = new JList<>(searchHistoryListModel);
        JScrollPane historyScrollPane = new JScrollPane(searchHistoryList);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Search History"));
    
        add(historyScrollPane, BorderLayout.WEST);
    
        pack();
    }
    
    private JButton createSearchButton() {
        JButton button = new JButton("Search");
        button.addActionListener(e -> performSearch());
        return button;
    }
    

    private void performSearch() {
        String term;
        List<Map.Entry<String, Integer>> results = null;
    
        // Determine which search tab is active and get the appropriate text field content
        switch (searchTabs.getSelectedIndex()) {
            case 0:
                term = exactSearchField.getText();
                break;
            case 1:
                term = commaSeparatedSearchField.getText();
                break;
            case 2:
                term = wildcardSearchField.getText();
                break;
            default:
                LOGGER.log(Level.SEVERE, "Unexpected tab selection");
                return;
        }
    
        // Check for spelling suggestions before performing the search
        if (term != null && !term.isEmpty()) {
            List<String> suggestions = spellChecker.suggestCorrections(term);
            if (!suggestions.isEmpty() && !suggestions.contains(term.toLowerCase())) {
                // Suggest corrections
                int choice = JOptionPane.showOptionDialog(this,
                                                          "Did you mean: " + suggestions + " ?",
                                                          "Spell Check Suggestion",
                                                          JOptionPane.YES_NO_OPTION,
                                                          JOptionPane.QUESTION_MESSAGE,
                                                          null, suggestions.toArray(), suggestions.get(0));
                if (choice != JOptionPane.CLOSED_OPTION) {
                    term = suggestions.get(choice);
                }
            }
            // Perform the search with the corrected or verified term
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
            }
        }
    
        if (results == null || results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for the query.", "No Results", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.log(Level.WARNING, "No search results found.");
        } else {
            updateSearchResults(results);
            updateSearchHistory(term, results);
        }
    }
    

    private void updateSearchResults(List<Map.Entry<String, Integer>> searchResults) {
        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for the query.", "No Results", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.log(Level.WARNING, "No search results found.");
        } else {
            DefaultListModel<String> listModel = new DefaultListModel<>();
            searchResults.forEach(entry -> {
                String displayText = String.format("File: %s - Occurrences: %d", entry.getKey(), entry.getValue());
                listModel.addElement(displayText);
            });
            resultList.setModel(listModel);
            LOGGER.log(Level.INFO, "Search results updated in UI.");
        }
    }

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
        pieChartFrame.getContentPane().removeAll();
        PieChart pieChart = new PieChart();
        pieChart.updateChart(history);
        pieChartFrame.add(pieChart);
        pieChartFrame.revalidate();
        LOGGER.log(Level.INFO, "Search history updated and pie chart refreshed.");
    }

    private void chooseDirectoryOrFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setAcceptAllFileFilterUsed(true);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            LOGGER.log(Level.INFO, "File/directory selected: {0}", selected.getAbsolutePath());
            if (selected.isDirectory()) {
                displayFileSelectionCheckboxes(selected);
            } else {
                selectedFiles.add(selected.getAbsolutePath());
                updateChosenPathDisplay();
            }
        }
    }

    private void displayFileSelectionCheckboxes(File directory) {
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        File[] files = directory.listFiles();
        LOGGER.log(Level.INFO, "Displaying file selection for directory: {0}", directory.getAbsolutePath());

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    JCheckBox checkBox = new JCheckBox(file.getName());
                    checkBox.addItemListener(e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            selectedFiles.add(file.getAbsolutePath());
                        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                            selectedFiles.remove(file.getAbsolutePath());
                        }
                        updateChosenPathDisplay();
                    });
                    checkBoxPanel.add(checkBox);
                }
            }
        }
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        JOptionPane.showMessageDialog(this, scrollPane, "Select Files", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateChosenPathDisplay() {
        chosenPathDisplay.setText(String.join("\n", selectedFiles));
        LOGGER.log(Level.INFO, "Updated chosen path display and re-indexing selected files.");
        selectedFiles.forEach(file -> search.indexDirectory(file));
        searchButton.setEnabled(!selectedFiles.isEmpty());
    }
}
