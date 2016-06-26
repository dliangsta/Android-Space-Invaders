package com.example.david.spaceinvaders;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

// SpaceInvadersActivity is the entry point to the game.
// It will handle the lifecycle of the game by calling
// methods of spaceInvadersView when prompted to so by the OS.

public class SpaceInvadersActivity extends Activity {

    // spaceInvadersView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);
        // Initialize gameView and set it as the view
        spaceInvadersView = new SpaceInvadersView(this, size.x, size.y);
        setContentView(spaceInvadersView);
    }

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();
        // Tell the gameView resume method to execute
        spaceInvadersView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();
        // Tell the gameView pause method to execute
        spaceInvadersView.pause();
    }
    
}