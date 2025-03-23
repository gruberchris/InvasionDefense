package org.chrisgruber.entity;

import static org.lwjgl.opengl.GL11.*;

public class Projectile extends Entity {
    private float targetX, targetY;
    private float damage;
    private float speed;
    private boolean friendly; // True if shot by player, false if shot by enemy
    private float lifetime = 4.0f; // Maximum lifetime in seconds to prevent stray projectiles

    public Projectile(float startX, float startY, float targetX, float targetY, float damage, float size, float speed, boolean friendly) {
        super(startX, startY, size, size);
        this.targetX = targetX;
        this.targetY = targetY;
        this.damage = damage;
        this.speed = speed;
        this.friendly = friendly;
    }

    @Override
    public void update(float deltaTime) {
        // Move toward target
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx*dx + dy*dy);

        // If very close to target, deactivate
        if (distance < 0.05f) {
            this.active = false;
            return;
        }

        // Update position
        if (distance > 0) {
            x += dx / distance * speed * deltaTime;
            y += dy / distance * speed * deltaTime;
        }

        // Reduce lifetime
        lifetime -= deltaTime;
        if (lifetime <= 0) {
            this.active = false;
        }
    }

    @Override
    public void render() {
        // Draw the projectile as a small glowing circle
        if (friendly) {
            // Green energy ball for player towers
            drawEnergyBall(0.2f, 0.9f, 0.3f);
        } else {
            // Red energy ball for enemies
            drawEnergyBall(0.9f, 0.2f, 0.2f);
        }
    }

    private void drawEnergyBall(float r, float g, float b) {
        // Outer glow
        glBegin(GL_TRIANGLE_FAN);
        glColor4f(r, g, b, 0.0f); // Transparent outer edge

        // Center point
        glColor4f(r, g, b, 0.9f); // Opaque center
        glVertex2f(x, y);

        // Outer points
        float outerSize = width * 1.5f;
        int segments = 12;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            glColor4f(r, g, b, 0.0f); // Transparent edge
            glVertex2f(
                    x + (float) Math.cos(angle) * outerSize,
                    y + (float) Math.sin(angle) * outerSize
            );
        }
        glEnd();

        // Inner solid part
        glBegin(GL_TRIANGLE_FAN);
        glColor4f(r * 1.2f, g * 1.2f, b * 1.2f, 0.9f); // Brighter center
        glVertex2f(x, y);

        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            glColor4f(r, g, b, 0.8f);
            glVertex2f(
                    x + (float) Math.cos(angle) * width / 2,
                    y + (float) Math.sin(angle) * width / 2
            );
        }
        glEnd();
    }

    public float getDamage() {
        return damage;
    }

    public boolean isFriendly() {
        return friendly;
    }
}