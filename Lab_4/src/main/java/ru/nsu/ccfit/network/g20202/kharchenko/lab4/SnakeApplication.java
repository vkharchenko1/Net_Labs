package ru.nsu.ccfit.network.g20202.kharchenko.lab4;

import ru.nsu.ccfit.network.g20202.kharchenko.lab4.game.SnakeGameController;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.net.SnakeNetCommunicator;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameAnnouncement;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameParameters;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeRole;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.SnakeView;

import java.io.IOException;
import java.util.*;

public class SnakeApplication {

    final String multicastAddress = "239.192.0.4";
    final int multicastPort = 9192;

    private SnakeView view;
    private SnakeGameController gameController;
    SnakeNetCommunicator communicator;

    // Game list
    HashMap<String, SnakeGameAnnouncement> gameList;
    HashMap<String, SnakeGameAnnouncement> checkList;
    Timer checkListTimer;

    public static void main(String[] args) {
        SnakeApplication app = new SnakeApplication();
        app.run();
    }

    public void run() {
        // Create view
        view = new SnakeView();
        view.showMainMenu();

        // Create network communicator
        try {
            communicator = new SnakeNetCommunicator(multicastAddress, multicastPort);
        } catch (IOException e) {
            System.out.println("Couldn't connect to socket, shutting down");
            System.exit(e.hashCode());
        }

        // Listen for announcement messages and update the list periodically
        gameList = new HashMap<>();
        checkList = new HashMap<>();
        communicator.onReceiveAnnouncement(this::addGame);
        checkListTimer = new Timer();
        checkListTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (var game: gameList.values()) {
                    if (checkList.containsKey(game.name)) {
                        view.addGameAnnouncement(game);
                    }
                    else {
                        view.removeGameAnnouncement(game);
                    }
                }
                gameList.clear();
                for (var game: checkList.values()) {
                    gameList.put(game.name, game);
                    view.addGameAnnouncement(game);
                }
                checkList.clear();
            }
        }, 0, 1100);

        // Send discover message
        communicator.sendDiscoverMessage();

        // Join game when pressing "Connect" button
        view.onPressConnect(this::joinGame);

        // Launch game when pressing "Launch" button
        view.onPressLaunch(e -> this.launchGame());

        // Stop game when pressing "Leave" button
        view.onPressLeave(e -> this.leaveGame());
    }

    private SnakeGameAnnouncement joinGame(SnakeGameAnnouncement gameAnnouncement) {
        // Launch join procedure
        communicator.joinGame(gameAnnouncement);

        // Set game parameters
        SnakeGameParameters gameParameters = gameAnnouncement.gameParameters;
        gameParameters.role = SnakeRole.NORMAL;

        // Run game contoller as NORMAL
        gameController = new SnakeGameController(view, communicator, gameParameters);
        gameController.run();

        return gameAnnouncement;
    }

    private SnakeGameAnnouncement addGame(SnakeGameAnnouncement gameAnnouncement) {
        if (!checkList.containsKey(gameAnnouncement.name))
            checkList.put(gameAnnouncement.name, gameAnnouncement);
        return gameAnnouncement;
    }

    private void leaveGame() {
        // Stop game controller
        gameController.stop();
    }

    private void launchGame() {
        // Specify gameParameters
        SnakeGameParameters gp = view.getGameParameters();
        if (!gameList.containsKey(gp.name)) {
            gp.role = SnakeRole.MASTER;
            // Run Game Controller as MASTER
            gameController = new SnakeGameController(view, communicator, gp);
            gameController.run();
        } else {
            view.showCreateErrorMsg("Game with this name already exists");
        }
    }
}