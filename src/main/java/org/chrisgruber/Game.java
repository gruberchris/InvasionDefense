package org.chrisgruber;

import org.chrisgruber.entity.EnemyShip;
import org.chrisgruber.entity.Entity;
import org.chrisgruber.entity.Projectile;
import org.chrisgruber.entity.Tower;
import org.chrisgruber.input.InputHandler;
import org.chrisgruber.world.IslandGenerator;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {
    // Window properties
    private final int WIDTH = 1024;
    private final int HEIGHT = 768;
    private final String TITLE = "Invasion Defense";

    // Game world properties
    private final int GRID_SIZE = 100;
    private IslandGenerator islandGenerator;

    // Game entities
    private List<Entity> entities = new ArrayList<>();
    private Random random = new Random();
    private float spawnTimer = 0;
    private final float SPAWN_INTERVAL = 10.0f; // Spawn enemy every 5 seconds

    // The window handle
    private long window;

    // Game loop variables
    private boolean running = false;
    private float deltaTime = 0;
    private float lastFrameTime = 0;

    // Input handler
    private InputHandler inputHandler;

    // Temporary list for new entities during update
    private List<Entity> entitiesToAdd = new ArrayList<>();
    private boolean isUpdating = false;

    public void run() {
        System.out.println("LWJGL Version: " + Version.getVersion());

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup key callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        // Setup mouse click callback
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);

                // Convert screen coordinates to world coordinates (-1 to 1)
                float worldX = (float)(xpos[0] / WIDTH) * 2 - 1;
                float worldY = (float)(1 - ypos[0] / HEIGHT) * 2 - 1;

                // Place a tower at click position
                placeTower(worldX, worldY);
            }
        });

        inputHandler = new InputHandler(window);

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // Get the window size
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Initialize game world
        islandGenerator = new IslandGenerator(GRID_SIZE, GRID_SIZE);
        islandGenerator.generateIsland();

        // Add initial towers
        Tower initialTower = new Tower(0, 0, this);
        entities.add(initialTower);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.2f, 0.4f, 0.0f); // Dark blue for water

        running = true;
        lastFrameTime = (float)glfwGetTime();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key
        while (running && !glfwWindowShouldClose(window)) {
            // Calculate delta time
            float currentTime = (float)glfwGetTime();
            deltaTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;

            // Update game state
            update(deltaTime);

            // Render game
            render();

            // Swap the color buffers
            glfwSwapBuffers(window);

            // Poll for window events
            glfwPollEvents();
        }
    }

    private void update(float deltaTime) {
        // Update input state
        inputHandler.update();

        // Update spawn timer
        spawnTimer += deltaTime;
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnEnemy();
            spawnTimer = 0;
        }

        // Mark that we're updating
        isUpdating = true;
        entitiesToAdd.clear();

        // Update all entities
        Iterator<Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            entity.update(deltaTime);

            // Remove inactive entities
            if (!entity.isActive()) {
                iterator.remove();
            }
        }

        // No longer updating, add any new entities
        isUpdating = false;
        entities.addAll(entitiesToAdd);
        entitiesToAdd.clear();

        // Check for collisions
        checkCollisions();
    }

    private void render() {
        // Clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Render island
        renderIsland();

        // Render all entities
        for (Entity entity : entities) {
            entity.render();
        }
    }

    private void renderIsland() {
        // Scale to fit the grid in the OpenGL coordinate system (-1 to 1)
        float scale = 1.8f / GRID_SIZE;

        // Render the island map
        glBegin(GL_QUADS);
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                float worldX = (x - GRID_SIZE/2) * scale;
                float worldY = (y - GRID_SIZE/2) * scale;

                if (islandGenerator.isLand(x, y)) {
                    // Land color (green)
                    glColor3f(0.2f, 0.6f, 0.2f);
                    glVertex2f(worldX, worldY);
                    glVertex2f(worldX + scale, worldY);
                    glVertex2f(worldX + scale, worldY + scale);
                    glVertex2f(worldX, worldY + scale);
                }
            }
        }
        glEnd();
    }

    private void placeTower(float x, float y) {
        // Check if position is on land (approximate conversion)
        int gridX = (int)((x + 1) / 1.8f * GRID_SIZE + GRID_SIZE/2);
        int gridY = (int)((y + 1) / 1.8f * GRID_SIZE + GRID_SIZE/2);

        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
            if (islandGenerator.isLand(gridX, gridY)) {
                Tower tower = new Tower(x, y, this);
                entities.add(tower);
                System.out.println("Tower placed at: " + x + ", " + y);
            } else {
                System.out.println("Cannot place tower on water!");
            }
        }
    }

    private void spawnEnemy() {
        // Spawn at random position at the edge of the screen
        float x, y;
        if (random.nextBoolean()) {
            // Spawn on left or right edge
            x = random.nextBoolean() ? -0.9f : 0.9f;
            y = random.nextFloat() * 1.8f - 0.9f;
        } else {
            // Spawn on top or bottom edge
            x = random.nextFloat() * 1.8f - 0.9f;
            y = random.nextBoolean() ? -0.9f : 0.9f;
        }

        EnemyShip ship = new EnemyShip(x, y);

        // Target the center of the island
        ship.setTargetPosition(0, 0);

        entities.add(ship);
        System.out.println("Enemy ship spawned at: " + x + ", " + y);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity entity) {
        if (isUpdating) {
            entitiesToAdd.add(entity);
        } else {
            entities.add(entity);
        }
    }

    private void checkCollisions() {
        for (Entity entity1 : entities) {
            // Skip if the entity is not active
            if (!entity1.isActive()) continue;

            // If entity1 is a projectile
            if (entity1 instanceof Projectile) {
                Projectile projectile = (Projectile) entity1;

                for (Entity entity2 : entities) {
                    // Skip if the same entity or entity2 is not active
                    if (entity1 == entity2 || !entity2.isActive()) continue;

                    // Check if projectile hit appropriate target
                    if (projectile.isFriendly() && entity2 instanceof EnemyShip) {
                        if (checkCollision(projectile, entity2)) {
                            // Apply damage to enemy
                            ((EnemyShip) entity2).takeDamage(projectile.getDamage());
                            // Deactivate projectile
                            projectile.setActive(false);
                        }
                    }
                    // Could add enemy projectiles hitting player units here
                }
            }
        }
    }

    private boolean checkCollision(Entity a, Entity b) {
        float ax = a.getX();
        float ay = a.getY();
        float aw = a.getWidth();
        float ah = a.getHeight();

        float bx = b.getX();
        float by = b.getY();
        float bw = b.getWidth();
        float bh = b.getHeight();

        // Simple axis-aligned bounding box collision
        return (ax - aw/2 < bx + bw/2 &&
                ax + aw/2 > bx - bw/2 &&
                ay - ah/2 < by + bh/2 &&
                ay + ah/2 > by - bh/2);
    }

    public static void main(String[] args) {
        new Game().run();
    }
}