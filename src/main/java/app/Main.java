package app;

import javax.swing.SwingUtilities;
import core.Indexer;
import core.Search;  // Make sure this import is correct
import ui.SearchUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Indexer indexer = new Indexer();
            Search search = new Search(indexer);
            SearchUI searchUI = new SearchUI(search);
            searchUI.setVisible(true);
        });
    }
}
