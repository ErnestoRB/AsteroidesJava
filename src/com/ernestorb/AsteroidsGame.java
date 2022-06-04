package com.ernestorb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class AsteroidsGame extends JPanel implements Runnable, KeyListener {

    private long score = 0;

    JFrame ventana;
    Thread thread;
    Dimension dim;

    long endTime, startTime, framePeriod;

    Ship ship;
    boolean paused; // True if the game is paused. Enter is the pause key
    int numShots;
    boolean shooting;
    Ovni ovni = new Ovni(100, 100, 25); //el tercer argumento indica el delay entre cada disparo de bala
    Vector<Shot> shots;

    Asteroid[] asteroids; //the array of asteroids
    int numAsteroids; //the number of asteroids currently in the array
    double astRadius, minAstVel, maxAstVel; //values used to create
    //asteroids
    int astNumHits, astNumSplit;

    int level; //the current level number

    public void init() {
        setSize(500, 500);
        shots = new Vector<>(100);

        numAsteroids = 0;
        level = 0; //will be incremented to 1 when first level is set up
        astRadius = 60; //values used to create the asteroids
        minAstVel = .5;
        maxAstVel = 5;
        astNumHits = 3;
        astNumSplit = 2;
        endTime = 0;
        startTime = 0;
        framePeriod = 25;
        dim = getSize();
        ventana = new JFrame("Asteroides");
        ventana.setSize(dim);
        ventana.add(this);
        ventana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ventana.addKeyListener(this); //tell it to listen for KeyEvents
        thread = new Thread(this);
        thread.start();
    }

    public void setUpNextLevel() { //start new level with one more asteroid
        level++;
        // create a new, inactive ship centered on the screen
        // I like .35 for acceleration, .98 for velocityDecay, and
        // .1 for rotationalSpeed. They give the controls a nice feel.
        ship = new Ship(250, 250, 0, .35, .98, .1, 12);
        numShots = 0; //no shots on the screen at beginning of level
        paused = false;
        shooting = false;
        //create an array large enough to hold the biggest number
        //of asteroids possible on this level (plus one because
        //the split asteroids are created first, then the original
        //one is deleted). The level number is equal to the
        //number of asteroids at it's start.
        numAsteroids = 4 + 2*(level-1); // SIEMPRE se aparecen 4 y 2 cada nivel
        asteroids = new Asteroid[numAsteroids *
                (int) Math.pow(astNumSplit, astNumHits - 1) + 1];
        //create asteroids in random spots on the screen
        for (int i = 0; i < numAsteroids; i++)
            asteroids[i] = new Asteroid(Math.random() * dim.width,
                    Math.random() * dim.height, astRadius, minAstVel,
                    maxAstVel, astNumHits, astNumSplit);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, 500, 500);

        for (Shot sht: shots) {
            sht.draw(g);
        }
        for (int i = 0; i < numAsteroids; i++)
            asteroids[i].draw(g);
        ship.draw(g); //draw the ship
        g.setColor(Color.cyan); //Display level number in top left corner
        try {
            g.setFont(Font.createFont(Font.TRUETYPE_FONT,new File("src/PressStart2P-Regular.ttf")).deriveFont(12.0F));
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        g.drawString("Level " + level, 20, 20);
        g.setColor(Color.green);
        g.drawString("Score " + score, 20, 40);
        ovni.draw(g);
    }


    public void run() {
        ventana.setVisible(true);
        while (true) {
            startTime = System.currentTimeMillis();

            //start next level when all asteroids are destroyed
            if (numAsteroids <= 0)
                setUpNextLevel();

            if (!paused) {
                ship.move(dim.width, dim.height); // move the ship
                //move shots and remove dead shots
                for (Shot sht: new ArrayList<>(shots)) {
                    sht.move(dim.width, dim.height);
                    if(sht.getLifeLeft() <= 0) {
                        shots.remove(sht);
                    }
                }

                if(ovni != null) {
                    ovni.move(dim.width, dim.height);
                    if(ovni.canShoot()){
                        shots.add(ovni.shoot());
                    }
                }
                //move asteroids and check for collisions
                updateAsteroids();

                if (shooting && ship.canShoot()) {
                    //add a shot on to the array
                    shots.add(ship.shoot());
                }
                if(score > 99990) {
                    score -= 99990;
                }
            }

            repaint();
            try {
                endTime = System.currentTimeMillis();
                if (framePeriod - (endTime - startTime) > 0)
                    Thread.sleep(framePeriod - (endTime - startTime));
            } catch (InterruptedException e) {
            }
        }
    }

    private void deleteAsteroid(int index) {
        //delete asteroid and shift ones after it up in the array
        numAsteroids--;
        for (int i = index; i < numAsteroids; i++)
            asteroids[i] = asteroids[i + 1];
        asteroids[numAsteroids] = null;
    }

    private void addAsteroid(Asteroid ast) {
        //adds the asteroid passed in to the end of the array
        asteroids[numAsteroids] = ast;
        numAsteroids++;
    }

    private void updateAsteroids() {
        for (int i = 0; i < numAsteroids; i++) {
            // move each asteroid
            asteroids[i].move(dim.width, dim.height);
            //check for collisions with the ship, restart the
            //level if the ship gets hit
            if (asteroids[i].shipCollision(ship)) {
                level--; //restart this level
                numAsteroids = 0;
                return;
            }
            //check for collisions with any of the shots
            for (Shot sht : new ArrayList<>(shots)) {
                if (asteroids[i].shotCollision(sht)) {
                    switch (asteroids[i].hitsLeft){
                        case 1:
                            score += 100;
                            break;
                        case 2:
                            score += 50;
                            break;
                        case 3:
                            score += 20;
                            break;
                    }
                    //if the shot hit an asteroid, delete the shot
                    shots.remove(sht);
                    //split the asteroid up if needed
                    if (asteroids[i].getHitsLeft() > 1) {
                        for (int k = 0; k < asteroids[i].getNumSplit();
                             k++)
                            addAsteroid(
                                    asteroids[i].createSplitAsteroid(
                                            minAstVel, maxAstVel));
                    }
                    //delete the original asteroid
                    deleteAsteroid(i);
                    i--; //don't skip asteroid shifted back into
                    //the deleted asteroid's position
                }
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            //These first two lines allow the asteroids to move
            //while the player chooses when to enter the game.
            //This happens when the player is starting a new life.
            if (!ship.isActive() && !paused)
                ship.setActive(true);
            else {
                paused = !paused; //enter is the pause button
                // grays out the ship if paused
                ship.setActive(!paused);
            }
        } else if (paused || !ship.isActive()) //if the game is
            return; //paused or ship is inactive, do not respond
            //to the controls except for enter to unpause
        else if (e.getKeyCode() == KeyEvent.VK_UP)
            ship.setAccelerating(true);
        else if (e.getKeyCode() == KeyEvent.VK_LEFT)
            ship.setTurningLeft(true);
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            ship.setTurningRight(true);
        else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
            shooting = true; //Start shooting if ctrl is pushed
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP)
            ship.setAccelerating(false);
        else if (e.getKeyCode() == KeyEvent.VK_LEFT)
            ship.setTurningLeft(false);
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            ship.setTurningRight(false);
        else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
            shooting = false;
    }

    public void keyTyped(KeyEvent e) {
    }
}
