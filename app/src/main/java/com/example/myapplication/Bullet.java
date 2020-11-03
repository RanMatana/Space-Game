package com.example.myapplication;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import static com.example.myapplication.Gameview.screenRationX;
import static com.example.myapplication.Gameview.screenRationY;

public class Bullet {

    int x, y, width, height;
    Bitmap bullet;

    // constructor that it creates a "Bitmap" object
    // called a "bullet" and runs it on the screen
    Bullet(Resources res) {
        bullet = BitmapFactory.decodeResource(res, R.drawable.bullet);

        width = bullet.getWidth();
        height = bullet.getHeight();

        width /= 4;
        height /= 4;


        width =(int) (width* screenRationX);
        height = (int) (height * screenRationY);

        bullet = Bitmap.createScaledBitmap(bullet, width, height, false);
    }

    Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}
