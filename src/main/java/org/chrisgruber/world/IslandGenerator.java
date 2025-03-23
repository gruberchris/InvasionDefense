package org.chrisgruber.world;

import java.util.Random;

public class IslandGenerator {
    private final int width;
    private final int height;
    private final Random random;

    // 0 = water, 1 = land
    private int[][] islandMap;

    public IslandGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.random = new Random();
        this.islandMap = new int[width][height];
    }

    public void generateIsland() {
        // Center of the island
        int centerX = width / 2;
        int centerY = height / 2;

        // Island radius (roughly circular)
        int baseRadius = Math.min(width, height) / 3;

        // Generate the island
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Distance from center
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

                // Add some noise to make the border irregular
                double noise = random.nextDouble() * baseRadius * 0.3;

                // If within radius, it's land
                if (distance < baseRadius + noise) {
                    islandMap[x][y] = 1; // Land
                } else {
                    islandMap[x][y] = 0; // Water
                }
            }
        }
    }

    public int[][] getIslandMap() {
        return islandMap;
    }

    public boolean isLand(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return islandMap[x][y] == 1;
    }

    public boolean isWater(int x, int y) {
        return !isLand(x, y);
    }
}