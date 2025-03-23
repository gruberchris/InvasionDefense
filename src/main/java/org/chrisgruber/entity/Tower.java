package org.chrisgruber.entity;

import org.chrisgruber.Game;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Tower extends Entity {
    private float attackRange;
    private float attackDamage;
    private float attackCooldown;
    private float currentCooldown = 0f;
    private Game game; // Reference to the game for accessing entities and creating projectiles

    public Tower(float x, float y, Game game) {
        super(x, y, 0.1f, 0.1f);
        this.attackRange = 0.5f;
        this.attackDamage = 10f;
        this.attackCooldown = 3f; // 2 seconds between attacks
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        // Decrease current cooldown
        if (currentCooldown > 0) {
            currentCooldown -= deltaTime;
        }

        // If we can attack, find the closest enemy
        if (canAttack()) {
            Entity target = findClosestEnemy();
            if (target != null) {
                attack(target);
            }
        }
    }

    @Override
    public void render() {
        // Draw the tower as a small square
        glBegin(GL_QUADS);
        glColor3f(0.7f, 0.7f, 0.7f); // Grey color
        glVertex2f(x - width/2, y - height/2);
        glVertex2f(x + width/2, y - height/2);
        glVertex2f(x + width/2, y + height/2);
        glVertex2f(x - width/2, y + height/2);
        glEnd();

        // Draw the attack range (for debugging)
        glBegin(GL_LINE_LOOP);
        glColor3f(1.0f, 1.0f, 1.0f);
        for (int i = 0; i < 20; i++) {
            float angle = (float) (i * 2 * Math.PI / 20);
            float xPos = x + (float) Math.cos(angle) * attackRange;
            float yPos = y + (float) Math.sin(angle) * attackRange;
            glVertex2f(xPos, yPos);
        }
        glEnd();

        // Show cooldown indicator
        if (!canAttack()) {
            float cooldownPercentage = currentCooldown / attackCooldown;
            glBegin(GL_QUADS);
            glColor3f(0.8f, 0.2f, 0.2f); // Red color for cooldown
            glVertex2f(x - width/2, y + height/2 + 0.05f);
            glVertex2f(x - width/2 + width * (1 - cooldownPercentage), y + height/2 + 0.05f);
            glVertex2f(x - width/2 + width * (1 - cooldownPercentage), y + height/2 + 0.03f);
            glVertex2f(x - width/2, y + height/2 + 0.03f);
            glEnd();
        }
    }

    private Entity findClosestEnemy() {
        List<Entity> entities = game.getEntities();
        Entity closest = null;
        float closestDistance = Float.MAX_VALUE;

        for (Entity entity : entities) {
            if (entity instanceof EnemyShip) {
                float dx = entity.getX() - x;
                float dy = entity.getY() - y;
                float distance = (float) Math.sqrt(dx*dx + dy*dy);

                if (distance <= attackRange && distance < closestDistance) {
                    closest = entity;
                    closestDistance = distance;
                }
            }
        }

        return closest;
    }

    public boolean canAttack() {
        return currentCooldown <= 0;
    }

    public void attack(Entity target) {
        currentCooldown = attackCooldown;

        // Create a projectile targeted at the enemy
        Projectile projectile = new Projectile(
                x, y,
                target.getX(), target.getY(),
                attackDamage,
                0.03f, // Size
                0.6f, // Speed
                true  // Friendly projectile
        );

        // Add projectile to the game
        game.addEntity(projectile);
    }
}