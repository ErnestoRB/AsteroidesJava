package com.ernestorb;

import java.awt.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Ovni extends Positionable implements Shooter,Drawable {

    private final double minVelocity = 1;
    private final double maxVelocity = 3;
    double angle;
    int shotDelayLeft, shotDelay;

    private class OvniVelocityChange implements Runnable {

        @Override
        public void run() {
            double vel= minVelocity + Math.random()*(maxVelocity-minVelocity),
                    dir=2*Math.PI*Math.random(); // random direction
            xVelocity=vel*Math.cos(dir);
            yVelocity=vel*Math.sin(dir);
        }
    }


    public Ovni(double x, double y, int shotDelay) {
        super(x, y, 0, 0);
        this.angle = 0; //angulo de ejemplo
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(new OvniVelocityChange(), 0, 2000, TimeUnit.MILLISECONDS);
        this.shotDelay = shotDelay; // # of frames between shots
        shotDelayLeft = 0; // ready to shoot
        // esta línea genera un hilo cada 2s que genera una nueva velocidad (sentido y magnitud)
    }

    @Override
    public void draw(Graphics g) {
        g.fillOval((int)x - 20, (int)y - 10, 40, 20);
        g.setColor(Color.cyan);
        g.fillArc((int)x - 10, (int)y - 19, 20, 20, 0, 180);
        
    }

    @Override
    public void move(int screenWidth, int screenHeight) {
        x+=xVelocity;
        y+=yVelocity;
        if (shotDelayLeft > 0) 
            shotDelayLeft--; 
        if (x < 0)
            x = screenWidth;
        if (y < 0)
            y = screenHeight;
        if (x > screenWidth)
            x = 0;
        if (y > screenHeight)
            y = 0;
    }
    
    public Shot shoot() {
        shotDelayLeft = shotDelay; //set delay till next shot can be fired
        //a life of 40 makes the shot travel about the width of the
        //screen before disappearing
        angle++;
        return new Shot(x, y, angle, xVelocity, yVelocity, 40, this);
    }
    
    public boolean canShoot() {
        if (shotDelayLeft > 0) //checks to see if the ship is ready to
            return false; //shoot again yet or needs to wait longer
        else
            return true;
    }
}
