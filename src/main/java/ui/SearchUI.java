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

import core.Indexer;

/**
 * SearchUI provides a graphical user interface for a file search tool.
 * It allows users to select a directory or file, enter a search term,
 * and view files that match the search term along with the number of occurrences.
 */
public class SearchUI extends JFrame {
    private JTextField searchField; // Text field for entering search terms
    private JButton searchButton, chooseButton; // Buttons for initiating search and choosing file/directory
    private JList<String> resultList; // List displaying search results
    private Indexer indexer; // Indexer instance for performing searches
    private JTextArea chosenPathDisplay; // Displays selected file or directory path
    private Set<String> selectedFiles = new HashSet<>(); // Holds the paths of selected files

    /**
     * Constructs a SearchUI frame with a given indexer.
     * @param indexer The indexer to use for searching files.
     */
    public SearchUI(Indexer indexer) {
        this.indexer = indexer;
        initComponents();
        setSize(1000, 600);
        setTitle("Search Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Initializes the components of the UI including layout, panels, and event listeners.
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel fileSelectionPanel = new JPanel();
        fileSelectionPanel.setLayout(new BorderLayout());

        chosenPathDisplay = new JTextArea(10, 20);
        chosenPathDisplay.setEditable(false);
        JScrollPane pathScrollPane = new JScrollPane(chosenPathDisplay);
        pathScrollPane.setBorder(BorderFactory.createTitledBorder("Chosen Path"));
        fileSelectionPanel.add(pathScrollPane, BorderLayout.NORTH);

        chooseButton = new JButton("Choose Directory or File");
        chooseButton.addActionListener(e -> chooseDirectoryOrFile());
        fileSelectionPanel.add(chooseButton, BorderLayout.SOUTH);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());

        searchField = new JTextField(20);
        searchField.setMaximumSize(searchField.getPreferredSize());
        searchPanel.add(searchField, BorderLayout.NORTH);

        searchButton = new JButton("Search");
        searchButton.setEnabled(false);
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton, BorderLayout.SOUTH);

        resultList = new JList<>();
        searchPanel.add(new JScrollPane(resultList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileSelectionPanel, searchPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);
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
                            searchButton.setEnabled(!selectedFiles.isEmpty());
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
            .forEach(indexer::indexDirectory);
        searchButton.setEnabled(!selectedFiles.isEmpty());
    }

    /**
     * Performs a search based on the text entered into the searchField and updates the resultList with the search results.
     */
    private void performSearch() {
        String query = searchField.getText();
        List<Map.Entry<String, Integer>> searchResults = indexer.search(query);

        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for \"" + query + "\".", "No Results", JOptionPane.INFORMATION_MESSAGE);
        } else {
            DefaultListModel<String> listModel = new DefaultListModel<>();
            searchResults.forEach(entry -> listModel.addElement(entry.getKey() + ": " + entry.getValue() + " occurrences"));
            resultList.setModel(listModel);
        }
    }
}
