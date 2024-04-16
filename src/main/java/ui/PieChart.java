package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * PieChart class represents a simple pie chart for displaying search history data.
 */
public class PieChart extends JPanel {
    private Map<String, Integer> data;
    private Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.CYAN};

    /**
     * Constructor for PieChart.
     * Sets the preferred size of the panel.
     */
    public PieChart() {
        this.setPreferredSize(new Dimension(400, 300));
    }

    /**
     * Updates the chart with new data and repaints the panel.
     * @param searchHistory The array of search history strings to update the chart with.
     */
    public void updateChart(String[] searchHistory) {
        Map<String, Integer> newData = convertToMap(searchHistory);
        this.data = newData;
        repaint(); // Redraw the pie chart
    }

    /**
     * Overrides the paintComponent method to draw the pie chart.
     * @param g The Graphics object used for painting.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.isEmpty()) {
            return;
        }

        // Calculate total occurrences
        int total = data.values().stream().mapToInt(Integer::intValue).sum();

        // Draw pie chart
        int startAngle = 0;
        int index = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int arcAngle = (int) ((double) entry.getValue() / total * 360);
            g.setColor(colors[index % colors.length]);
            g.fillArc(100, 50, 200, 200, startAngle, arcAngle);
            drawPieChartLabels(g, entry.getKey(), startAngle, arcAngle);
            startAngle += arcAngle;
            index++;
        }

        // Draw legend
        drawLegend(g);
    }

    /**
     * Draws labels for each pie slice indicating the search term.
     * @param g The Graphics object used for painting.
     * @param searchTerm The search term associated with the pie slice.
     * @param startAngle The starting angle of the pie slice.
     * @param arcAngle The arc angle of the pie slice.
     */
    private void drawPieChartLabels(Graphics g, String searchTerm, int startAngle, int arcAngle) {
        double midAngle = Math.toRadians(startAngle + arcAngle / 2);
        int x = (int) (200 + 100 * Math.cos(midAngle));
        int y = (int) (150 + 100 * Math.sin(midAngle));
        g.setColor(Color.BLACK);
        g.drawString(searchTerm, x, y);
    }

    /**
     * Draws a legend for the pie chart indicating the search terms and their colors.
     * @param g The Graphics object used for painting.
     */
    private void drawLegend(Graphics g) {
        int legendX = 350;
        int legendY = 50;
        int legendWidth = 100;
        int legendHeight = 20;
        int legendSpacing = 25;
        int legendTextX = legendX + 20;
        int legendTextY = legendY + 15;
        g.setColor(Color.BLACK);
        g.drawString("Legend:", legendX, legendY - 20);
        int index = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            g.setColor(colors[index % colors.length]);
            g.fillRect(legendX, legendY, legendWidth, legendHeight);
            g.setColor(Color.BLACK);
            g.drawString(entry.getKey() + " - " + entry.getValue(), legendTextX, legendTextY);
            legendY += legendSpacing;
            legendTextY += legendSpacing;
            index++;
        }
    }

    /**
     * Helper method to convert the search history array into a map of search terms and occurrences.
     * @param searchHistory The array of search history strings to convert.
     * @return A map containing search terms as keys and occurrences as values.
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
