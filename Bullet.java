package com.example.david.spaceinvaders;

import android.graphics.RectF;

/**
 * Created by David on 3/20/16.
 */
public class Bullet {

    private float x;
    private float y;

    private RectF rect;

    // Which way is it shooting
    public final int UP = 0;
    public final int DOWN = 1;

    // Going nowhere
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
   // Next we have a bunch of getters and setters that pass rect back to SpaceInvadersView for collision detection and drawing, returns the status (isActive true or false) which as we will see is useful for knowing when to draw and check for collisions and finally we have a getImpactPointY method which returns the tip pixel of the bullet. This will be different depending upon whether the bullet is heading down (fired by an invader) or up (fired by player). Enter the getters and setters we have just discussed below the previous block of code.

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
        if (heading == DOWN){
            return y + height;
        }else{
            return  y;
        }

    }
    public boolean shoot(float startX, float startY, int direction) {
        if (!isActive) {
            x = startX;
            y = startY;
            heading = direction;
            isActive = true;
            return true;
        }
        // Bullet already active
        return false;
    }

    public void update(long fps){

        // Just move up or down
        if(heading == UP){
            y = y - speed / fps;
        }else{
            y = y + speed / fps;
        }

        // Update rect
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;

    }
}
