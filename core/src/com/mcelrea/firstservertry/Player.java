package com.mcelrea.firstservertry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Player {
    private float x;
    private float y;
    private Color color;
    private float prevX;
    private float prevY;

    public Player(Color color) {
        this.color = color;
        x = 300;
        y = 300;
        prevX = x;
        prevY = y;
    }

    public boolean hasMoved() {
        if(prevX != x || prevY != y) {
            prevX = x;
            prevY = y;
            return true;
        }
        return false;
    }

    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(x,y,7);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
