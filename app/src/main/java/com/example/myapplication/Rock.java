package com.example.myapplication;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import static com.example.myapplication.Gameview.screenRationX;
import static com.example.myapplication.Gameview.screenRationY;

public class Rock {

    public int speed = 200;
    public boolean wasShoot = true;
    int x = 0, y, width, height;
    Bitmap rock;

    // Here I Create a Rock by taking the picture
    // and putting it in the Bitmap variable so that I can know
    // the height and width of the object
    Rock(Resources res) {
        rock = BitmapFactory.decodeResource(res, R.drawable.rock);

        width = rock.getWidth(); // width rock
        height = rock.getHeight(); // height rock

        width /= 5;
        height /= 5;

        // Position screenRation
        width =(int) (width* screenRationX);
        height = (int) (height * screenRationY);


        // Show Rock
        rock = Bitmap.createScaledBitmap(rock, width, height, false);
        y = -height - 5;
    }

    // A method that returns a Rock type object
    Bitmap getRock() {
        return rock;
    }

    // A method that returns an object of type Rect
    Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}
