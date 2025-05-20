import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

// Move PowerUpEffect enum to file level
enum PowerUpEffect { NONE, SPEED_BOOST, COMET_FREEZE, IMMUNITY }

public class UmbrellaSaurus extends JFrame {
    public UmbrellaSaurus() {
        setTitle("UmbrellaSaurus");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new GamePanel());
        setVisible(true);
        setFocusable(true);
    }

    public static void main(String[] args) {
        new UmbrellaSaurus();
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
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
    private BufferedImage dinosaurLeftImage, dinosaurRightImage, backgroundImage;
    private BufferedImage cometDownImage, cometLeftImage, cometRightImage;
    private BufferedImage powerUpImmunityImage, powerUpFreezeImage, powerUpSpeedImage;
    private PowerUpEffect activeEffect;
    private int effectTimer;
    Rectangle platform = new Rectangle(300, 500, 100, 10);

    private enum State { START, GAME, GAME_OVER }

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
        setFocusable(true);
        requestFocusInWindow();

        // Load and resize images with fallback
        try {
            BufferedImage originalDinosaurLeft = ImageIO.read(getClass().getResource("/DinosaurLeft2.png"));
            BufferedImage originalDinosaurRight = ImageIO.read(getClass().getResource("/DinosaurRight2.png"));
            BufferedImage originalCometDown = ImageIO.read(getClass().getResource("/downmeteor.png"));
            BufferedImage originalCometLeft = ImageIO.read(getClass().getResource("/MetorLeft.png"));
            BufferedImage originalCometRight = ImageIO.read(getClass().getResource("/MeteorRight.png"));
            BufferedImage originalPowerUpImmunity = ImageIO.read(getClass().getResource("/PinkCandy.png"));
            BufferedImage originalPowerUpFreeze = ImageIO.read(getClass().getResource("/YellowCandy.png"));
            BufferedImage originalPowerUpSpeed = ImageIO.read(getClass().getResource("/BlueCandy.png"));
            BufferedImage originalBackground = ImageIO.read(getClass().getResource("/backgroundgame.png"));

            if (originalDinosaurLeft == null) throw new IOException("DinosaurLeft2.png not found");
            if (originalDinosaurRight == null) throw new IOException("DinosaurRight2.png not found");
            if (originalCometDown == null) throw new IOException("downmeteor.png not found");
            if (originalCometLeft == null) throw new IOException("MeteorLeft.png not found");
            if (originalCometRight == null) throw new IOException("MeteorRight.png not found");
            if (originalPowerUpImmunity == null) throw new IOException("PinkCandy.png not found");
            if (originalPowerUpFreeze == null) throw new IOException("YellowCandy.png not found");
            if (originalPowerUpSpeed == null) throw new IOException("BlueCandy.png not found");
            if (originalBackground == null) throw new IOException("backgroundgame.png not found");

            dinosaurLeftImage = originalDinosaurLeft;
            dinosaurRightImage = originalDinosaurRight;
            cometDownImage =originalCometDown;
            cometLeftImage =originalCometLeft;
            cometRightImage = originalCometRight;
            powerUpImmunityImage = originalPowerUpImmunity;
            powerUpFreezeImage = originalPowerUpFreeze;
            powerUpSpeedImage = originalPowerUpSpeed;
            backgroundImage = originalBackground; // No resize for background
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
            dinosaurLeftImage = null;
            dinosaurRightImage = null;
            cometDownImage = null;
            cometLeftImage = null;
            cometRightImage = null;
            powerUpImmunityImage = null;
            powerUpFreezeImage = null;
            powerUpSpeedImage = null;
            backgroundImage = null;
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

    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        if (original == null) return null;
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
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, null);
        } else {
            g2d.setColor(Color.BLUE);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (state == State.START) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("UmbrellaSaurus", 300, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Use arrow keys to avoid comets!", 280, 250);
        } else if (state == State.GAME) {
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(platform.x, platform.y, platform.width, platform.height);

            // Draw dinosaur based on direction
            BufferedImage dinoImage = dinosaur.isMovingLeft() ? dinosaurLeftImage : dinosaurRightImage;
            if (dinoImage != null) {
                g2d.drawImage(dinoImage, dinosaur.getX(), dinosaur.getY(), null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(dinosaur.getX(), dinosaur.getY(), dinosaur.getWidth(), dinosaur.getHeight());
            }

            // Draw comets
            if (cometDownImage != null && cometLeftImage != null && cometRightImage != null) {
                for (Comet comet : comets) {
                    BufferedImage cometImg = comet.getDirection() == 0 ? cometDownImage :
                                          comet.getDirection() == 1 ? cometLeftImage : cometRightImage;
                    g2d.drawImage(cometImg, comet.getX(), comet.getY(), null);
                }
            } else {
                g2d.setColor(Color.RED);
                for (Comet comet : comets) {
                    g2d.fillOval(comet.getX(), comet.getY(), comet.getWidth(), comet.getHeight());
                }
            }

            // Draw power-up
            if (powerUp != null) {
                BufferedImage powerUpImg = powerUp.getEffect() == PowerUpEffect.IMMUNITY ? powerUpImmunityImage :
                                        powerUp.getEffect() == PowerUpEffect.COMET_FREEZE ? powerUpFreezeImage :
                                        powerUpSpeedImage;
                if (powerUpImg != null) {
                    g2d.drawImage(powerUpImg, powerUp.getX(), powerUp.getY(), null);
                } else {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());
                }
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
            dinosaur.updatePosition(platform);

            // Spawn power-up (1% chance if none exists)
            if (powerUp == null && random.nextInt(100) < 1) {
                PowerUpEffect effect = PowerUpEffect.values()[random.nextInt(3) + 1]; // Random effect (1-3)
                powerUp = new PowerUp(random.nextInt(750), 530, 20, 20, 360, effect);
            }

            // Spawn comets with random direction
            if (random.nextInt(100) < 5) {
                int cometSpeed = (activeEffect == PowerUpEffect.COMET_FREEZE) ? 0 : (3 + (frameCount / 60) / 10);
                int direction = random.nextInt(3); // 0: down, 1: left, 2: right
                comets.add(new Comet(random.nextInt(750), 0, 8, 14, cometSpeed, direction));
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
                    activeEffect = powerUp.getEffect();
                    if (activeEffect == PowerUpEffect.SPEED_BOOST) {
                        dinosaur.incSpeed(); // Increase speed by 5
                        score += 5;
                    } else if (activeEffect == PowerUpEffect.COMET_FREEZE) {
                        score += 5;
                    } else if (activeEffect == PowerUpEffect.IMMUNITY) {
                        score += 5;
                    }
                    effectTimer = 360; // 6 seconds
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
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                dinosaur.jump();
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
    public void keyTyped(KeyEvent e) {}
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
    private int velocityY = 0;
    private boolean jumping = false;
    private final int GRAVITY = 1;
    private final int JUMP_STRENGTH = -15;
    private final int GROUND_Y = 530; // Adjust as needed

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
        speed += 5;
    }

    public void jump() {
        if (!jumping) {
            velocityY = JUMP_STRENGTH;
            jumping = true;
        }
    }

    public void updatePosition(Rectangle platform) {
        y += velocityY;
        velocityY += GRAVITY;
        // Land on platform
        if (y + height >= platform.y && y + height <= platform.y + 10 &&
            x + width > platform.x && x < platform.x + platform.width &&
            velocityY >= 0) {
            y = platform.y - height;
            velocityY = 0;
            jumping = false;
        }
        // Land on ground
        if (y >= GROUND_Y) {
            y = GROUND_Y;
            velocityY = 0;
            jumping = false;
            
        }
    }

    public boolean isJumping() {
        return jumping;
    }
}

class Comet extends Thing {
    private int speed;
    private int direction; // 0: down, 1: left, 2: right

    public Comet(int x, int y, int width, int height, int speed, int direction) {
        super(x, y, width, height);
        this.speed = speed;
        this.direction = direction;
    }

    public void update() {
        y += speed;
        if (direction == 1) x -= 1; // Slightly left
        else if (direction == 2) x += 1; // Slightly right
    }

    public int getDirection() {
        return direction;
    }
}

class PowerUp extends Thing {
    private int timer;
    private PowerUpEffect effect;

    public PowerUp(int x, int y, int width, int height, int duration, PowerUpEffect effect) {
        super(x, y, width, height);
        this.timer = duration;
        this.effect = effect;
    }

    public void update() {
        timer--;
    }

    public boolean isExpired() {
        return timer <= 0;
    }

    public PowerUpEffect getEffect() {
        return effect;
    }
}
