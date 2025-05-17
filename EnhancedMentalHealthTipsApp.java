import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EnhancedMentalHealthTipsApp {

    private static final String FILE_NAME = "mental_health_tips.txt";
    private static final String FAVORITE_MARK = "⭐ "; // Prefix for favorites

    private DefaultListModel<String> listModel;
    private JTextField addTipField;
    private JLabel statusLabel;
    private JList<String> tipsList;
    private JCheckBox showFavoritesOnly;
    private List<String> allTips = new ArrayList<>(); // Full list including favorites
    private Stack<List<String>> undoStack = new Stack<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnhancedMentalHealthTipsApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Enhanced Mental Health Tips");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 650);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 248, 255));

        JLabel headerLabel = new JLabel("Mental Health Tips", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        headerLabel.setForeground(new Color(58, 134, 255));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        tipsList = new JList<>(listModel);
        tipsList.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tipsList.setForeground(new Color(40, 60, 90));
        tipsList.setBackground(new Color(250, 255, 255));
        tipsList.setFixedCellHeight(40);
        tipsList.setSelectionBackground(new Color(200, 220, 255));
        tipsList.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 255), 2));
        tipsList.setCellRenderer(new FavoriteTipCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tipsList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(mainPanel.getBackground());
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel addPanel = new JPanel(new BorderLayout(10, 10));
        addPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addTipField = new JTextField();
        addTipField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        addTipField.setToolTipText("Type a new tip here");
        addPanel.add(addTipField, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Tip");
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addPanel.add(addButton, BorderLayout.EAST);

        bottomPanel.add(addPanel);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(bottomPanel.getBackground());

        JButton removeButton = new JButton("Remove Selected");
        JButton undoButton = new JButton("Undo Remove");
        JButton sortButton = new JButton("Sort Alphabetically");
        showFavoritesOnly = new JCheckBox("Show Favorites Only");

        buttonsPanel.add(removeButton);
        buttonsPanel.add(undoButton);
        buttonsPanel.add(sortButton);
        buttonsPanel.add(showFavoritesOnly);

        bottomPanel.add(buttonsPanel);

        // Favorite toggle button
        JButton toggleFavoriteButton = new JButton("Toggle Favorite");
        toggleFavoriteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(toggleFavoriteButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(new Color(30, 100, 30));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(statusLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Load tips from file (or defaults)
        loadTipsFromFile();

        // Add tip action
        addButton.addActionListener(e -> {
            String newTip = addTipField.getText().trim();
            if (newTip.isEmpty()) {
                showStatus("Please enter a tip to add.", Color.RED);
            } else if (containsTip(newTip)) {
                showStatus("This tip already exists.", Color.RED);
            } else {
                addTip(newTip, false);
                addTipField.setText("");
                saveTipsToFile();
                showStatus("Tip added successfully!", new Color(30, 100, 30));
            }
        });

        // Remove tip action
        removeButton.addActionListener(e -> {
            List<String> selected = tipsList.getSelectedValuesList();
            if (selected.isEmpty()) {
                showStatus("Please select tip(s) to remove.", Color.RED);
                return;
            }
            pushUndo();
            selected.forEach(this::removeTip);
            saveTipsToFile();
            showStatus(selected.size() + " tip(s) removed.", new Color(30, 100, 30));
            refreshList();
        });

        // Undo remove action
        undoButton.addActionListener(e -> {
            if (undoStack.isEmpty()) {
                showStatus("Nothing to undo.", Color.RED);
                return;
            }
            allTips = undoStack.pop();
            saveTipsToFile();
            refreshList();
            showStatus("Undo successful.", new Color(30, 100, 30));
        });

        // Sort action
        sortButton.addActionListener(e -> {
            pushUndo();
            allTips.sort(String.CASE_INSENSITIVE_ORDER);
            saveTipsToFile();
            refreshList();
            showStatus("Tips sorted alphabetically.", new Color(30, 100, 30));
        });

        // Filter favorites toggle
        showFavoritesOnly.addActionListener(e -> refreshList());

        // Toggle favorite on selected tip
        toggleFavoriteButton.addActionListener(e -> {
            int idx = tipsList.getSelectedIndex();
            if (idx == -1) {
                showStatus("Select a tip to toggle favorite.", Color.RED);
                return;
            }
            String tip = listModel.get(idx);
            toggleFavorite(tip);
            saveTipsToFile();
            refreshList();
            showStatus("Favorite toggled.", new Color(30, 100, 30));
        });

        // Show daily tip popup on startup
        showDailyTipPopup();

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private void showDailyTipPopup() {
        if (allTips.isEmpty()) return;
        Random rand = new Random();
        // Pick a random tip (including favorites)
        String tip = allTips.get(rand.nextInt(allTips.size()));
        JOptionPane.showMessageDialog(null, tip.replace(FAVORITE_MARK, ""),
                "Daily Mental Health Tip", JOptionPane.INFORMATION_MESSAGE);
    }

    private void pushUndo() {
        undoStack.push(new ArrayList<>(allTips));
        if (undoStack.size() > 10) undoStack.remove(0); // Limit undo history
    }

    private void addTip(String tip, boolean favorite) {
        String fullTip = favorite ? FAVORITE_MARK + tip : tip;
        allTips.add(fullTip);
        refreshList();
    }

    private void removeTip(String tip) {
        allTips.remove(tip);
    }

    private void toggleFavorite(String tip) {
        int idx = allTips.indexOf(tip);
        if (idx == -1) return;
        String newTip;
        if (tip.startsWith(FAVORITE_MARK)) {
            newTip = tip.substring(FAVORITE_MARK.length());
        } else {
            newTip = FAVORITE_MARK + tip;
        }
        allTips.set(idx, newTip);
    }

    private boolean containsTip(String tip) {
        String normalized = tip.toLowerCase();
        return allTips.stream()
                .map(t -> t.replace(FAVORITE_MARK, ""))
                .anyMatch(t -> t.toLowerCase().equals(normalized));
    }

    private void refreshList() {
        listModel.clear();
        List<String> filtered;
        if (showFavoritesOnly.isSelected()) {
            filtered = allTips.stream()
                    .filter(tip -> tip.startsWith(FAVORITE_MARK))
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>(allTips);
        }
        filtered.forEach(listModel::addElement);
    }

    private void loadTipsFromFile() {
        Path path = Paths.get(FILE_NAME);
        if (!Files.exists(path)) {
            // Load default tips
            allTips = new ArrayList<>(Arrays.asList(
                    "Take breaks and relax your mind regularly.",
                    "Maintain a healthy sleep schedule.",
                    "Exercise to boost your mood and energy.",
                    "Talk to friends or family when feeling overwhelmed.",
                    "Practice mindfulness or meditation daily.",
                    "Limit screen time and social media use.",
                    "Seek professional help if needed."
            ));
            saveTipsToFile();
        } else {
            try {
                allTips = Files.readAllLines(path);
            } catch (IOException e) {
                showStatus("Error loading tips from file.", Color.RED);
                allTips = new ArrayList<>();
            }
        }
        refreshList();
    }

    private void saveTipsToFile() {
        try {
            Files.write(Paths.get(FILE_NAME), allTips);
        } catch (IOException e) {
            showStatus("Error saving tips to file.", Color.RED);
        }
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        // Clear after 4 seconds using Swing Timer
        new javax.swing.Timer(4000, e -> statusLabel.setText(" ")).start();
    }

    // Custom cell renderer to highlight favorite tips
    static class FavoriteTipCellRenderer extends DefaultListCellRenderer {
        private static final Color favoriteColor = new Color(255, 215, 0);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String tip = value.toString();
            if (tip.startsWith(FAVORITE_MARK)) {
                label.setText(tip.substring(FAVORITE_MARK.length()));
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setForeground(favoriteColor);
            }
            return label;
        }
    }
}
