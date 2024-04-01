package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import core.Indexer;

public class SearchUI extends JFrame {
    private JTextField searchField;
    private JButton searchButton;
    private JList<String> resultList;
    private JButton chooseButton;
    private Indexer indexer;
    private JTextArea chosenPathDisplay;


    private void chooseDirectoryOrFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Directory or File");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        chooser.setAcceptAllFileFilterUsed(true); // Optional: to restrict to directories only, for example

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            // Run indexing in a separate thread
            new Thread(() -> {
                indexer.indexDirectory(selectedFile.getAbsolutePath());
                chosenPathDisplay.setText(selectedFile.getAbsolutePath());
            }).start();
        }
    }



    public SearchUI(Indexer indexer) {
        this.indexer = indexer;
        setTitle("Search Tool");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        northPanel.add(searchField);
        northPanel.setPreferredSize(new Dimension(getWidth(), 65));

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
        northPanel.add(chooseButton); // Add the choose button to the panel

        add(northPanel, BorderLayout.NORTH); // Add the panel to the NORTH region of the BorderLayout

        searchButton = new JButton("Search");
        add(searchButton, BorderLayout.SOUTH);

        resultList = new JList<>();
        add(new JScrollPane(resultList), BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
    }

    private void performSearch() {
        System.out.println("Performing search");
        String query = searchField.getText();
        Set<String> searchResults = indexer.search(query);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        searchResults.forEach(listModel::addElement);
        resultList.setModel(listModel);
    }


    }

