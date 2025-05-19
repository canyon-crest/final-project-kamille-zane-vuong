import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class UmbrellaSaurus extends JFrame {
    public UmbrellaSaurus() {
        setTitle("UmbrellaSaurus");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new GamePanel());
        setVisible(true);
    }

    public static void main(String[] args) {
        new UmbrellaSaurus();
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    private enum State { START, GAME, GAME_OVER }
    private enum PowerUpEffect { NONE, SPEED_BOOST, COMET_FREEZE, IMMUNITY }
    private State state;
    private Dinosaur dinosaur;
    private ArrayList<Comet> comets;
    private PowerUp powerUp;
    private Timer timer;
    private int score, lives;
    private Random random;
    private ArrayList<String> leaderboard;
    private JButton startButton, playAgainButton;
    private int frameCount;
    private BufferedImage dinosaurLeftImage, dinosaurRightImage, cometImage, powerUpImage, backgroundImage;
    private PowerUpEffect activeEffect;
    private int effectTimer;

    public GamePanel() {
        state = State.START;
        dinosaur = new Dinosaur(350, 525, 60, 30, 5); // Larger size: 60x30
        comets = new ArrayList<>();
        powerUp = null;
        score = 0;
        lives = 3;
        random = new Random();
        frameCount = 0;
        activeEffect = PowerUpEffect.NONE;
        effectTimer = 0;
        leaderboard = new ArrayList<>();
        leaderboard.add("X");
        leaderboard.add("X");
        leaderboard.add("X");
        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        // Load and resize images
        try {
            BufferedImage originalDinosaurLeft = ImageIO.read(getClass().getResource("/DinosaurLeft2.png"));
            BufferedImage originalDinosaurRight = ImageIO.read(getClass().getResource("/DinosaurRight2.png"));
            BufferedImage originalComet = ImageIO.read(getClass().getResource("/downmeteor.png"));
            BufferedImage originalPowerUp = ImageIO.read(getClass().getResource("/PinkCandy.png"));
            BufferedImage originalBackground=ImageIO.read(getClass().getResource("/backgroundgame.png"));
            dinosaurLeftImage = originalDinosaurLeft;
            dinosaurRightImage =originalDinosaurRight;
            cometImage = originalComet;
            powerUpImage = originalPowerUp;
            backgroundImage=originalBackground;
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
            dinosaurLeftImage = null;
            dinosaurRightImage = null;
            cometImage = null;
            powerUpImage = null;
            backgroundImage=null;
        }

        // Initialize buttons
        setLayout(null);
        startButton = new JButton("Start");
        startButton.setBounds(350, 400, 100, 40);
        startButton.addActionListener(e -> startGame());
        add(startButton);

        playAgainButton = new JButton("Play Again");
        playAgainButton.setBounds(350, 400, 100, 40);
        playAgainButton.addActionListener(e -> restartGame());
        add(playAgainButton);
        playAgainButton.setVisible(false);

        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
    }

    // Helper method to resize images
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resized;
    }

    private void startGame() {
        state = State.GAME;
        score = 0;
        lives = 3;
        frameCount = 0;
        comets.clear();
        powerUp = null;
        activeEffect = PowerUpEffect.NONE;
        effectTimer = 0;
        dinosaur.setSpeed(5); // Reset speed
        startButton.setVisible(false);
        playAgainButton.setVisible(false);
        requestFocus();
    }

    private void restartGame() {
        updateLeaderboard();
        startGame();
    }

    private void updateLeaderboard() {
        leaderboard.removeIf(s -> s.equals("X"));
        leaderboard.add(String.valueOf(score));
        leaderboard.sort((a, b) -> {
            if (a.equals("X")) return 1;
            if (b.equals("X")) return -1;
            return Integer.compare(Integer.parseInt(b), Integer.parseInt(a));
        });
        if (leaderboard.size() > 3) {
            leaderboard.subList(3, leaderboard.size()).clear();
        }
        while (leaderboard.size() < 3) {
            leaderboard.add("X");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        g2d.drawImage(backgroundImage, 100 ,100, null);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (state == State.START) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("UmbrellaSaurus", 300, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Use arrow keys to avoid comets!", 280, 250);
        } else if (state == State.GAME) {
            // Draw dinosaur based on direction
            BufferedImage dinoImage = dinosaur.isMovingLeft() ? dinosaurLeftImage : dinosaurRightImage;
            if (dinoImage != null) {
                g2d.drawImage(dinoImage, dinosaur.getX(), dinosaur.getY(), null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(dinosaur.getX(), dinosaur.getY(), dinosaur.getWidth(), dinosaur.getHeight());
            }

            // Draw comets
            if (cometImage != null) {
                for (Comet comet : comets) {
                    g2d.drawImage(cometImage, comet.getX(), comet.getY(), null);
                }
            } else {
                g2d.setColor(Color.RED);
                for (Comet comet : comets) {
                    g2d.fillOval(comet.getX(), comet.getY(), comet.getWidth(), comet.getHeight());
                }
            }

            // Draw power-up
            if (powerUp != null && powerUpImage != null) {
                g2d.drawImage(powerUpImage, powerUp.getX(), powerUp.getY(), null);
            } else if (powerUp != null) {
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());
            }

            // Draw score and lives
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Score: " + score, 10, 20);
            g2d.drawString("Lives: " + lives, 10, 40);
        } else if (state == State.GAME_OVER) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("YOU LOST!", 320, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Final Score: " + score, 320, 250);
            drawLeaderboard(g2d, 300, 300);
        }
    }

    private void drawLeaderboard(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("Leaderboard:", x, y);
        for (int i = 0; i < leaderboard.size(); i++) {
            g2d.drawString((i + 1) + ". " + leaderboard.get(i), x, y + 30 * (i + 1));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == State.GAME) {
            frameCount++;

            // Spawn power-up (1% chance if none exists)
            if (powerUp == null && random.nextInt(100) < 1) {
                powerUp = new PowerUp(random.nextInt(750), 530, 20, 20, 300);
            }

            // Spawn comets
            if (random.nextInt(100) < 5) {
                int cometSpeed = (activeEffect == PowerUpEffect.COMET_FREEZE) ? 0 : (3 + (frameCount / 60) / 10);
                comets.add(new Comet(random.nextInt(750), 0, 8, 14, cometSpeed));
            }

            // Update comets
            for (int i = comets.size() - 1; i >= 0; i--) {
                Comet comet = comets.get(i);
                comet.update();
                if (comet.getY() > getHeight()) {
                    comets.remove(i);
                    score++;
                } else if (dinosaur.getBounds().intersects(comet.getBounds()) && activeEffect != PowerUpEffect.IMMUNITY) {
                    comets.remove(i);
                    lives--;
                }
            }

            // Update power-up
            if (powerUp != null) {
                powerUp.update();
                if (powerUp.isExpired()) {
                    powerUp = null;
                } else if (dinosaur.getBounds().intersects(powerUp.getBounds())) {
                    int effect = random.nextInt(3);
                    if (effect == 0) {
                        activeEffect = PowerUpEffect.SPEED_BOOST;
                      
                        score+=5;
                        dinosaur.incSpeed(); // increase speed by 5
                    } else if (effect == 1) {
                        activeEffect = PowerUpEffect.COMET_FREEZE;
                        score+=5;
                    } else {
                        activeEffect = PowerUpEffect.IMMUNITY;
                        score+=5;
                    }
                    effectTimer = 360;
                    
                    // 5 seconds
                    powerUp = null;
                }
            }

            // Update effect timer
            if (effectTimer > 0) {
                effectTimer--;
                if (effectTimer <= 0) {
                    activeEffect = PowerUpEffect.NONE;
                    dinosaur.setSpeed(5); // Reset speed
                }
            }

            // Check game over
            if (lives <= 0) {
                state = State.GAME_OVER;
                playAgainButton.setVisible(true);
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (state == State.GAME) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT && dinosaur.getX() > 0) {
                dinosaur.moveLeft(true);
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT && dinosaur.getX() < getWidth() - dinosaur.getWidth()) {
                dinosaur.moveRight(false);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (state == State.GAME) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                dinosaur.stopMoving();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}

abstract class Thing {
    protected int x, y, width, height;

    public Thing(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}

class Dinosaur extends Thing {
    private int speed;
    private boolean movingLeft;

    public Dinosaur(int x, int y, int width, int height, int speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.movingLeft = false;
    }

    public void moveLeft(boolean left) {
        x -= speed;
        movingLeft = left;
    }

    public void moveRight(boolean left) {
        x += speed;
        movingLeft = left;
    }

    public void stopMoving() {
        movingLeft = false;
    }

    public boolean isMovingLeft() {
        return movingLeft;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
    public void incSpeed() {
    	speed+=5;
    }
}

class Comet extends Thing {
    private int speed;

    public Comet(int x, int y, int width, int height, int speed) {
        super(x, y, width, height);
        this.speed = speed;
    }

    public void update() {
        y += speed;
    }
}

class PowerUp extends Thing {
    private int timer;

    public PowerUp(int x, int y, int width, int height, int duration) {
        super(x, y, width, height);
        this.timer = duration;
    }

    public void update() {
        timer--;
    }

    public boolean isExpired() {
        return timer <= 0;
    }
}
