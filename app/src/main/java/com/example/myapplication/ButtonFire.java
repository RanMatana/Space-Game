package com.example.myapplication;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ButtonFire {
    Bitmap btnFire;
    private boolean isPressed;
    int x, y;
    private double CenterToTouchDistance;

    // constructor that it creates a "Bitmap" object
    // called "background" and runs it on the screen
    ButtonFire(int screenX, int screenY, Resources res) {
        btnFire = BitmapFactory.decodeResource(res, R.drawable.btnfire);
        btnFire = Bitmap.createScaledBitmap(btnFire, screenX, screenY, false);
        this.x = screenX;
        this.y = screenY;
    }

    public boolean getIsPressed() {
        return isPressed;
    }

    public void setIsPressed(boolean b) {
        this.isPressed = b;
    }

    public boolean isPressed(double touchPositionX, double touchPositionY) {
        CenterToTouchDistance = Math.sqrt(
                Math.pow(x - touchPositionX, 2) +
                        Math.pow(y - touchPositionY, 2)
        );
        return true;
    }

}
