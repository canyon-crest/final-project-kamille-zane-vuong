import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class KamboomAvoidGame extends JFrame {
    public KamboomAvoidGame() {
        setTitle("Kaboom! Avoid");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new GamePanel());
        setVisible(true);
    }

    public static void main(String[] args) {
        new KamboomAvoidGame();
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    private enum State { START, GAME, GAME_OVER }
    private State state;
    private Bucket bucket;
    private ArrayList<Bomb> bombs;
    private Timer timer;
    private int score, lives;
    private Random random;
    private ArrayList<Integer> leaderboard;
    private JButton startButton, playAgainButton;

    public GamePanel() {
        state = State.START;
        bucket = new Bucket(350, 550, 50, 20, 5);
        bombs = new ArrayList<>();
        score = 0;
        lives = 3;
        random = new Random();
        leaderboard = new ArrayList<>();
        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        // Initialize buttons
        setLayout(null); // Absolute positioning for buttons
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

    private void startGame() {
        state = State.GAME;
        score = 0;
        lives = 3;
        bombs.clear();
        startButton.setVisible(false);
        playAgainButton.setVisible(false);
        requestFocus();
    }

    private void restartGame() {
        updateLeaderboard();
        startGame();
    }

    private void updateLeaderboard() {
        leaderboard.add(score);
        Collections.sort(leaderboard, Collections.reverseOrder());
        if (leaderboard.size() > 5) {
            leaderboard.subList(5, leaderboard.size()).clear();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (state == State.START) {
            // Draw start screen
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("Kaboom! Avoid", 300, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Use arrow keys to avoid bombs!", 280, 250);
            drawLeaderboard(g2d, 300, 300);
        } else if (state == State.GAME) {
            // Draw game elements
            g2d.setColor(Color.BLUE);
            g2d.fillRect(bucket.getX(), bucket.getY(), bucket.getWidth(), bucket.getHeight());

            g2d.setColor(Color.RED);
            for (Bomb bomb : bombs) {
                g2d.fillOval(bomb.getX(), bomb.getY(), bomb.getWidth(), bomb.getHeight());
            }

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Score: " + score, 10, 20);
            g2d.drawString("Lives: " + lives, 10, 40);
        } else if (state == State.GAME_OVER) {
            // Draw game over screen
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("Game Over!", 320, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Final Score: " + score, 320, 250);
            drawLeaderboard(g2d, 300, 300);
        }
    }

    private void drawLeaderboard(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("Leaderboard:", x, y);
        for (int i = 0; i < leaderboard.size() && i < 5; i++) {
            g2d.drawString((i + 1) + ". " + leaderboard.get(i), x, y + 30 * (i + 1));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == State.GAME) {
            // Spawn bombs (5% chance per frame)
            if (random.nextInt(100) < 5) {
                bombs.add(new Bomb(random.nextInt(750), 0, 10, 10, 3));
            }

            // Update bombs
            for (int i = bombs.size() - 1; i >= 0; i--) {
                Bomb bomb = bombs.get(i);
                bomb.update();
                // Score point if bomb reaches bottom
                if (bomb.getY() > getHeight()) {
                    bombs.remove(i);
                    score++; // Increment score for missed bomb
                }
                // Lose life if bomb hits bucket
                else if (bucket.getBounds().intersects(bomb.getBounds())) {
                    bombs.remove(i);
                    lives--;
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
            if (e.getKeyCode() == KeyEvent.VK_LEFT && bucket.getX() > 0) {
                bucket.moveLeft();
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT && bucket.getX() < getWidth() - bucket.getWidth()) {
                bucket.moveRight();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

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
}

class Bucket {
    private int x, y, width, height, speed;

    public Bucket(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void moveLeft() { x -= speed; }
    public void moveRight() { x += speed; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}

class Bomb {
    private int x, y, width, height, speed;

    public Bomb(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void update() { y += speed; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}





