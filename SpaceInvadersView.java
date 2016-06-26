/**
 * Created by David Liang on 3/20/16.
 */
package com.example.david.spaceinvaders;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

public class SpaceInvadersView extends SurfaceView implements Runnable {
    private Context context;
    private Thread gameThread = null;
    private SurfaceHolder ourHolder;
    // A boolean for if the game is being played
    private volatile boolean playing;
    // Game is paused at the start
    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    // This is used to help calculate the fps
    private long timeThisFrame;
    private long fps;

    // The size of the screen in pixels
    private int screenX, screenY;

    private PlayerShip playerShip;
    private Bullet[] playerBullets = new Bullet[250];
    private int numBullets;
    private int nextInvaderBullet;
    private Bullet[] invadersBullets = new Bullet[50];
    private int maxInvaderBullets = 50;
    private Invader[] invaders = new Invader[30];
    private int numInvaders = 0, score = 0;
    private DefenceBrick[] bricks = new DefenceBrick[200];
    private int numBricks;

    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    // Score increase per kill and initial lives
    private final int INCREASE = 10;
    private final int INIT_LIVES = 20;
    private int lives = INIT_LIVES;

    // How menacing should the sound be?
    private long menaceInterval = 1000;
    // Which menace sound should play next
    private boolean uhOrOh;
    // When did we last play a menacing sound
    private long lastMenaceTime = System.currentTimeMillis();

