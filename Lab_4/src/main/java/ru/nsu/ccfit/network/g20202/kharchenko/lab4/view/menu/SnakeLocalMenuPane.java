package ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.menu;

import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameAnnouncement;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameParameters;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.UnaryOperator;

public class SnakeLocalMenuPane extends JPanel {

    HashMap<String, SnakeGameAnnouncement> gameList;
    JList<String> gameListView;
    DefaultListModel<String> gameListNames;
    JButton connectButton;
    JButton createButton;

    SnakeLocalMenuPane() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create panel label
        JLabel paneName = new JLabel("Local games");

        // Create list of games
        gameList = new HashMap<>();
        gameListNames = new DefaultListModel<>();
        gameListView = new JList<>(gameListNames);
        gameListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create "Connect" button
        connectButton = new JButton("Connect");

        // Create "Create new game" button
        createButton = new JButton("Create new game");

        // Set element alignments
        paneName.setAlignmentX(Component.LEFT_ALIGNMENT);
        gameListView.setAlignmentX(Component.LEFT_ALIGNMENT);
        connectButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        createButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Set element sizes
        paneName.setFont(new Font(paneName.getFont().getName(), paneName.getFont().getStyle(), 20));

        // Set element borders
        paneName.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gameListView.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add all elements
        this.add(paneName, BorderLayout.PAGE_START);
        this.add(gameListView, BorderLayout.CENTER);
        this.add(connectButton, BorderLayout.PAGE_END);
        this.add(createButton, BorderLayout.PAGE_END);
    }

    public JButton getCreateButton() {
        return createButton;
    }

    public void addGameAnnouncement(SnakeGameAnnouncement gameAnnouncement) {
        if (!gameList.containsKey(gameAnnouncement.name)) {
            gameList.put(gameAnnouncement.name, gameAnnouncement);
            gameListNames.addElement(gameAnnouncement.name);
            gameListView.setVisible(true);
        }
    }

    public void removeGameAnnouncement(SnakeGameAnnouncement gameAnnouncement) {
        gameList.remove(gameAnnouncement.name);
        gameListNames.removeElement(gameAnnouncement.name);
        gameList.remove(gameAnnouncement.name);
    }

    public void onPressConnect(UnaryOperator<SnakeGameAnnouncement> function) {
        connectButton.addActionListener(e -> {
            if (gameListView.getSelectedIndex() >= 0) {
                SnakeGameAnnouncement gameAnnouncement = gameList.get(gameListNames.get(gameListView.getMinSelectionIndex()));
                function.apply(gameAnnouncement);
            }
        });
    }

    public void showCreateErrorMsg(String s) {
        System.out.println(s);
    }

}
