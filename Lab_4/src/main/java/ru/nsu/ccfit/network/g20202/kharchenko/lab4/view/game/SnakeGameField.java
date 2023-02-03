package ru.nsu.ccfit.network.g20202.kharchenko.lab4.view.game;

import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.Point;
import ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils.SnakeGameState;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Vector;

public class SnakeGameField extends JPanel {

    int tileRows;
    int tileCols;
    HashMap<Point, JPanel> tiles = new HashMap<>();
    HashMap<Integer, Color> snakeColors = new HashMap<>();

    public void setDimensions(int height, int width) {
        this.removeAll();
        tiles.clear();

        this.setLayout(new GridLayout(height, width, 1, 1));

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                JPanel tile = new JPanel();

                tile.setBackground(Color.WHITE);

                tiles.put(new Point(x, y), tile);
                this.add(tile);
            }
        }

        this.tileRows = height;
        this.tileCols = width;
    }

    public void updateGameField(SnakeGameState gameState) {
        // Reset game field
        for (int x = 0; x < tileRows; x++) {
            for (int y = 0; y < tileCols; y++) {
                JPanel tile = tiles.get(new Point(x, y));

                tile.setBackground(Color.WHITE);
            }
        }

        // Draw foods
        for (var food: gameState.getFoods()) {
            JPanel tile = tiles.get(food);

            tile.setBackground(Color.RED);
        }

        // Draw snakes
        for (var snake: gameState.getSnakes().values()) {
            if (!snakeColors.containsKey(snake.id)) {
                float hue = (float)Math.random();
                int rgb = Color.HSBtoRGB(hue,1f,0.8f);
                Color color = new Color(rgb);
                snakeColors.put(snake.id, color);
            }

            Vector<Point> body = snake.getBody();

            // Acquire head
            Point currentPoint = snake.getHead();

            // Draw body
            for (var body_part: body) {
                if (body_part != body.firstElement())
                    currentPoint = currentPoint.add(body_part).wrap(tileRows, tileCols);

                JPanel tile = tiles.get(currentPoint);

                tile.setBackground(snakeColors.get(snake.id));
            }
        }
    }

    public void gameExit() {
        snakeColors.clear();
        tiles.clear();
    }
}
