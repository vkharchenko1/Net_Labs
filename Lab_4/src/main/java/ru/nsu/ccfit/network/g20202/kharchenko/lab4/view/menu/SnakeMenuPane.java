package ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.menu;

import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameAnnouncement;

import javax.swing.*;
import java.awt.*;
import java.util.function.UnaryOperator;

public class SnakeMenuPane extends JPanel {

    //SnakeServerMenuPane serverMenu;
    SnakeLocalMenuPane localMenu;

    public SnakeMenuPane() {
        super(new GridLayout(1, 2, 10, 10));

        // Add pane with info about local games
        localMenu = new SnakeLocalMenuPane();
        this.add(localMenu);
    }

    public JButton getCreateElement() {
        return localMenu.getCreateButton();
    }

    public void addGameAnnouncement(SnakeGameAnnouncement gameAnnouncement) {
        localMenu.addGameAnnouncement(gameAnnouncement);
    }

    public void removeGameAnnouncement(SnakeGameAnnouncement gameAnnouncement) {
        localMenu.removeGameAnnouncement(gameAnnouncement);
    }
    public void onPressConnect(UnaryOperator<SnakeGameAnnouncement> function) {
        localMenu.onPressConnect(function);
    }

    public void showCreateErrorMsg(String s) {
        localMenu.showCreateErrorMsg(s);
    }
}
