package ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.awt.event.ItemEvent;



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
    
        // File selection area
        JPanel fileSelectionPanel = new JPanel();
        fileSelectionPanel.setLayout(new BoxLayout(fileSelectionPanel, BoxLayout.Y_AXIS));
        fileSelectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        chosenPathDisplay = new JTextArea(2, 20);
        chosenPathDisplay.setEditable(false);
        JScrollPane pathScrollPane = new JScrollPane(chosenPathDisplay);
        pathScrollPane.setBorder(BorderFactory.createTitledBorder("Chosen Path"));
        fileSelectionPanel.add(pathScrollPane);
    
        chooseButton = new JButton("Choose Directory or File");
        chooseButton.addActionListener(e -> chooseDirectoryOrFile());
        fileSelectionPanel.add(chooseButton);
    
        add(fileSelectionPanel, BorderLayout.NORTH);
    
        // Search area
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
    
        searchField = new JTextField(20);
        searchField.setMaximumSize(searchField.getPreferredSize());
        searchPanel.add(searchField);
    
        searchButton = new JButton("Search");
        searchButton.setEnabled(false); // Initially disabled
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);
    
        resultList = new JList<>();
        searchPanel.add(new JScrollPane(resultList));
    
        add(searchPanel, BorderLayout.CENTER);
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
                    checkBox.addItemListener(e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            selectedFiles.add(file.getAbsolutePath());
                        } else {
                            selectedFiles.remove(file.getAbsolutePath());
                        }
                        searchButton.setEnabled(!selectedFiles.isEmpty());
                        updateChosenPathDisplay(); // Update the display and index the files
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
        indexer.indexFiles(selectedFiles); 
        searchButton.setEnabled(!selectedFiles.isEmpty());
    }


    private void performSearch() {
        String query = searchField.getText();
        Map<String, Integer> searchResults = indexer.search(query);

        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for \"" + query + "\".", "No Results", JOptionPane.INFORMATION_MESSAGE);
        } else {
            DefaultListModel<String> listModel = new DefaultListModel<>();
            searchResults.forEach((filePath, count) -> listModel.addElement(filePath + ": " + count + " occurrences"));
            resultList.setModel(listModel);
        }
    }
}



