package org.chrisgruber.input;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private final long window;

    // Movement states
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean shooting = false;

    public InputHandler(long window) {
        this.window = window;
    }

    public void update() {
        // Update movement states based on key states
        moveLeft = glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS ||
                glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS;

        moveRight = glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS ||
                glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS;

        moveUp = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS ||
                glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS;

        moveDown = glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS ||
                glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS;

        shooting = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
    }

    public boolean isMoveLeft() {
        return moveLeft;
    }

    public boolean isMoveRight() {
        return moveRight;
    }

    public boolean isMoveUp() {
        return moveUp;
    }

    public boolean isMoveDown() {
        return moveDown;
    }

    public boolean isShooting() {
        return shooting;
    }
}