package com.ernestorb;

import java.awt.*;

public class Shot {
    final double shotSpeed = 12;
    double x, y, xVelocity, yVelocity;
    int lifeLeft;

    public Shot(double x, double y, double angle, double shipXVel,
                double shipYVel, int lifeLeft) {
        this.x = x;
        this.y = y;
        // add the velocity of the ship to the velocity the shot velocity
        // (so the shot's velocity is relative to the ship's)
        xVelocity = shotSpeed * Math.cos(angle) + shipXVel;
        yVelocity = shotSpeed * Math.sin(angle) + shipYVel;
        // the number of frames the shot will last for before
        // disappearing if it doesn't hit anything
        this.lifeLeft = lifeLeft;
    }

    public void move(int scrnWidth, int scrnHeight) {
        lifeLeft--; // used to make shot disappear if it goes too long
        // without hitting anything
        x += xVelocity; // move the shot
        y += yVelocity;
        if (x < 0) // wrap the shot around to the opposite side of the
            x += scrnWidth; // screen if needed
        else if (x > scrnWidth)
            x -= scrnWidth;
        if (y < 0)
            y += scrnHeight;
        else if (y > scrnHeight)
            y -= scrnHeight;
    }

    public void draw(Graphics g) {
        g.setColor(Color.yellow); //set shot color
        //draw circle of radius 3 centered at the closest point
        //with integer coordinates (.5 added to x-1 and y-1 for rounding)
        g.fillOval((int) (x - .5), (int) (y - .5), 3, 3);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getLifeLeft() {
        return lifeLeft;
    }
}

