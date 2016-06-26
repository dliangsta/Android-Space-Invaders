package com.example.david.spaceinvaders;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

// SpaceInvadersActivity is the entry point to the game.
// It will handle the lifecycle of the game by calling
// methods of spaceInvadersView when prompted to so by the OS.

public class SpaceInvadersActivity extends Activity {

    SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        spaceInvadersView = new SpaceInvadersView(this, size.x, size.y);
        setContentView(spaceInvadersView);
    }

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();
        spaceInvadersView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();
        spaceInvadersView.pause();
    }
    
}