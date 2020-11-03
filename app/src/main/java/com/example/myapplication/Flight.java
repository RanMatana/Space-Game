package com.example.myapplication;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import static com.example.myapplication.Gameview.screenRationX;
import static com.example.myapplication.Gameview.screenRationY;

public class Flight {

    int toShoot = 0;
    boolean isGoingUp = false;
    // x,y
    int width, height, shootCounter = 1;
    Bitmap flight, dead;
    private Gameview gameview;
    public double velocityX;
    public double velocityY;
    public int positionX;
    public int positionY;

    public Flight(Gameview gameview, int screenX, int screenY, Resources res) {

        this.gameview = gameview;
        flight = BitmapFactory.decodeResource(res, R.drawable.player);

        this.positionY = screenX/4;
        this.positionX = (int)(64*screenRationX);

        width = flight.getWidth(); // width Flight
        height = flight.getHeight(); // height Flight

        width /= 4;
        height /= 4;

        width = (int) (width * screenRationX);
        height = (int) (height * screenRationY);

        flight = Bitmap.createScaledBitmap(flight, width, height, false);

        dead = BitmapFactory.decodeResource(res, R.drawable.dead);
        dead = Bitmap.createScaledBitmap(dead, width, height + 150, false);

//        y = screenY / 2;
//        x = (int) (64 * screenRationX);
    }


    // A method that returns a variable called flight
    Bitmap getFlight() {
        if (toShoot != 0) {
            shootCounter++;
            toShoot--;
            gameview.newBullet();
        }
        return flight;
    }

    // A method that returns a Rect object
//    Rect getCollisionShape() {
//        return new Rect(x, y, x + width, y + height);
//    }
    Rect getCollisionShape() {
        return new Rect(positionX, positionY, positionX + width, positionY + height);
    }

    // A method that returns the variable dead
    Bitmap getDead() {
        return dead;
    }
}
