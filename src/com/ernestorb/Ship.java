package com.ernestorb;

import java.awt.*;

public class Ship extends Positionable implements Drawable, Shooter {
    final double[] origXPts = {14, -10, -6, -10}, origYPts = {0, -8, 0, 8},
            origFlameXPts = {-6, -23, -6}, origFlameYPts = {-3, 0, 3};
    final int radius = 6;

    double angle, acceleration,
            velocityDecay, rotationalSpeed;
    boolean turningLeft, turningRight, accelerating, active;
    int[] xPts, yPts, flameXPts, flameYPts;
    int shotDelay, shotDelayLeft;

    public Ship(double x, double y, double angle, double acceleration,
                double velocityDecay, double rotationalSpeed,
                int shotDelay) {
        super(x, y, 0,0);
        //this.x refers to the Ship's x, x refers to the x parameter
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.acceleration = acceleration;
        this.velocityDecay = velocityDecay;
        this.rotationalSpeed = rotationalSpeed;
        xVelocity = 0; // not moving
        yVelocity = 0;
        turningLeft = false; // not turning
        turningRight = false;
        accelerating = false; // not accelerating
        active = false; // start off paused
        xPts = new int[4]; // allocate space for the arrays
        yPts = new int[4];
        flameXPts = new int[3];
        flameYPts = new int[3];
        this.shotDelay = shotDelay; // # of frames between shots
        shotDelayLeft = 0; // ready to shoot
    }

    public void draw(Graphics g) {
        //rotate the points, translate them to the ship's location (by
        //adding x and y), then round them by adding .5 and casting them
        //as integers (which truncates any decimal place)
        if (accelerating && active) { // draw flame if accelerating
            for (int i = 0; i < 3; i++) {
                flameXPts[i] = (int) (origFlameXPts[i] * Math.cos(angle) -
                        origFlameYPts[i] * Math.sin(angle) +
                        x + .5);
                flameYPts[i] = (int) (origFlameXPts[i] * Math.sin(angle) +
                        origFlameYPts[i] * Math.cos(angle) +
                        y + .5);
            }
            g.setColor(Color.red); //set color of flame
            g.fillPolygon(flameXPts, flameYPts, 3); // 3 is # of points
        }
        //calculate the polgyon for the ship, then draw it
        for (int i = 0; i < 4; i++) {
            xPts[i] = (int) (origXPts[i] * Math.cos(angle) - //rotate
                    origYPts[i] * Math.sin(angle) +
                    x + .5); //translate and round
            yPts[i] = (int) (origXPts[i] * Math.sin(angle) + //rotate
                    origYPts[i] * Math.cos(angle) +
                    y + .5); //translate and round
        }
        if (active) // active means game is running (not paused)
            g.setColor(Color.white);
        else // draw the ship dark gray if the game is paused
            g.setColor(Color.darkGray);
        g.fillPolygon(xPts, yPts, 4); // 4 is number of points
    }

    public void move(int scrnWidth, int scrnHeight) {
        if (shotDelayLeft > 0) //move() is called every frame that the game
            shotDelayLeft--; //is run; this ticks down the shot delay
        if (turningLeft) //this is backwards from typical polar coodinates
            angle -= rotationalSpeed; //because positive y is downward.
        if (turningRight) //Because of that, adding to the angle is
            angle += rotationalSpeed; //rotating clockwise (to the right)
        if (angle > (2 * Math.PI)) //Keep angle within bounds of 0 to 2*PI
            angle -= (2 * Math.PI);
        else if (angle < 0)
            angle += (2 * Math.PI);
        if (accelerating) { //adds accel to velocity in direction pointed
            //calculates components of accel and adds them to velocity
            xVelocity += acceleration * Math.cos(angle);
            yVelocity += acceleration * Math.sin(angle);
        }
        x += xVelocity; //move the ship by adding velocity to position
        y += yVelocity;
        xVelocity *= velocityDecay; //slows ship down by percentages
        yVelocity *= velocityDecay; //(velDecay should be between 0 and 1)
        if (x < 0) //wrap the ship around to the opposite side of the screen
            x += scrnWidth; //when it goes out of the screen's bounds
        else if (x > scrnWidth)
            x -= scrnWidth;
        if (y < 0)
            y += scrnHeight;
        else if (y > scrnHeight)
            y -= scrnHeight;
    }

    public void setAccelerating(boolean accelerating) {
        this.accelerating = accelerating;
    }

    public void setTurningLeft(boolean turningLeft) {
        this.turningLeft = turningLeft;
    }

    public void setTurningRight(boolean turningRight) {
        this.turningRight = turningRight;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public boolean canShoot() {
        //checks to see if the ship is ready to
        return shotDelayLeft <= 0; //shoot again yet or needs to wait longer
    }

    public Shot shoot() {
        shotDelayLeft = shotDelay; //set delay till next shot can be fired
        //a life of 40 makes the shot travel about the width of the
        //screen before disappearing
        return new Shot(x, y, angle, xVelocity, yVelocity, 40, this);
    }
}