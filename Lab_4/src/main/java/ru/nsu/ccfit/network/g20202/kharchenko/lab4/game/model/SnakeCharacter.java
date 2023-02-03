package ru.nsu.ccfit.network.g20202.kharchenko.lab4.game.model;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.Point;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeDirection;

import java.util.HashMap;
import java.util.Vector;

public class SnakeCharacter {

    final HashMap<SnakeDirection, Point> dirToShift = new HashMap<>() {{
        put(SnakeDirection.UP, new Point(-1, 0));
        put(SnakeDirection.DOWN, new Point(1, 0));
        put(SnakeDirection.LEFT, new Point(0, -1));
        put(SnakeDirection.RIGHT, new Point(0, 1));
    }};

    public enum SnakeState {
        ALIVE,
        ZOMBIE
    }

    public int id;
    public int score;
    public int length;
    public SnakeDirection direction;
    public SnakeState state = SnakeState.ALIVE;
    public Vector<Point> body = new Vector<>();

    public SnakeCharacter() {}

    public SnakeCharacter(int id, Point head, int size) {
        this.id = id;
        length = size;
        direction = SnakeDirection.randomDirection();

        // Create body
        body.add(head);

        // Elongate body
        for (int i = 0; i < size - 1; i++) {
            body.add(new Point(dirToShift.
                    get(direction)
                    .opposite()));
        }
    }

    public Vector<Point> getBody() {
        return new Vector<>(body);
    }

    public Point getHead() {
        return new Point(body.firstElement());
    }

    public void addHead(int height, int width) {
        Point newHeadPos = body.get(0)
                .add(dirToShift.get(direction))
                .wrap(height, width);

        body.setElementAt(dirToShift.get(direction).opposite(), 0);
        body.add(0, newHeadPos);
    }


    public void removeTail() {
        body.removeElementAt(body.size() - 1);
    }

    public void steer(SnakeDirection dir) {
        if (!body.get(1).equals(dirToShift.get(dir))) {
            direction = dir;
        }
    }

    public void addScore(int score) {
        this.score += score;
    }


    public SnakeState getState() {
        return state;
    }

    public SnakeDirection getDirection() {
        return direction;
    }
}