    /**
     * Constructor
     * @param context
     * @param x
     * @param y
     */
    public SpaceInvadersView(Context context, int x, int y) {
        super(context);
        this.context = context;
        ourHolder = getHolder();
        paint = new Paint();
        screenX = x;
        screenY = y;
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);
        }
        catch (IOException e) {
            Log.e("error", "failed to load sound files");
        }
        prepareLevel();
    }

    /**
     * Prepares the level for gameplay by initializing fields.
     */
    private void prepareLevel() {
        paused = true;
        lives = INIT_LIVES;
        score = 0;
        numBullets = 0;

        // Make a new player space ship
        playerShip = new PlayerShip(context, screenX, screenY);

        // Prepare the players bullet
        for (int i = 0; i < playerBullets.length; i++)
            playerBullets[i] = new Bullet(screenY);

        // Initialize the invadersBullets array
        for (int i = 0; i < invadersBullets.length; i++)
            invadersBullets[i] = new Bullet(screenY);

        // Build an army of invaders
        numInvaders = 0;
        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 5; row++) {
                invaders[numInvaders++] = new Invader(context, row, column, screenX, screenY);
//                numInvaders++;
            }
        }

        // Build the shelters
        numBricks = 0;
        for (int shelterNumber = 0; shelterNumber < 4; shelterNumber++) {
            for (int column = 0; column < 10; column++)
                for (int row = 0; row < 5; row++) {
                    bricks[numBricks++] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
//                    numBricks++;
                }
        }

        // Reset the menace level
        menaceInterval = 1000;
    }

    @Override
    /**
     * Method the continues to run while playing is true.
     */
    public void run() {
        while (playing) {
            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();
            // Update the frame
            if (!paused) update();
            draw();
            // Calculate FPS
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1)
                fps = 1000 / timeThisFrame;
            // Play a sound based on the menace level
            if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) soundPool.play(uhID, 1, 1, 0, 0, 1);
                    else soundPool.play(ohID, 1, 1, 0, 0, 1);
                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }
        }
    }

    /**
     * Updates the SurfaceInvadersView's fields.
     */
    private void update() {
        // Did an invader bump into the side of the screen
        boolean bumped = false;
        // Has the player lost
        boolean lost = false;
        // Move the player's ship
        playerShip.update(fps);

        // Update the players bullet
        for (Bullet bullet : playerBullets) {
            if (bullet.getStatus())
                bullet.update(fps);
        }

        // Update all the invaders bullets if active
        for (Bullet bullet : invadersBullets) {
            if (bullet.getStatus())
                bullet.update(fps);
        }

        // Update all the invaders if visible
        for (Invader invader : invaders) {
            if (invader.getVisibility()) {
                // Move the next invader
                invader.update(fps);
                // Does he want to take a shot?
                if (invader.takeAim(playerShip.getX(), playerShip.getLength()))
                    // If so try and spawn a bullet
                    if (invadersBullets[nextInvaderBullet].shoot(invader.getX() + invader.getLength() / 2, invader.getY(), 1)) {
                        // Shot fired, prepare for the next shot
                        nextInvaderBullet++;
                        // Loop back to the first one if we have reached the last
                        if (nextInvaderBullet == maxInvaderBullets)
                            // makes sure that only one bullet is fired at a time
                            nextInvaderBullet = 0;
                    }
                // If that move caused them to bump the screen change bumped to true
                if (invader.getX() > screenX - invader.getLength() || invader.getX() < 0)
                    bumped = true;
            }
        }

        // Did an invader bump into the edge of the screen
        if (bumped) {
            // Move all the invaders down and change direction
            for (Invader invader : invaders) {
                invader.dropDownAndReverse();
                // Have the invaders landed
                if (invader.getY() > screenY - screenY / 10)
                    lost = true;
            }
            // Increase the menace level by making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }

        if (lost) prepareLevel();

        // Has the player's bullet hit the top of the screen
        for (Bullet bullet : playerBullets) {
            if (bullet.getImpactPointY() < 0)
                bullet.setInactive();
        }

        // Has an invaders bullet hit the bottom of the screen
        for (Bullet bullet : invadersBullets) {
            if (bullet.getImpactPointY() > screenY)
                bullet.setInactive();
        }

        // Has the player's bullet hit an invader
        for (Bullet bullet : playerBullets) {
            if (bullet.getStatus())
                for (Invader invader : invaders)
                    if (invader.getVisibility() && RectF.intersects(bullet.getRect(), invader.getRect())) {
                        invader.setInvisible();
                        bullet.setInactive();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        score = score + INCREASE;
                        // Has the player won
                        if (score == numInvaders * INCREASE) {
                            prepareLevel();
                            break;
                        }
                    }
        }

        // Has an alien bullet hit a shelter brick
        for (Bullet bullet : invadersBullets) {
            if (bullet.getStatus())
                for (DefenceBrick brick : bricks)
                    if (brick.getVisibility() && (RectF.intersects(bullet.getRect(), brick.getRect()))) {
                        // A collision has occurred
                        bullet.setInactive();
                        brick.setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
        }

        // Has a player bullet hit a shelter brick
        for (Bullet bullet : playerBullets) {
            if (bullet.getStatus())
                for (DefenceBrick brick : bricks)
                    if (brick.getVisibility() && RectF.intersects(bullet.getRect(), brick.getRect())) {
                        // A collision has occurred
                        bullet.setInactive();
                        brick.setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
        }

        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus())
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) {
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
                    // Is it game over?
                    if (lives == 0)
                        prepareLevel();
                }
        }
    }

    private void draw() {
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();
            // Draw the background color
            canvas.drawColor(Color.argb(255, 26, 128, 182));
            // Choose the brush color for drawing
            paint.setColor(Color.argb(255, 255, 255, 255));
            // Now draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - 50, paint);
            // Draw the invaders
            for (int i = 0; i < numInvaders; i++)
                if (invaders[i].getVisibility())
                    if (uhOrOh)
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    else
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
            // Draw the bricks if visible
            for (int i = 0; i < numBricks; i++)
                if (bricks[i].getVisibility())
                    canvas.drawRect(bricks[i].getRect(), paint);
            // Draw the players bullet if active
            for (int i = 0; i < playerBullets.length; i++) {
                Bullet bullet = playerBullets[i];
                if (bullet.getStatus())
                    canvas.drawRect(bullet.getRect(), paint);
            }
            // Update all the invader's bullets if active
            for (int i = 0; i < invadersBullets.length; i++)
                if (invadersBullets[i].getStatus())
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
            // Draw the score and remaining lives
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);
            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // If SpaceInvadersActivity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // If SpaceInvadersActivity is started then
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean run = true;
        int j = 0;
        boolean shot = false;
        while (run && j < 100) {
            j++;
            int switchInt = motionEvent.getAction() & MotionEvent.ACTION_MASK;

            boolean id2Exists = true;
            int id2 = 0;
            try {
                id2 = motionEvent.getPointerId(1);
            }
            catch (Exception e) {
                id2Exists = false;
            }
            switch (switchInt) {
                // Player has touched the screen or moved finger
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    // if touch is above bottom eigth, interpret as movement
                    if (motionEvent.getY() > screenY * 3 / 4)
                        if (motionEvent.getX() > screenX / 2)
                            playerShip.setMovementState(playerShip.RIGHT);
                        else
                            playerShip.setMovementState(playerShip.LEFT);
                    else if (id2Exists && motionEvent.getY(id2) > screenY * 3 / 4)
                            if (motionEvent.getX(id2) > screenX / 2)
                                playerShip.setMovementState(playerShip.RIGHT);
                            else
                                playerShip.setMovementState(playerShip.LEFT);

                    // shooting
                    if (motionEvent.getY() <= screenY * 3 / 4) {
                        // Shots fired
                        Bullet bullet = new Bullet(screenY);
                        if (numBullets < playerBullets.length) {
                            playerBullets[numBullets] = bullet;
                            if (bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, bullet.UP) && shot) {
                                soundPool.play(shootID, 1, 1, 0, 0, 1);
                                shot = false;
                            }
                            numBullets++;
                        }
                        else
                            numBullets = 0;
                        run = false;
                    }
                    else if (id2Exists) {
                        if (motionEvent.getY(id2) < screenY * 3 / 4) {
                            // Shots fired
                            Bullet bullet = new Bullet(screenY);
                            if (numBullets < playerBullets.length) {
                                playerBullets[numBullets] = bullet;
                                if (bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, bullet.UP) && shot) {
                                    soundPool.play(shootID, 1, 1, 0, 0, 1);
                                    shot = false;
                                }
                                numBullets++;
                            } else
                                numBullets = 0;
                            run = false;
                        }
                    }
                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    run = false;
                        if (motionEvent.getY() > screenY *3/4) {
                            playerShip.setMovementState(playerShip.STOPPED);
                        } else if (id2Exists) {
                            if (motionEvent.getY(id2) > screenY *3/4) {
                                playerShip.setMovementState(playerShip.STOPPED);
                            }
                        }
                    playerShip.setMovementState(playerShip.STOPPED);
                    break;
            }
        }
        return true;
    }
}