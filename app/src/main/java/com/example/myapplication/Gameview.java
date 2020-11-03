package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.facebook.share.internal.DeviceShareDialogFragment.TAG;

@SuppressLint("ViewConstructor")
public class Gameview extends SurfaceView implements Runnable {

    private static final double SPEED_PIXELS_PER_SECOND = 1000.0;
    private static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / 30.0;
    private final Joystick joystick;
    private Thread thread;
    boolean isPlaying, isGameOver = false;
    boolean loadImage = false;
    private int screenX, screenY, score = 0;
    public static float screenRationX, screenRationY;
    private Paint paint;
    private Rock[] rocks;
    private SharedPreferences preferences;
    private Random random;
    private SoundPool soundPool;
    private List<Bullet> bullets;
    private int sound;
    private Flight flight;
    private GameActivity activity;
    private Background background;
    DatabaseReference myRef, myRefScores;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    private List<User> listUsersScore;
    private boolean checkUser;
    private boolean dataSnapCheck;
    private ButtonFire btnFire;


    public Gameview(GameActivity activity, int screenX, int screenY) {
        super(activity);

        // Initialize the activity and preferences
        this.activity = activity;
        preferences = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

        // Sounds of shots
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        sound = soundPool.load(activity, R.raw.shoot, 1);

        // Initialize joystick
        joystick = new Joystick(275, 700, 120, 65);
        // Initialize the db
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRefScores = FirebaseDatabase.getInstance().getReference("Score");

        // Initialize the screen
        this.screenX = screenX;
        this.screenY = screenY;
        screenRationX = 1920f / screenX;
        screenRationY = 1080f / screenY;

        // Initialize a Background
        background = new Background(screenX, screenY, getResources());

        btnFire = new ButtonFire(250, 250, getResources());


        // Initialize a Flight
//        flight = new Flight(this, screenY, getResources());
        flight = new Flight(this, screenX, screenY, getResources());
        // Initialize a list of balls
        bullets = new ArrayList<>();

        // Initialize a Paint
        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        // Initialize a Array of Rock
        rocks = new Rock[4];

        // All rocks take Resources and attribute
        for (int i = 0; i < 4; i++) {
            Rock rock = new Rock(getResources());
            rocks[i] = rock;
        }
        // this Random brings each Rocks its own speed and position
        random = new Random();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    private void update() {

        joystick.update();

        flight.velocityX = joystick.getActuatorX() * MAX_SPEED;
        flight.velocityY = joystick.getActuatorY() * MAX_SPEED;
        flight.positionX += flight.velocityX;
        flight.positionY += flight.velocityY;
        if (flight.positionY < 0)
            flight.positionY = 0;
        if (flight.positionY > screenY - flight.height)
            flight.positionY = screenY / 2 + 400;

        // Checks if the value of the variable and its width
        // is smaller than zero. So as not to exceed boundaries
        if (background.x + background.background.getWidth() < 0) {
            background.x = screenX;
        }

        // A list of "bullet" called "trash" that stores
        // all the balls that come out of the screen
        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : bullets) {
            if (bullet.x > screenX)
                trash.add(bullet);

            bullet.x += 50 * screenRationX;

            // A loop that checks if one of the rocks was damaged
            // then pulls it off the screen and counts
            for (Rock rock : rocks) {
                if (Rect.intersects(rock.getCollisionShape(),
                        bullet.getCollisionShape())) {
                    score++;
                    rock.x = -500;
                    bullet.x = screenX + 500;
                    rock.wasShoot = true;
                }
            }
        }

        // Remove trash bullets
        for (Bullet bullet : trash) {
            bullets.remove(bullet);
        }
        // Updates to rock speed
        for (Rock rock : rocks) {
            rock.x -= rock.speed;

            if (rock.x + rock.width < 0) {

                // Updates to rock speed by Random
                if (score > 10) {
                    level(rock, score);
                } else if (score > 20) {
                    level(rock, score);
                } else if (score > 30) {
                    level(rock, score);
                } else {
                    int bound = (int) (30 * screenRationX);
                    rock.speed = random.nextInt(bound);
                    if (rock.speed < 10 * screenRationX)
                        rock.speed = (int) (10 * screenRationX);
                }

                // Position By Random
                rock.x = screenX;
                rock.y = random.nextInt(screenY - rock.height);
                // Update status of Rock
                rock.wasShoot = false;

            }
            // If the player is hit by the rock then the game is over
            if (Rect.intersects(rock.getCollisionShape(), flight.getCollisionShape())) {
                isGameOver = true;
                return;
            }
        }
    }

