package app;

import javax.swing.SwingUtilities;
import core.Indexer;
import ui.SearchUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Indexer indexer = new Indexer();
            SearchUI searchUI = new SearchUI(indexer);
            searchUI.setVisible(true);
        });
    }
}
