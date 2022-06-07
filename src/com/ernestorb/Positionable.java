package com.ernestorb;

public abstract class Positionable {

    double x;
    double y;
    double xVelocity;
    double yVelocity;

    public Positionable(double x, double y, double xVelocity, double yVelocity) {
        this.x = x;
        this.y = y;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    abstract void move(int screenWidth, int screenHeight);

}
