package ru.nsu.ccfit.network.g20202.kharchenko.lab4.game;

import ru.nsu.ccfit.network.g20202.kharchenko.lab4.game.model.SnakeModel;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.net.SnakeNetCommunicator;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.*;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.SnakeView;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SnakeGameController {

    // For all roles
    SnakeRole role;
    SnakeView view;
    SnakeNetCommunicator communicator;
    SnakeGameParameters gameParameters;
    int selfID;

    // If MASTER
    SnakeModel model;
    Timer updateTimer;
    Timer announceTimer;
    HashMap<Integer, SnakePlayer> peers;

    // Create controller
    public SnakeGameController(SnakeView view, SnakeNetCommunicator communicator, SnakeGameParameters gameParameters) {
        this.view = view;
        this.communicator = communicator;
        this.gameParameters = new SnakeGameParameters(gameParameters);
        this.role = this.gameParameters.role;

        view.showGame(this.gameParameters);
    }

    public void run() {
        if (role == SnakeRole.MASTER) {
            // Create model
            model = new SnakeModel(gameParameters.width, gameParameters.height, gameParameters.foodStatic);
            selfID = model.addSnake();

            // Create peer list
            peers = new HashMap<>();

            // Create update timer
            updateTimer = new Timer("Game Update");
            int updatePeriod = gameParameters.delay;
            updateTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() { updateGameState();
                }
            }, updatePeriod, updatePeriod);

            // Create announce timer
            announceTimer = new Timer();
            announceTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    announcePresence();
                }
            }, 0, 1000);

            // Assign operation on join request
            communicator.onJoinRequest(this::joinPeer);

            // Assign operation on steer request
            communicator.onSteerGet(this::steerPeer);

            // Show initial state
            SnakeGameState gameState = model.currentState();
            view.updateGameField(gameState);
        } else if (role == SnakeRole.NORMAL || role == SnakeRole.DEPUTY) {
            // Assign operation on state message
            communicator.onStateGet(this::updateGameState);
        }

        // Assign steer actions
        view.onPressArrowKey(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    steerSnake(selfID, SnakeDirection.RIGHT);
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    steerSnake(selfID, SnakeDirection.LEFT);
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    steerSnake(selfID, SnakeDirection.UP);
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    steerSnake(selfID, SnakeDirection.DOWN);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private SnakeSteer steerPeer(SnakeSteer steer) {
        int peer_id = -1;
        for (var peer: peers.entrySet()) {
            if (peer.getValue().address.equals(steer.address) && peer.getValue().port == steer.port) {
                peer_id = peer.getKey();
            }
        }

        if (peer_id != -1)
            steerSnake(peer_id, steer.direction);

        return steer;
    }

    private SnakeJoinRequest joinPeer(SnakeJoinRequest joinRequest) {
        int peerId = model.addSnake();

        SnakePlayer player = new SnakePlayer();
        player.id = peerId;
        player.name = joinRequest.playerName;
        player.address = joinRequest.senderAddress;
        player.port = joinRequest.senderPort;
        player.playerType = joinRequest.playerType;
        player.role = joinRequest.role;
        player.score = 0;

        peers.put(peerId, player);

        return joinRequest;
    }

    public void stop() {
        if (role == SnakeRole.MASTER) {
            updateTimer.cancel();
            announceTimer.cancel();
        }
        communicator.destroyFunctions();
        view.gameExit();
    }

    private void updateGameState() {
        SnakeGameState gameState = model.nextState();
        view.updateGameField(gameState);

        // Collect scores
        for (var id: peers.keySet()) {
            peers.get(id).score = model.getScore(id);
        }

        // Send next state through communicator
        for (var peer: peers.values()) {
            communicator.sendGameState(peer, gameState, peers);
        }
    }

    private SnakeGameState updateGameState(SnakeGameState gameState) {
        view.updateGameField(gameState);
        return gameState;
    }

    private void steerSnake(int id, SnakeDirection dir) {
        if (role == SnakeRole.MASTER)
            model.steerSnake(id, dir);
        else if (role == SnakeRole.NORMAL || role == SnakeRole.DEPUTY)
            communicator.sendSteerMsg(dir);
    }

    private void announcePresence() {
        SnakeGameAnnouncement gameAnnouncement = new SnakeGameAnnouncement();
        gameAnnouncement.name = gameParameters.name;
        gameAnnouncement.canJoin = model.canSpawn();
        gameAnnouncement.gameParameters = this.gameParameters;
        gameAnnouncement.players = new HashMap<>(peers);

        communicator.sendAnnouncementMsg(gameAnnouncement);
    }
}
