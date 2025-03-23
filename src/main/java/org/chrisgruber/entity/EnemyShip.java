package org.chrisgruber.entity;

import static org.lwjgl.opengl.GL11.*;

public class EnemyShip extends Entity {
    private float health;
    private float speed;
    private float[] targetPosition;

    public EnemyShip(float x, float y) {
        super(x, y, 0.15f, 0.15f);
        this.health = 20f;
        this.speed = 0.05f;
        this.targetPosition = new float[]{0, 0}; // Default target
    }

    @Override
    public void update(float deltaTime) {
        // Simple movement toward target
        float dx = targetPosition[0] - x;
        float dy = targetPosition[1] - y;
        float distance = (float) Math.sqrt(dx*dx + dy*dy);

        if (distance > 0.01f) {
            float moveX = dx / distance * speed * deltaTime;
            float moveY = dy / distance * speed * deltaTime;
            x += moveX;
            y += moveY;
        }

        // Check if health is zero
        if (health <= 0) {
            active = false;
        }
    }

    @Override
    public void render() {
        // Scales size of the ship
        float scaleFactor = 0.7f; // Reduce size by 30%
        float scaledWidth = width * scaleFactor;
        float scaledHeight = height * scaleFactor;

        // Draw the ship as a triangle
        glBegin(GL_TRIANGLES);
        glColor3f(0.9f, 0.1f, 0.1f); // Red color

        // Ship pointing in the direction of movement
        float angle = (float) Math.atan2(targetPosition[1] - y, targetPosition[0] - x);
        float x1 = x + (float) Math.cos(angle) * scaledWidth;
        float y1 = y + (float) Math.sin(angle) * scaledHeight;
        float x2 = x + (float) Math.cos(angle + 2.5f) * scaledWidth;
        float y2 = y + (float) Math.sin(angle + 2.5f) * scaledHeight;
        float x3 = x + (float) Math.cos(angle - 2.5f) * scaledWidth;
        float y3 = y + (float) Math.sin(angle - 2.5f) * scaledHeight;

        glVertex2f(x1, y1);
        glVertex2f(x2, y2);
        glVertex2f(x3, y3);
        glEnd();
    }

    public void setTargetPosition(float x, float y) {
        this.targetPosition[0] = x;
        this.targetPosition[1] = y;
    }

    public void takeDamage(float damage) {
        this.health -= damage;

        System.out.println("Enemy ship took " + damage + " damage! Health remaining: " + this.health);

        if (this.health <= 0) {
            this.health = 0;
            this.active = false;
            System.out.println("Enemy ship destroyed!");
        }
    }
}