    private void level(Rock rock, int score) {
        int bound = (int) (30 * screenRationX);
        rock.speed = random.nextInt(bound + score);
        if (rock.speed < 10 * screenRationX)
            rock.speed = (int) (10 * screenRationX);
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            // Show by canvas background
            canvas.drawBitmap(background.background, background.x, background.y, paint);

            joystick.draw(canvas);
            // Show by canvas rocks
            for (Rock rock : rocks)
                canvas.drawBitmap(rock.getRock(), rock.x, rock.y, paint);

            // draw score
            canvas.drawText(score + "", screenX / 2f, 164, paint);

            // If the game is over, the player's picture will be replaced.
            // Keep the high score
            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getDead(), flight.positionX, flight.positionY, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting();
                return;
            }
            // Show Flight
            canvas.drawBitmap(flight.getFlight(), flight.positionX, flight.positionY, paint);
            // Show btnFire
            canvas.drawBitmap(btnFire.btnFire, screenX - 350, screenY / 2 + 20, paint);

            // Show bullets
            for (Bullet bullet : bullets) {
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);
            }
            // unlock canvas
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    // A method that holds the thread for 3 seconds and goes to the MainActivity page
    private void waitBeforeExiting() {
        try {
            //Thread.sleep(3000);
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra("needToLoad", true);
            activity.startActivity(intent);
            activity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A method that saves the high score results in the DB
    private void saveIfHighScore() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User value = dataSnapshot.child(user.getUid()).getValue(User.class);
                assert value != null;
                if (value.getScore() < score) {
                    value.setScore(score);
                    myRef.child(user.getUid()).setValue(value);
                    checkInTable(value);
                }
            }

            private void checkInTable(final User value) {
                checkUser = false;
                dataSnapCheck = false;
                listUsersScore = new ArrayList<>();
                myRefScores.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapCheck) {
                            for (int i = 0; i < 5; i++) {
                                listUsersScore.add(dataSnapshot.child("" + (i + 1)).getValue(User.class));
                            }
                            for (int i = 0; i < listUsersScore.size(); i++) {
                                if (listUsersScore.get(i).getUserID().equals(user.getEmail())) {
                                    if (listUsersScore.get(i).getScore() <= score) {
                                        listUsersScore.set(i, new User(listUsersScore.get(i).getUserID(), listUsersScore.get(i).getUserName(), score, listUsersScore.get(i).getUri_image()));
                                        checkUser = true;
                                        break;
                                    }
                                }
                            }
                            if (!checkUser) {
                                for (int i = 0; i < listUsersScore.size(); i++) {
                                    if (listUsersScore.get(i).getScore() < score) {
                                        listUsersScore.add(0, new User(value.getUserID(), value.getUserName(), score, value.getUri_image()));
                                        break;
                                    }
                                }
                            }
                            Collections.sort(listUsersScore);
                            if (listUsersScore.size() > 5)
                                listUsersScore.remove(listUsersScore.size() - 1);
                            for (int i = 0; i < 5; i++) {
                                myRefScores.child("" + (i + 1)).setValue(listUsersScore.get(i));
                            }
                        }
                        dataSnapCheck = true;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // A method that holds the thread for 17 millis
    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Thread start
    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    // Thread join
    public void pause() throws InterruptedException {
        isPlaying = false;
        loadImage = true;
        thread.join();
    }

    // Override to a method called "onTouchEvent" that checks the Action.
    // If the player clicks the screen on his left then the player will go up,
    // if not then the player will go down.
    //The method returns a boolean
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                btnFire.setIsPressed(true);
                if (btnFire.isPressed((double) event.getX(), (double) event.getY())) {
                    flight.toShoot++;
                }
                if (joystick.isPressed((double) event.getX(), (double) event.getY())) {
                    joystick.setIsPressed(true);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (joystick.getIsPressed()) {
                    joystick.setActuator((double) event.getX(), (double) event.getY());
                }
                return true;

            case MotionEvent.ACTION_UP:
                joystick.setIsPressed(false);
                joystick.resetActuator();
                btnFire.setIsPressed(false);
                return true;
        }
        return super.onTouchEvent(event);
    }


    public void newBullet() {
        if (!preferences.getBoolean("isMute", false))
            soundPool.play(sound, 1, 1, 0, 0, 1);

        Bullet bullet = new Bullet(getResources());
        bullet.x = flight.positionX + flight.width;
        bullet.y = flight.positionY + (flight.height / 2);
        bullets.add(bullet);
    }
}
