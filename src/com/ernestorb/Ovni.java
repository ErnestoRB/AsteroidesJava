package com.ernestorb;

import java.awt.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Ovni extends Positionable implements Drawable {

    private final double minVelocity = 1;
    private final double maxVelocity = 3;

    private class OvniVelocityChange implements Runnable {

        @Override
        public void run() {
            double vel= minVelocity + Math.random()*(maxVelocity-minVelocity),
                    dir=2*Math.PI*Math.random(); // random direction
            xVelocity=vel*Math.cos(dir);
            yVelocity=vel*Math.sin(dir);
        }
    }


    public Ovni(double x, double y) {
        super(x, y, 0, 0);
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(new OvniVelocityChange(), 0, 2000, TimeUnit.MILLISECONDS);
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

        if (x < 0)
            x = screenWidth;
        if (y < 0)
            y = screenHeight;
        if (x > screenWidth)
            x = 0;
        if (y > screenHeight)
            y = 0;

    }
}
