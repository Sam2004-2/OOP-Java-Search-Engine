package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
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
        addWindowListener(new WindowAdapter() {
        });
        setVisible(true);
    }


    private void initComponents() {
        JPanel northPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        northPanel.add(searchField);

        chosenPathDisplay = new JTextArea(2, 20);
        chosenPathDisplay.setEditable(false);
        JScrollPane pathScrollPane = new JScrollPane(chosenPathDisplay);
        pathScrollPane.setBorder(BorderFactory.createTitledBorder("Chosen Path"));
        northPanel.add(pathScrollPane);

        chooseButton = new JButton("Choose Directory or File");
        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseDirectoryOrFile();
            }
        });
        northPanel.add(chooseButton);

        add(northPanel, BorderLayout.NORTH);

        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        add(searchButton, BorderLayout.SOUTH);

        resultList = new JList<>();
        add(new JScrollPane(resultList), BorderLayout.CENTER);
    }

    private void chooseDirectoryOrFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = chooser.getSelectedFile();
            displayFileSelectionCheckboxes(selectedDirectory);
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
                    });
                    checkBoxPanel.add(checkBox);
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        JOptionPane.showMessageDialog(this, scrollPane, "Select Files", JOptionPane.PLAIN_MESSAGE);
    }



    private void performSearch() {
        String query = searchField.getText();
        Set<String> searchResults = indexer.search(query);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        searchResults.forEach(listModel::addElement);
        resultList.setModel(listModel);
    }

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        new SearchUI(indexer);
    }
}
