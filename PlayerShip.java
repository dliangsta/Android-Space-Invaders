package com.example.david.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

/**
 * Created by David on 3/20/16.
 */
public class PlayerShip {

    RectF rect;

    // the player ship will be represented by a Bitmap
    private Bitmap bitmap;

    private float length;
    private float height;

    // x is the far left of the rectangle which forms the ship, y is the top coordinate
    private float x;
    private float y;

    private float shipSpeed;
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // ship's movement status and direction
    private int shipMoving = STOPPED;

    public PlayerShip(Context context, int screenX, int screenY){
        // initialize a blank RectF
        rect = new RectF();

        length = screenX/10;
        height = screenY/10;

        // start ship in roughly the screen centre
        x = screenX / 2;
        y = screenY - 20;

        // initialize the bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);

        // stretch the bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (length), (int) (height), false);

        // how fast is the spaceship in pixels per second
        shipSpeed = 800;
    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public float getX(){
        return x;
    }

    public float getLength(){
        return length;
    }

    public void setMovementState(int state){
        shipMoving = state;
    }

    public void update(long fps){
        if(shipMoving == LEFT )
            if (x > length*.5) x = x - shipSpeed / fps;
        if(shipMoving == RIGHT)
            if (x < length*9.5) x = x + shipSpeed / fps;

        // update rect which is used to detect hits
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;
        shipMoving = STOPPED;
    }
}
