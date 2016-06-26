package com.example.david.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

/**
 * Created by David on 3/20/16.
 */
public class Invader {

    RectF rect;

    Random generator = new Random();

    // The player ship will be represented by a Bitmap
    private Bitmap bitmap1;
    private Bitmap bitmap2;

    // How long and high our paddle will be
    private float length;
    private float height;

    // X is the far left of the rectangle which forms our paddle
    private float x;

    // Y is the top coordinate
    private float y;

    // This will hold the pixels per second speedthat the paddle will move
    private float shipSpeed;

    public final int LEFT = 1;
    public final int RIGHT = 2;

    // Is the ship moving and in which direction
    private int shipMoving = RIGHT;

    boolean isVisible;
    public Invader(Context context, int row, int column, int screenX, int screenY) {

        // Initialize a blank RectF
        rect = new RectF();

        length = screenX / 20;
        height = screenY / 20;

        isVisible = true;

        int padding = screenX / 25;

        x = column * (length + padding);
        y = row * (length + padding/4);

        // Initialize the bitmap
        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);

        // stretch the first bitmap to a size appropriate for the screen resolution
        bitmap1 = Bitmap.createScaledBitmap(bitmap1,
                (int) (length),
                (int) (height),
                false);

        // stretch the first bitmap to a size appropriate for the screen resolution
        bitmap2 = Bitmap.createScaledBitmap(bitmap2,
                (int) (length),
                (int) (height),
                false);

        // How fast is the invader in pixels per second
        shipSpeed = 40;
    }
    //Next we have a bunch of getters, setters and helper methods. The first two below get and set the object as invisible and get the current state of visibility. The next two get the two bitmaps and return them to the code that called them. The next three; getX, getY and getLength return the x,  y and length values respectively. These are used, as they was with PlayerShip to get precise coordinates at which to fire a bullet. Enter these getter and setter methods.

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap1;
    }

    public Bitmap getBitmap2(){
        return bitmap2;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getLength(){
        return length;
    }

    public void update(long fps){
        if(shipMoving == LEFT){
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT){
            x = x + shipSpeed / fps;
        }

        // Update rect which is used to detect hits
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }
    public void dropDownAndReverse(){
        if(shipMoving == LEFT){
            shipMoving = RIGHT;
        }else{
            shipMoving = LEFT;
        }

        y = y + height;

        shipSpeed = shipSpeed * 1.18f;
    }
    public boolean takeAim(float playerShipX, float playerShipLength){

        int randomNumber = -1;

        // If near the player
        if((playerShipX + playerShipLength > x && playerShipX + playerShipLength < x + length) ||
                (playerShipX > x && playerShipX < x + length)) {

            // A 1 in 500 chance to shoot
            randomNumber = generator.nextInt(150);
            if(randomNumber == 0) {
                return true;
            }

        }

        // If firing randomly (not near the player) a 1 in 5000 chance
        randomNumber = generator.nextInt(2000);
        if(randomNumber == 0){
            return true;
        }

        return false;
    }
}
