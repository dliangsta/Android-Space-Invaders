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
    // a boolean for if the game is being played
    private volatile boolean playing;
    // game is paused at the start
    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    // used to help calculate fps
    private long timeThisFrame;
    private long fps;

    // screen size in pixels
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

    // for sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    // score increase per kill and initial lives
    private final int INCREASE = 10;
    private final int INIT_LIVES = 20;
    private int lives = INIT_LIVES;

    // how menacing should the sound be
    private long menaceInterval = 1000;
    // alternate which menace sound should play next
    private boolean uhOrOh;
    // when the last menace sound was played
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
            // create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
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

        // make a new player space ship, player's bullets, invader's bullets, invaders, shelters
        playerShip = new PlayerShip(context, screenX, screenY);

        for (int i = 0; i < playerBullets.length; i++)
            playerBullets[i] = new Bullet(screenY);

        for (int i = 0; i < invadersBullets.length; i++)
            invadersBullets[i] = new Bullet(screenY);

        numInvaders = 0;
        for (int column = 0; column < 6; column++)
            for (int row = 0; row < 5; row++)
                invaders[numInvaders++] = new Invader(context, row, column, screenX, screenY);

        numBricks = 0;
        for (int shelterNumber = 0; shelterNumber < 4; shelterNumber++)
            for (int column = 0; column < 10; column++)
                for (int row = 0; row < 5; row++)
                    bricks[numBricks++] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);

        // Reset the menace level
        menaceInterval = 1000;
    }

    @Override
    /**
     * Game loop
     */
    public void run() {
        while (playing) {
            // capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();
            // update the frame
            if (!paused) update();
            draw();
            // calculate FPS
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1)
                fps = 1000 / timeThisFrame;
            // play a sound based on the menace level
            if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) soundPool.play(uhID, 1, 1, 0, 0, 1);
                    else soundPool.play(ohID, 1, 1, 0, 0, 1);
                    // reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // flip value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }
        }
    }

    /**
     * Updates the SurfaceInvadersView's fields
     */
    private void update() {
        // true if an invader bumps into the side of the screen
        boolean bumped = false;
        // true if the player lost
        boolean lost = false;
        // update the player's ship
        playerShip.update(fps);

        // update the players bullet
        for (Bullet bullet: playerBullets)
            if (bullet.getStatus())
                bullet.update(fps);

        // update all the invaders bullets if active
        for (Bullet bullet: invadersBullets)
            if (bullet.getStatus())
                bullet.update(fps);

        // update all the invaders if visible
        for (Invader invader: invaders)
            if (invader.getVisibility()) {
                // move the next invader
                invader.update(fps);
                // if invader will shoot
                if (invader.takeAim(playerShip.getX(), playerShip.getLength()))
                    // if so spawn a bullet
                    if (invadersBullets[nextInvaderBullet].shoot(invader.getX() + invader.getLength() / 2, invader.getY(), 1)) {
                        // shot fired, prepare for the next shot
                        nextInvaderBullet++;
                        // loop back to the first one if we have reached the last
                        if (nextInvaderBullet == maxInvaderBullets)
                            // makes sure that only one bullet is fired at a time
                            nextInvaderBullet = 0;
                    }
                // if that move caused them to bump the screen change bumped to true
                if (invader.getX() > screenX - invader.getLength() || invader.getX() < 0)
                    bumped = true;
            }


        // determines what to do if an invader bumps into the edge of the screen
        if (bumped) {
            // move all the invaders down and change direction
            for (Invader invader: invaders) {
                invader.dropDownAndReverse();
                // have the invaders landed
                if (invader.getY() > screenY - screenY / 10)
                    lost = true;
            }
            // increase the menace level by making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }

        if (lost) prepareLevel();

        // determines if the player's bullet hit the top of the screen
        for (Bullet bullet: playerBullets) {
            if (bullet.getImpactPointY() < 0)
                bullet.setInactive();
        }

        // renders bullets inactive after they've hit the bottom of the screen
        for (Bullet bullet: invadersBullets) {
            if (bullet.getImpactPointY() > screenY)
                bullet.setInactive();
        }

        // determines if a player's bullets have hit an invader
        for (Bullet bullet: playerBullets)
            if (bullet.getStatus())
                for (Invader invader: invaders)
                    if (invader.getVisibility() && RectF.intersects(bullet.getRect(), invader.getRect())) {
                        invader.setInvisible();
                        bullet.setInactive();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        score = score + INCREASE;
                        // checks to see if the player has won
                        if (score == numInvaders * INCREASE) {
                            prepareLevel();
                            break;
                        }
                    }


        // determines if an alien bullet hit a shelter brick
        for (Bullet bullet: invadersBullets)
            if (bullet.getStatus())
                for (DefenceBrick brick: bricks)
                    if (brick.getVisibility() && (RectF.intersects(bullet.getRect(), brick.getRect()))) {
                        // if a collision has occurred
                        bullet.setInactive();
                        brick.setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }


        // determines what to do if a player bullet hits a shelter brick
        for (Bullet bullet: playerBullets)
            if (bullet.getStatus())
                for (DefenceBrick brick: bricks)
                    if (brick.getVisibility() && RectF.intersects(bullet.getRect(), brick.getRect())) {
                        // if a collision has occurred
                        bullet.setInactive();
                        brick.setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }


        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus())
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) {
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
                    // checks if game is over
                    if (lives == 0)
                        prepareLevel();
                }
        }
    }

    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            // lock the canvas to make it ready for drawing
            canvas = ourHolder.lockCanvas();
            // draw the background color
            canvas.drawColor(Color.argb(255, 26, 128, 182));
            paint.setColor(Color.argb(255, 255, 255, 255));
            // draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - 50, paint);
            // draw the invaders
            for (int i = 0; i < numInvaders; i++)
                if (invaders[i].getVisibility())
                    if (uhOrOh)
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    else
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
            // draw the bricks if visible
            for (int i = 0; i < numBricks; i++)
                if (bricks[i].getVisibility())
                    canvas.drawRect(bricks[i].getRect(), paint);
            // draw the players bullet if active
            for (int i = 0; i < playerBullets.length; i++) {
                Bullet bullet = playerBullets[i];
                if (bullet.getStatus())
                    canvas.drawRect(bullet.getRect(), paint);
            }
            // update all the invader's bullets if active
            for (int i = 0; i < invadersBullets.length; i++)
                if (invadersBullets[i].getStatus())
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
            // draw the score and remaining lives
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);
            // draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

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
                // when player has touched the screen or moved finger
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
                        // shots fired
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
                            // shots fired
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

                // player has removed finger from screen
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    run = false;
                        if (motionEvent.getY() > screenY *3/4)
                            playerShip.setMovementState(playerShip.STOPPED);
                        else if (id2Exists)
                            if (motionEvent.getY(id2) > screenY *3/4)
                                playerShip.setMovementState(playerShip.STOPPED);
                    playerShip.setMovementState(playerShip.STOPPED);
                    break;
            }
        }
        return true;
    }
}