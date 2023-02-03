package ru.nsu.ccfit.network.g20202.kharchenko.lab4.game.model;

import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.Point;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeDirection;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameState;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class SnakeModel {

    final int START_SIZE = 2;

    int height;
    int width;

    int highest_id = 0;
    HashMap<Integer, SnakeCharacter> snakes;
    Vector<Point> foods;
    int foodCount;
    int stateNum;


    public SnakeModel(int width, int height, int food_static) {
        // Set field bounds
        this.width = width;
        this.height = height;

        // Set stateNum to be 0
        stateNum = 0;

        // Create list of characters
        snakes = new HashMap<>();

        // Create list of foods
        foods = new Vector<>();
        foodCount = food_static;
        spawnFoods();
    }

    private void spawnFoods() {
        boolean canSpawn = true;
        while (foods.size() < foodCount && canSpawn) {
            canSpawn = spawnRandomFood();
        }
    }

    private boolean spawnRandomFood() {
        // Possible spots
        Vector<Point> availableSpaces = new Vector<>();

        // Add all field tiles
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                availableSpaces.add(new Point(x, y));
            }
        }

        // Remove the ones that are occupied by snakes
        for (var snake: snakes.values()) {
            // Get snake's body
            Vector<Point> body = snake.getBody();

            // Acquire head
            Point currentPoint = snake.getHead();

            // Remove each tile occupied by snake's body
            for (var body_part: body) {
                if (body_part != body.firstElement())
                    currentPoint = currentPoint.add(body_part).wrap(height, width);
                availableSpaces.remove(currentPoint);
            }
        }

        // Remove the tiles occupied by foods
        for (var food: foods) {
            availableSpaces.remove(food);
        }

        // Choose random spot
        if (!availableSpaces.isEmpty()) {
            Point spawn = availableSpaces.get(new Random().nextInt(availableSpaces.size()));
            return spawnFood(spawn);
        }

        return false;
    }

    private boolean spawnFood(Point food) {
        // Remove the ones that are occupied by snakes
        for (var snake: snakes.values()) {
            Vector<Point> body = snake.getBody();
            Point currentPoint = snake.getHead();
            for (var body_part: body) {
                if (body_part != body.firstElement())
                    currentPoint = currentPoint.add(body_part).wrap(height, width);

                if (currentPoint.equals(food))
                    return false;
            }
        }

        foods.add(food);
        return true;
    }

    public int addSnake() {
        Point startPos = locateSpawn();

        if (startPos == null)
            return -1;

        // Add snake
        SnakeCharacter snake = new SnakeCharacter(highest_id, startPos, START_SIZE);
        snakes.put(highest_id, snake);

        // Spawn food
        foodCount++;
        spawnFoods();

        // Return new snake id
        return highest_id++;
    }

    public boolean canSpawn() {
        return (locateSpawn() != null);
    }

    public Point locateSpawn() {
        // Locate a free spot 5x5
        Point startPos = null;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                startPos = new Point(x, y);

                // Check each point surrounding center
                for (int a = -2; a <= 2; a++) {
                    for (int b = -2; b <= 2; b++) {
                        assert startPos != null;
                        Point p = startPos.add(new Point(a, b)).wrap(height, width);

                        // Check if the chosen area has any snakes
                        for (var snake: snakes.values()) {
                            Vector<Point> body = snake.getBody();
                            Point currentPoint = snake.getHead();

                            for (var body_part: body) {
                                if (body_part != body.firstElement())
                                    currentPoint = currentPoint.add(body_part).wrap(height, width);

                                if (p.equals(currentPoint)) {
                                    startPos = null;
                                    break;
                                }
                            }
                        }

                        if (startPos == null)
                            break;

                        // Check if chosen area has any foods
                        for (var food: foods) {
                            if (p == food) {
                                startPos = null;
                                break;
                            }
                        }
                    }
                    if (startPos == null)
                        break;
                }

                if (startPos != null)
                    break;
            }

            if (startPos != null)
                break;
        }
        return startPos;
    }

    public SnakeGameState nextState() {
        // Increase snakes / check if food was eaten
        for (var snake: snakes.values()) {
            snake.addHead(height, width);
            int foodEaten = foods.indexOf(snake.getHead());
            if (foodEaten != -1) {
                foods.remove(foodEaten);
            } else {
                snake.removeTail();
            }
        }

        // Bump each snake
        Vector<Integer> killList = new Vector<>();
        for (var snake1: snakes.values()) {
            Point head = snake1.getHead();

            for (var snake2: snakes.values()) {
                // Get snake's body
                Vector<Point> body = snake2.getBody();

                // Acquire head
                Point currentPoint = snake2.getHead();

                // Check each body part
                for (var body_part: body) {
                    if (body_part != body.firstElement())
                        currentPoint = currentPoint.add(body_part).wrap(height, width);

                    if (currentPoint.equals(head)) {
                        // If not (snake collided with itself AND snake collided with its own head)
                        if (snake1.id != snake2.id || body_part != body.firstElement()) {
                            // Kill snake
                            killList.add(snake1.id);

                            // Increase killer snake's score
                            if (snake1.id != snake2.id) {
                                snake2.addScore(1);
                            }
                        }
                    }
                }
            }
        }

        for (var killId: killList) {
            killSnake(killId);
        }

        spawnFoods();

        return new SnakeGameState(snakes, foods, stateNum++);
    }

    private void killSnake(int id) {
        SnakeCharacter snake = snakes.remove(id);
        snakes.remove(id);

        foodCount--;

        // Spawn foods with 50% chance
        Vector<Point> body = snake.getBody();
        Point currentPoint = snake.getHead();
        for (var body_part: body) {
            if (body_part != body.firstElement())
                currentPoint = currentPoint.add(body_part).wrap(height, width);

            if (1 == new Random().nextInt(2)) {
                spawnFood(currentPoint);
            }
        }
    }

    public void steerSnake(int id, SnakeDirection direction) {
        if (snakes.containsKey(id))
            snakes.get(id).steer(direction);
    }

    public SnakeGameState currentState() {
        return new SnakeGameState(snakes, foods, stateNum);
    }

    public int getScore(int id) {
        if (snakes.containsKey(id))
            return snakes.get(id).score;
        else
            return -1;
    }
}
