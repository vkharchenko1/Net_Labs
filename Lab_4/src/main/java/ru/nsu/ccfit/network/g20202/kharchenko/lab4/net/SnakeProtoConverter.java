package ru.nsu.ccfit.network.g20202.kharchenko.lab4.net;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.game.model.SnakeCharacter;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.*;
import me.ippolitov.fit.snakes.SnakesProto.*;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class SnakeProtoConverter {

    public enum MessageType {
        NONE,
        ANNOUNCEMENT,
        DISCOVER,
        JOIN,
        STEER,
        STATE
    }

    public static MessageType readGameMessageType(byte[] data) throws InvalidProtocolBufferException {
        GameMessage gameMessage = GameMessage.parseFrom(data);

        if (gameMessage.hasAnnouncement()) {
            return MessageType.ANNOUNCEMENT;
        } else if (gameMessage.hasDiscover()) {
            return MessageType.DISCOVER;
        } else if (gameMessage.hasJoin()) {
            return MessageType.JOIN;
        } else if (gameMessage.hasState()) {
            return MessageType.STATE;
        } else if (gameMessage.hasSteer()) {
            return MessageType.STEER;
        } else {
            return MessageType.NONE;
        }
    }

    public static byte[] writeDiscoverMessage(int msg_seq) {
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgSeq(msg_seq)
                .setDiscover(GameMessage.DiscoverMsg.newBuilder().build())
                .build();

        return gameMessage.toByteArray();
    }

    private static SnakeRole protoToRole(NodeRole role) {
        switch (role) {
            case MASTER: return SnakeRole.MASTER;
            case VIEWER: return SnakeRole.VIEWER;
            case DEPUTY: return SnakeRole.DEPUTY;
            case NORMAL: return SnakeRole.NORMAL;
        }
        return null;
    }

    public static SnakeGameAnnouncement readGameAnnouncement(byte[] data) throws InvalidProtocolBufferException {
        GameMessage.AnnouncementMsg protoMessage = GameMessage.parseFrom(data).getAnnouncement();
        int count = protoMessage.getGamesCount();

        SnakeGameAnnouncement snakeAnnouncement = new SnakeGameAnnouncement();
        if (count > 0) {
            GameAnnouncement protoAnnouncement = protoMessage.getGames(0);

            // Name
            snakeAnnouncement.name = protoAnnouncement.getGameName();

            // Can join
            if (protoAnnouncement.hasCanJoin())
                snakeAnnouncement.canJoin = protoAnnouncement.getCanJoin();

            // Players
            snakeAnnouncement.players = readPlayers(protoAnnouncement.getPlayers());

            // Config
            GameConfig gameConfig = protoAnnouncement.getConfig();

            snakeAnnouncement.gameParameters = new SnakeGameParameters(
                        snakeAnnouncement.name,
                        gameConfig.getHeight(),
                        gameConfig.getWidth(),
                        gameConfig.getStateDelayMs(),
                        gameConfig.getFoodStatic()
                    );
        }
        return snakeAnnouncement;
    }

    private static HashMap<Integer, SnakePlayer> readPlayers(GamePlayers gamePlayers) {
        HashMap<Integer, SnakePlayer> players = new HashMap<>();
        for (var protoPlayer: gamePlayers.getPlayersList()) {
            SnakePlayer player = new SnakePlayer();
            player.id = protoPlayer.getId();
            player.score = protoPlayer.getScore();
            player.playerType = 1;
            player.role = protoToRole(protoPlayer.getRole());
            player.name = protoPlayer.getName();
            players.put(player.id, player);
        }
        return players;
    }


    private static GamePlayers writeGamePlayers(HashMap<Integer, SnakePlayer> snakePlayers) {
        GamePlayers gamePlayers = GamePlayers.newBuilder().buildPartial();
        for (var player: snakePlayers.entrySet()) {
            GamePlayer gamePlayer = GamePlayer.newBuilder()
                    .setId(player.getValue().id)
                    .setName(player.getValue().name)
                    .setRole(roleToProto(player.getValue().role))
                    .setScore(player.getValue().score)
                    .setType(PlayerType.HUMAN)
                    .build();
            gamePlayers.toBuilder().addPlayers(gamePlayer).buildPartial();
        }
        gamePlayers.toBuilder().build();
        return gamePlayers;
    }

    public static byte[] writeAnnouncementMessage(int msg_seq, SnakeGameAnnouncement snakeGameAnnouncement) {
        // Players
        GamePlayers gamePlayers = writeGamePlayers(snakeGameAnnouncement.players);

        SnakeGameParameters gameParameters = snakeGameAnnouncement.gameParameters;
        GameConfig gameConfig = GameConfig.newBuilder()
                .setWidth(gameParameters.width)
                .setHeight(gameParameters.height)
                .setFoodStatic(gameParameters.foodStatic)
                .setStateDelayMs(gameParameters.delay)
                .build();

        GameAnnouncement gameAnnouncement = GameAnnouncement.newBuilder()
                .setPlayers(gamePlayers)
                .setConfig(gameConfig)
                .setCanJoin(snakeGameAnnouncement.canJoin)
                .setGameName(snakeGameAnnouncement.name)
                .build();

        GameMessage.AnnouncementMsg announcementMsg = GameMessage.AnnouncementMsg.newBuilder()
                .addGames(gameAnnouncement)
                .build();

        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgSeq(msg_seq)
                .setAnnouncement(announcementMsg)
                .build();

        return gameMessage.toByteArray();
    }

    private static NodeRole roleToProto(SnakeRole role) {
        switch (role) {
            case MASTER: return NodeRole.MASTER;
            case VIEWER: return NodeRole.VIEWER;
            case DEPUTY: return NodeRole.DEPUTY;
            case NORMAL: return NodeRole.NORMAL;
        }
        return null;
    }

    public static byte[] writeJoinMessage(int msg_seq, String gameName) {
        GameMessage.JoinMsg joinMsg = GameMessage.JoinMsg.newBuilder()
                .setPlayerType(PlayerType.HUMAN)
                .setRequestedRole(NodeRole.NORMAL)
                .setGameName(gameName)
                .setPlayerName("Player " + new Random().nextInt(500))
                .build();

        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgSeq(msg_seq)
                .setJoin(joinMsg)
                .build();

        return gameMessage.toByteArray();
    }

    public static SnakeJoinRequest readJoinMsg(byte[] data) throws InvalidProtocolBufferException {
        GameMessage.JoinMsg protoMessage = GameMessage.parseFrom(data).getJoin();
        SnakeJoinRequest joinRequest = new SnakeJoinRequest();
        joinRequest.gameName = protoMessage.getGameName();
        joinRequest.playerName = protoMessage.getPlayerName();
        if (protoMessage.hasRequestedRole()) {
            switch (protoMessage.getRequestedRole()) {
                case NORMAL:
                    joinRequest.role = SnakeRole.NORMAL;
                    break;
                case VIEWER:
                    joinRequest.role = SnakeRole.VIEWER;
                    break;
                default:
                    joinRequest.role = SnakeRole.NONE;
                    break;
            }
        } else {
            joinRequest.role = SnakeRole.NONE;
        }
        return joinRequest;
    }


    public static SnakeGameState readStateMsg(byte[] data) throws InvalidProtocolBufferException {
        GameState protoState = GameMessage.parseFrom(data).getState().getState();

        // Get state number
        int stateNumber = protoState.getStateOrder();

        // Get snakes
        HashMap<Integer, SnakeCharacter> snakes = new HashMap<>();
        for (var snake: protoState.getSnakesList()) {
            // Get body
            Vector<Point> body = new Vector<>();
            for (var p: snake.getPointsList()) {
                Point point = snakePointFromProto(p);
                body.add(point);
            }

            SnakeCharacter.SnakeState state = snakeStateFromProto(snake.getState());
            int id = snake.getPlayerId();
            SnakeDirection direction = snakeDirectionFromProto(snake.getHeadDirection());

            SnakeCharacter snakeCharacter = new SnakeCharacter();
            snakeCharacter.id = id;
            snakeCharacter.state = state;
            snakeCharacter.body = body;
            snakeCharacter.direction = direction;
            snakes.put(id, snakeCharacter);
        }

        // Get foods
        Vector<Point> foods = new Vector<>();
        for (var food: protoState.getFoodsList()) {
            foods.add(snakePointFromProto(food));
        }

        return new SnakeGameState(snakes, foods, stateNumber);
    }

    private static SnakeDirection snakeDirectionFromProto(Direction headDirection) {
        switch (headDirection) {
            case UP: return SnakeDirection.UP;
            case DOWN: return SnakeDirection.DOWN;
            case LEFT: return SnakeDirection.LEFT;
            case RIGHT: return SnakeDirection.RIGHT;
        }
        return SnakeDirection.UP;
    }

    private static SnakeCharacter.SnakeState snakeStateFromProto(GameState.Snake.SnakeState state) {
        switch (state) {
            case ALIVE: return SnakeCharacter.SnakeState.ALIVE;
            case ZOMBIE: return SnakeCharacter.SnakeState.ZOMBIE;
        }
        return SnakeCharacter.SnakeState.ZOMBIE;
    }

    private static Point snakePointFromProto(GameState.Coord p) {
        return new Point(p.getX(), p.getY());
    }

    public static byte[] writeStateMessage(int meg_seq, SnakeGameState gameState, HashMap<Integer, SnakePlayer> peers) {
        // Include state number
        GameState protoState = GameState.newBuilder()
                .setStateOrder(gameState.getOrder())
                .buildPartial();

        // Add snakes
        for (var snake: gameState.getSnakes().values()) {
            GameState.Snake protoSnake = GameState.Snake.newBuilder()
                    .setState(snakeStateToProto(snake.getState()))
                    .setHeadDirection(snakeDirectionToProto(snake.getDirection()))
                    .setPlayerId(snake.id)
                    .build();
            for (var p: snake.getBody()) {
                protoSnake = protoSnake.toBuilder().addPoints(snakePointToProto(p)).build();
            }
            protoState = protoState.toBuilder().addSnakes(protoSnake).buildPartial();
        }

        // Add foods
        for (var p: gameState.getFoods()) {
            protoState = protoState.toBuilder().addFoods(snakePointToProto(p)).buildPartial();
        }

        // Add players
        protoState = protoState.toBuilder().setPlayers(writeGamePlayers(peers)).buildPartial();

        // Enclose in state message
        GameMessage.StateMsg stateMsg = GameMessage.StateMsg.newBuilder()
                .setState(protoState)
                .build();

        // Enclose in game message
        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgSeq(meg_seq)
                .setState(stateMsg)
                .build();


        return gameMessage.toByteArray();
    }

    private static GameState.Coord snakePointToProto(Point p) {
        return GameState.Coord.newBuilder()
                .setX(p.x)
                .setY(p.y)
                .build();
    }

    private static Direction snakeDirectionToProto(SnakeDirection direction) {
        switch (direction) {
            case UP: return Direction.UP;
            case DOWN: return Direction.DOWN;
            case LEFT: return Direction.LEFT;
            case RIGHT: return Direction.RIGHT;
        }
        return Direction.UP;
    }

    private static GameState.Snake.SnakeState snakeStateToProto(SnakeCharacter.SnakeState state) {
        switch (state) {
            case ALIVE: return GameState.Snake.SnakeState.ALIVE;
            case ZOMBIE: return GameState.Snake.SnakeState.ZOMBIE;
        }
        return GameState.Snake.SnakeState.ZOMBIE;
    }

    public static byte[] writeSteerMsg(int msg_seq, SnakeDirection dir) {
        GameMessage.SteerMsg steerMsg = GameMessage.SteerMsg.newBuilder()
                .setDirection(snakeDirectionToProto(dir))
                .build();

        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgSeq(msg_seq)
                .setSteer(steerMsg)
                .build();

        return gameMessage.toByteArray();
    }


    public static SnakeSteer readSteerMsg(byte[] data) throws InvalidProtocolBufferException {
        GameMessage.SteerMsg steerMsg = GameMessage.parseFrom(data).getSteer();

        SnakeSteer steer = new SnakeSteer();
        steer.direction = snakeDirectionFromProto(steerMsg.getDirection());
        return steer;
    }



}
