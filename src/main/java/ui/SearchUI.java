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

public class SearchUI extends JFrame {
    private JTextField searchField;
    private JButton searchButton, chooseButton;
    private JList<String> resultList;
    private Indexer indexer;
    private JTextArea chosenPathDisplay;
    private Set<String> selectedFiles = new HashSet<>();

    public SearchUI(Indexer indexer) {
        this.indexer = indexer;
        initComponents();
        setSize(1000, 600);
        setTitle("Search Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

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

        //Create the search field
        searchField = new JTextField(20);
        searchField.setMaximumSize(searchField.getPreferredSize());
        searchPanel.add(searchField, BorderLayout.NORTH);
        
        //Set the search button to be disabled until a file is selected
        searchButton = new JButton("Search");
        searchButton.setEnabled(false);
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton, BorderLayout.SOUTH);

        //Set the result list to be scrollable
        resultList = new JList<>();
        searchPanel.add(new JScrollPane(resultList), BorderLayout.CENTER);

        // Create a split pane to hold the file selection panel and search panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileSelectionPanel, searchPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);
    }

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
                                System.out.println("Selected: " + file.getName());
                                selectedFiles.add(file.getAbsolutePath());
                            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                                System.out.println("Deselected: " + file.getName());
                                selectedFiles.remove(file.getAbsolutePath());
                            }
                            System.out.println(selectedFiles);
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


    private void updateChosenPathDisplay() {
        chosenPathDisplay.setText(String.join("\n", selectedFiles));
        selectedFiles.stream()
            .map(Path::of)
            .map(Path::getParent)
            .distinct()
            .map(Path::toString)
            .forEach(indexer::indexDirectory);
        searchButton.setEnabled(!selectedFiles.isEmpty());


        JMenu selectedFilesMenu = new JMenu("Selected Files");
        for (String filePath : selectedFiles) {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(filePath, true);
            menuItem.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    selectedFiles.remove(filePath);
                    updateChosenPathDisplay();
                }
            });
            selectedFilesMenu.add(menuItem);
        }


        JMenuBar menuBar = getJMenuBar();
        if (menuBar == null) {
            menuBar = new JMenuBar();
            setJMenuBar(menuBar);
        }
        menuBar.add(selectedFilesMenu);
    }


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



