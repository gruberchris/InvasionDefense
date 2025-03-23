package org.chrisgruber.entity;

import java.util.UUID;

public abstract class Entity {
    private final UUID id;
    protected float x, y;
    protected float width, height;
    protected boolean active = true;

    protected Entity(float x, float y, float width, float height) {
        this.id = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(float deltaTime);
    public abstract void render();

    public UUID getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
