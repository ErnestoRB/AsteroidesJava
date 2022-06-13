package com.ernestorb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AsteroidsGame extends JPanel implements Runnable, KeyListener {

    private int currentScore = 0;

    JFrame ventana;
    Thread thread;
    Dimension dim;

    long endTime, startTime, framePeriod;

    Ship ship;
    boolean paused; // True if the game is paused. Enter is the pause key
    int numShots;
    boolean shooting;
    boolean ovniAlreadyGenerated;
    Ovni ovni;
    Vector<Shot> shots;
    Vector<Asteroid> asteroids;
    Map<Integer, Integer> scores = new HashMap<>();
    boolean hasCollided;

    double astRadius, minAstVel, maxAstVel; //values used to create
    //asteroids
    int astNumHits, astNumSplit;

    int level; //the current level number

    public void init() {
        setSize(500, 500);
        shots = new Vector<>(100);
        asteroids = new Vector<>(100);
        level = 0; //will be incremented to 1 when first level is set up
        astRadius = 40; //values used to create the asteroids
        minAstVel = .5;
        maxAstVel = 3;
        astNumHits = 3;
        astNumSplit = 2;
        endTime = 0;
        startTime = 0;
        framePeriod = 25;
        hasCollided = false;
        ovniAlreadyGenerated = false;
        dim = getSize();
        ventana = new JFrame("Asteroides");
        ventana.setSize(dim);
        ventana.add(this);
        ventana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ventana.addKeyListener(this); //tell it to listen for KeyEvents
        ventana.setResizable(false);
        thread = new Thread(this);
        thread.start();
    }

    public void setUpNextLevel() {
        shots.clear();
        if (!hasCollided) {
            level++;
            scores.put(level, currentScore);
        } else {
            asteroids.clear();
            currentScore = 0;
            hasCollided = false;
        }
        ovniAlreadyGenerated = false;
        ovni = null;
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
        int numAsteroids = 4 + 2 * (level - 1); // SIEMPRE se aparecen 4 y 2 cada nivel
        if (numAsteroids > 12) {
            numAsteroids = 12;
        }
        for (int i = 0; i < numAsteroids; i++)
            asteroids.add(new Asteroid(Math.random() * dim.width,
                    Math.random() * dim.height, astRadius, minAstVel,
                    maxAstVel, astNumHits, astNumSplit));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, 500, 500);

        for (Shot sht : shots) {
            sht.draw(g);
        }
        for (Asteroid ast : asteroids) {
            ast.draw(g);
        }
        ship.draw(g); //draw the ship
        g.setColor(Color.cyan); //Display level number in top left corner
        try {
            g.setFont(Font.createFont(Font.TRUETYPE_FONT, new File("src/PressStart2P-Regular.ttf")).deriveFont(12.0F));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        g.drawString("Level " + level, 20, 20);
        g.setColor(Color.green);
        long accumulatedScore = scores.values().stream().mapToInt(value -> value).sum();
        if (accumulatedScore > 99990) {
            accumulatedScore -= 99990;
        }
        g.drawString("LScore " + currentScore, 20, 40);
        g.drawString("TScore " + accumulatedScore, 20, 60);
        g.drawString("Activar escudo (SHIFT)", 20, 480);
        if (ship.isShieldActive()) {
            g.setColor(Color.green);
            g.drawString("E", 20, 80);
        }

        if(ovni != null)
            ovni.draw(g);
    }


    public void run() {
        ventana.setVisible(true);
        while (true) {
            startTime = System.currentTimeMillis();

            //start next level when all asteroids are destroyed
            if (hasCollided || level == 0 || (asteroids.size() == 0 && ovni == null))
                setUpNextLevel();

            if (!paused) {
                ship.move(dim.width, dim.height); // move the ship
                if (ovni != null) {
                    ovni.move(dim.width, dim.height);
                    if (ovni.canShoot()) {
                        shots.add(ovni.shoot());
                    }
                    for (Shot sht : new ArrayList<>(shots)) {
                        if(sht.shooter == ship && ovni.shotCollision(sht)){
                            ovni.decreaseHitsLeft();
                            shots.remove(sht);
                        }
                    }
                    if (ovni.getHitsLeft() == 0) {
                        ovni = null;
                    }
                }

                for (Shot sht : new ArrayList<>(shots)) {
                    if(sht.shooter == ovni && ship.shotCollision(sht)){
                        shots.remove(sht);
                        hasCollided = true;
                    }
                }

                //move shots and remove dead shots
                for (Shot sht : new ArrayList<>(shots)) {
                    sht.move(dim.width, dim.height);
                    if (sht.getLifeLeft() <= 0) {
                        shots.remove(sht);
                    }
                }
                //move asteroids and check for collisions
                updateAsteroids();

                if (!ovniAlreadyGenerated && asteroids.stream().filter(asteroid -> asteroid.hitsLeft == 3).count() == 3) {
                    ovni = new Ovni(Math.random(), 100, 25); //el tercer argumento indica el delay entre cada disparo de bala
                    ovniAlreadyGenerated = true;
                }

                if (shooting && ship.canShoot()) {
                    //add a shot on to the array
                    shots.add(ship.shoot());
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

    private void updateAsteroids() {
        for (Asteroid ast : new ArrayList<>(asteroids)) {
            // move each asteroid
            ast.move(dim.width, dim.height);
            //check for collisions with the ship, restart the
            //level if the ship gets hit
            if (ast.shipCollision(ship)) {
                hasCollided = true;
                return;
            }
            //check for collisions with any of the shots
            for (Shot sht : new ArrayList<>(shots)) {
                if (ast.shotCollision(sht) && sht.shooter instanceof Ship) {
                    switch (ast.hitsLeft) {
                        case 1:
                            currentScore += 100;
                            break;
                        case 2:
                            currentScore += 50;
                            break;
                        case 3:
                            currentScore += 20;
                            break;
                    }
                    //if the shot hit an asteroid, delete the shot
                    shots.remove(sht);
                    //split the asteroid up if needed
                    asteroids.remove(ast);
                    if (ast.getHitsLeft() > 1) {
                        for (int k = 0; k < ast.getNumSplit();
                             k++)
                            asteroids.add(ast.createSplitAsteroid(
                                    minAstVel, maxAstVel));
                    }
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
        else if (e.getKeyCode() == KeyEvent.VK_SHIFT && ship.canShieldBeActivated())
            ship.updateLastShieldUsed();
        else if (e.getKeyCode() == KeyEvent.VK_UP || Character.toUpperCase(e.getKeyChar()) == 'W')
            ship.setAccelerating(true);
        else if (e.getKeyCode() == KeyEvent.VK_LEFT || Character.toUpperCase(e.getKeyChar()) == 'A')
            ship.setTurningLeft(true);
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT || Character.toUpperCase(e.getKeyChar()) == 'D')
            ship.setTurningRight(true);
        else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
            shooting = true; //Start shooting if ctrl is pushed
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || Character.toUpperCase(e.getKeyChar()) == 'W')
            ship.setAccelerating(false);
        else if (e.getKeyCode() == KeyEvent.VK_LEFT || Character.toUpperCase(e.getKeyChar()) == 'A')
            ship.setTurningLeft(false);
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT || Character.toUpperCase(e.getKeyChar()) == 'D')
            ship.setTurningRight(false);
        else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
            shooting = false;
    }

    public void keyTyped(KeyEvent e) {
    }
}
