package com.example.david.spaceinvaders;

import android.graphics.RectF;

/**
 * Created by David on 3/20/16.
 */
public class DefenceBrick {

    private RectF rect;

    private boolean isVisible;

    public DefenceBrick(int row, int column, int shelterNumber, int screenX, int screenY){

        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;

        int brickPadding = 1;
        int shelterPadding = screenX / 9;
        int startHeight = screenY - (screenY /8 * 2);

        // what the brick will look like
        rect = new RectF(column * width + brickPadding + (shelterPadding * shelterNumber) + shelterPadding + shelterPadding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding + (shelterPadding * shelterNumber) + shelterPadding + shelterPadding * shelterNumber,
                row * height + height - brickPadding + startHeight);
    }
    public RectF getRect(){
        return this.rect;
    }

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }
}
