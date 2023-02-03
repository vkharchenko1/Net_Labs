package ru.nsu.ccfit.network.g20202.kharchenko.lab4.view;

import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameAnnouncement;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameParameters;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameState;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.game.SnakeGamePane;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.menu.SnakeCreateGamePane;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.menu.SnakeMenuPane;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.function.UnaryOperator;

public class SnakeView extends JFrame {

    SnakeMenuPane menuPane;
    SnakeCreateGamePane createGamePane;
    SnakeGamePane gamePane;

    public SnakeView() {
        // Set window parameters
        this.setName("Snake game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(250, 150, 1800/2, 1800/4);
        this.setFocusable(true);

        menuPane = new SnakeMenuPane();
        createGamePane = new SnakeCreateGamePane();
        gamePane = new SnakeGamePane();

        // Main menu -> Create menu
        menuPane.getCreateElement().addActionListener(e -> this.showCreateMenu());

        // Create menu -> Main menu
        createGamePane.getCancelElement().addActionListener(e -> this.showMainMenu());
    }

    public void showMainMenu() {
        this.setContentPane(menuPane);
        this.setVisible(true);
    }

    private void showCreateMenu() {
        this.setContentPane(createGamePane);
        this.setVisible(true);
    }

    public void showGame(SnakeGameParameters gameParameters) {
        if (gameParameters != null) gamePane.setParameters(gameParameters);
        this.setContentPane(gamePane);
        this.setVisible(true);
    }

    public void onPressLaunch(ActionListener actionListener) {
        createGamePane.getLaunchElement().addActionListener(actionListener);
    }

    public SnakeGameParameters getGameParameters() {
        return createGamePane.getGameParameters();
    }

    public void onPressLeave(ActionListener actionListener) {
        gamePane.getLeaveElement().addActionListener(actionListener);
    }


    public void onPressArrowKey(KeyListener keyListener) {
        this.addKeyListener(keyListener);
    }

    public void updateGameField(SnakeGameState gameState) {
        gamePane.updateGameField(gameState);
    }

    @Override
    public void setFocusable(boolean b) {
        super.setFocusable(b);
    }

    public void gameExit() {
        // Remove key listeners
        for (var kl: this.getKeyListeners()) {
            this.removeKeyListener(kl);
        }

        // Clear game panel
        gamePane.gameExit();

        // Show main menu
        showMainMenu();
    }

    public void addGameAnnouncement(SnakeGameAnnouncement gameAnnouncement) {
        menuPane.addGameAnnouncement(gameAnnouncement);
    }

    public void removeGameAnnouncement(SnakeGameAnnouncement gameAnnouncement) {
        menuPane.removeGameAnnouncement(gameAnnouncement);
    }
    public void onPressConnect(UnaryOperator<SnakeGameAnnouncement> function) {
        menuPane.onPressConnect(function);
    }

    public void showCreateErrorMsg(String s) {
        menuPane.showCreateErrorMsg(s);
    }
}
