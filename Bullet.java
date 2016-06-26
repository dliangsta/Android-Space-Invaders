package com.example.david.spaceinvaders;

import android.graphics.RectF;

/**
 * Created by David on 3/20/16.
 */
public class Bullet {

    private float x;
    private float y;

    private RectF rect;

    // direction
    public final int UP = 0;
    public final int DOWN = 1;

    // headed nowhere
    int heading = -1;
    float speed =  800;

    private int width = 5;
    private int height;

    private boolean isActive;

    public Bullet(int screenY) {

        height = screenY / 30;
        isActive = false;

        rect = new RectF();
    }

    public RectF getRect(){
        return  rect;
    }

    public boolean getStatus(){
        return isActive;
    }

    public void setInactive(){
        isActive = false;
    }

    public float getImpactPointY(){
        if (heading == DOWN)
            return y + height;
        else
            return  y;
    }

    public boolean shoot(float startX, float startY, int direction) {
        if (!isActive) {
            x = startX;
            y = startY;
            heading = direction;
            isActive = true;
            return true;
        }
        return false;
    }

    public void update(long fps){

        // just move up or down
        if(heading == UP)
            y = y - speed / fps;
        else
            y = y + speed / fps;
        // update rect
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;
    }
}
