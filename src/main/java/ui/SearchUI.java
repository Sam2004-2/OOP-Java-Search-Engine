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

/**
 * SearchUI provides a graphical user interface for a file search tool.
 * It allows users to select a directory or file, enter a search term,
 * and view files that match the search term along with the number of occurrences.
 */
public class SearchUI extends JFrame {
    private JTextField searchField;
    private JButton searchButton, chooseButton;
    private JList<String> resultList;
    private Search search;
    private JTextArea chosenPathDisplay;
    private Set<String> selectedFiles = new HashSet<>();
    private JTabbedPane searchTabs;

    public SearchUI(Search search) {
        this.search = search;
        initComponents();
        setSize(1000, 600);
        setTitle("Search Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }


    private JButton createSearchButton() {
        JButton button = new JButton("Search");
        button.addActionListener(e -> performSearch());
        return button;
    }

    /**
     * Initializes the components of the UI including layout, panels, and event listeners.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        searchField = new JTextField(20);


        chooseButton = new JButton("Choose Directory or File");


        searchTabs = new JTabbedPane();


        JPanel exactSearchPanel = new JPanel(new BorderLayout());
        JPanel separateWordsSearchPanel = new JPanel(new BorderLayout());
        JPanel wildcardSearchPanel = new JPanel(new BorderLayout());


        exactSearchPanel.add(new JLabel("Enter exact phrase:"), BorderLayout.NORTH);
        exactSearchPanel.add(searchField, BorderLayout.CENTER);


        separateWordsSearchPanel.add(new JLabel("Enter words, separated by commas:"), BorderLayout.NORTH);
        separateWordsSearchPanel.add(new JTextField(20), BorderLayout.CENTER); // Separate field if needed

        wildcardSearchPanel.add(new JLabel("Enter search pattern with wildcards (*):"), BorderLayout.NORTH);
        wildcardSearchPanel.add(new JTextField(20), BorderLayout.CENTER); // Separate field if needed

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


        exactSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);
        separateWordsSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);
        wildcardSearchPanel.add(createSearchButton(), BorderLayout.SOUTH);

        pack();
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
     * Performs a search based on the text entered into the searchField and updates the resultList with the search results.
     */
    private void performSearch() {
        String term = searchField.getText();
        List<Map.Entry<String, Integer>> results;

        switch (searchTabs.getSelectedIndex()) {
            case 0: //
                results = search.performSearch(term);
                break;
            case 1: //
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
