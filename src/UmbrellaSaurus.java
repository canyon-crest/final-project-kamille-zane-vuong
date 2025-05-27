import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;//images stuff
import java.io.IOException;//checking if images are valid
import javax.imageio.ImageIO;//"
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*; //sound effects


enum PowerUpEffect { NONE, SPEED_BOOST, COMET_FREEZE, IMMUNITY }

//master class
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


//game runner class
class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    private State state; //changes if in menu screen or in game
    private Dinosaur dinosaur;//player
    private ArrayList<Comet> comets;//all comets on screen
    private PowerUp powerUp; //powerups duh
    private Timer timer;//keeps track of time
    private int score, lives;//score increments if meteor hits ground, lives are set to 3 and decrement when hit
    private Random random;
    private ArrayList<String> leaderboard;//keeps track of all scores not including the most recent
    private JButton startButton, playAgainButton;
    private int frameCount;
    private BufferedImage dinosaurLeftImage, dinosaurRightImage, backgroundImage, heartImage, umbrellaImage, titleScreen, deathScreen;//main images
    private BufferedImage cometDownImage, cometLeftImage, cometRightImage;//player images
    private BufferedImage powerUpImmunityImage, powerUpFreezeImage, powerUpSpeedImage;//powerup images
    private PowerUpEffect activeEffect;// which powerup was most recently applied
    private int effectTimer;//how long the powerup will be active
    private Umbrella umbrella;// the umbrella which you can use to defend yourself
    
    private Rectangle platform4 = new Rectangle (680, 500, 100, 10);// all platforms
    private Rectangle platform5= new Rectangle (585, 400, 100, 10);
    private Rectangle platform6= new Rectangle(490,500,100,10);
    private Rectangle platform7= new Rectangle (395, 400,100,10);
    private Rectangle platform = new Rectangle(300, 500, 100, 10);
    private Rectangle platform1= new Rectangle (205, 400, 100, 10);
    private Rectangle platform2= new Rectangle(110,500,100,10);
    private Rectangle platform3= new Rectangle (15, 400,100,10);
   
    // Sounds
    private Clip jumpSound, clickSound, gameOverSound, lifeLostSound;

    private enum State { START, GAME, GAME_OVER }

    public GamePanel() {
        state = State.START; //setting up menu screen, instantiates everything and makes leaderboard
        dinosaur = new Dinosaur(350, 525, 60, 30, 5);
        comets = new ArrayList<>();
        powerUp = null;
        score = 0;
        lives = 3;
        random = new Random();
        frameCount = 0;
        activeEffect = PowerUpEffect.NONE;
        effectTimer = 0;
        umbrella = new Umbrella();
        leaderboard = new ArrayList<>();
        leaderboard.add("X");
        leaderboard.add("X");
        leaderboard.add("X");
        timer = new Timer(16, this);
        timer.start();
        

        // Load all images
        try {
            BufferedImage DinosaurLeft = ImageIO.read(getClass().getResource("/DinosaurLeft2.png"));
            BufferedImage DinosaurRight = ImageIO.read(getClass().getResource("/DinosaurRight2.png"));
            BufferedImage CometDown = ImageIO.read(getClass().getResource("/downmeteor.png"));
            BufferedImage CometLeft = ImageIO.read(getClass().getResource("/MetorLeft.png"));
            BufferedImage CometRight = ImageIO.read(getClass().getResource("/MeteorRight.png"));
            BufferedImage PowerUpImmunity = ImageIO.read(getClass().getResource("/PinkCandy.png"));
            BufferedImage PowerUpFreeze = ImageIO.read(getClass().getResource("/BlueCandy.png"));
            BufferedImage PowerUpSpeed = ImageIO.read(getClass().getResource("/YellowCandy.png"));
            BufferedImage Background = ImageIO.read(getClass().getResource("/backgroundgame.png"));
            BufferedImage Heart = ImageIO.read(getClass().getResource("/Heart.png")); 
            BufferedImage Umbrella=ImageIO.read(getClass().getResource("/Umbrella.png"));
            BufferedImage TitleScreen=ImageIO.read(getClass().getResource("/TitleScreen.png"));
            BufferedImage EndScreen=ImageIO.read(getClass().getResource("/deathscreen.png"));

            if (DinosaurLeft == null) throw new IOException("DinosaurLeft2.png not found");
            if (DinosaurRight == null) throw new IOException("DinosaurRight2.png not found");
            if (CometDown == null) throw new IOException("downmeteor.png not found");
            if (CometLeft == null) throw new IOException("MeteorLeft.png not found");
            if (CometRight == null) throw new IOException("MeteorRight.png not found");
            if (PowerUpImmunity == null) throw new IOException("PinkCandy.png not found");
            if (PowerUpFreeze == null) throw new IOException("BlueCandy.png not found");
            if (PowerUpSpeed == null) throw new IOException("YellowCandy.png not found");
            if (Background == null) throw new IOException("backgroundgame.png not found");
            if (Heart == null) throw new IOException("heart.png not found");
            if (Umbrella==null)throw new IOException("umbrella.png not found");

            dinosaurLeftImage =  DinosaurLeft;
            dinosaurRightImage =  DinosaurRight;
            cometDownImage =  CometDown;
            cometLeftImage =  CometLeft;
            cometRightImage =  CometRight;
            powerUpImmunityImage =  PowerUpImmunity;
            powerUpFreezeImage =  PowerUpFreeze;
            powerUpSpeedImage =  PowerUpSpeed;
            backgroundImage =  Background;
            heartImage =  Heart;
            umbrellaImage= Umbrella;
            deathScreen= EndScreen;
            titleScreen= TitleScreen;
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
            heartImage = null;
            umbrellaImage=null;
            deathScreen=null;
            titleScreen=null;
        }

        // load sounds
        try {
            jumpSound = loadSound("/jump.wav");
            clickSound = loadSound("/click.wav");
            gameOverSound = loadSound("/youLost.wav");
            lifeLostSound = loadSound("/liveLost.wav");
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            jumpSound = null;
            clickSound = null;
            gameOverSound = null;
            lifeLostSound = null;
        }

        // make buttons
        setLayout(null);
        startButton = new JButton("Start");
        startButton.setBounds(350, 400, 100, 40);
        startButton.addActionListener(e -> {
            playSound(clickSound);
            startGame();
        });
        add(startButton);

        playAgainButton = new JButton("Play Again");
        playAgainButton.setBounds(350, 400, 100, 40);
        playAgainButton.addActionListener(e -> {
            playSound(clickSound);
            restartGame();
        });
        add(playAgainButton);
        playAgainButton.setVisible(false);

        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
    }

    // helper method to load sound clips
    private Clip loadSound(String path) throws Exception {
        AudioInputStream audioInput = AudioSystem.getAudioInputStream(getClass().getResource(path));
        Clip clip = AudioSystem.getClip();
        clip.open(audioInput);
        return clip;
    }

    // helper method to play those sounds
    private void playSound(Clip clip) {
        if (clip != null) {
            clip.stop(); // Stop if already playing
            clip.setFramePosition(0); // Rewind to start
            clip.start();
        }
    }

    private void startGame() {
        state = State.GAME;//sets up the game, initiating score, lives, frames, comets, and powerups, clearing the gui from the menu
        score = 0;
        lives = 3;
        frameCount = 0;
        comets.clear();
        powerUp = null;
        activeEffect = PowerUpEffect.NONE;
        effectTimer = 0;
        dinosaur.setSpeed(5);
        startButton.setVisible(false);
        playAgainButton.setVisible(false);
        requestFocus();
    }

    private void restartGame() {//updates leaderboard and restarts
        updateLeaderboard();
        startGame();
    }

    private void updateLeaderboard() {//keeps only the largest scores and deletes others 
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
        
        // loads background image
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, null);
        } else {//makes sky blue otherwise
            g2d.setColor(Color.BLUE);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (state == State.START) {//instructional text for user
        	g2d.drawImage(titleScreen,70,50,null);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("UmbrellaSaurus", 280, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Use arrow keys to avoid comets!,", 260, 250);
            g2d.drawString(" Space to Jump", 290, 280);
            g2d.drawString("and Up Arrow to use Umbrella", 260, 310);
        } else if (state == State.GAME) {
            g2d.setColor(Color.ORANGE);//makes all platforms
            g2d.fillRect(platform.x, platform.y, platform.width, platform.height);
            g2d.fillRect(platform1.x, platform1.y, platform1.width, platform1.height);
            g2d.fillRect(platform2.x, platform2.y, platform2.width, platform2.height);
            g2d.fillRect(platform3.x, platform3.y, platform3.width, platform3.height);
            g2d.fillRect(platform4.x, platform4.y, platform4.width, platform4.height);
            g2d.fillRect(platform5.x, platform5.y, platform5.width, platform5.height);
            g2d.fillRect(platform6.x, platform6.y, platform6.width, platform6.height);
            g2d.fillRect(platform7.x, platform7.y, platform7.width, platform7.height);

            // loads dinosaur image, places it at player
            BufferedImage dinoImage = dinosaur.isMovingLeft() ? dinosaurLeftImage : dinosaurRightImage;
            if (dinoImage != null) {
                g2d.drawImage(dinoImage, dinosaur.getX(), dinosaur.getY(), null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(dinosaur.getX(), dinosaur.getY(), dinosaur.getWidth(), dinosaur.getHeight());
            }
            if (umbrella.isActive()) {
                g2d.drawImage(umbrellaImage,dinosaur.getX()+25, dinosaur.getY()-30, null);
            }

            // makes comet images and puts one at every comet place
            if (cometDownImage != null && cometLeftImage != null && cometRightImage != null) {
                for (Comet comet : comets) {
                    BufferedImage cometImg = comet.getDirection() == 0 ? cometDownImage :
                            comet.getDirection() == 1 ? cometLeftImage : cometRightImage;
                    g2d.drawImage(cometImg, comet.getX(), comet.getY(), null);
                }
            } else {//makes circle in case the image doesnt load
                g2d.setColor(Color.RED);
                for (Comet comet : comets) {
                    g2d.fillOval(comet.getX(), comet.getY(), comet.getWidth(), comet.getHeight());
                }
            }

            // add power-up images, depending on the effect of them a different image is used
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
            g2d.setFont(new Font("Arial", Font.PLAIN,18));//text to inform player of their umbrella and powerup status
            g2d.setColor(Color.WHITE);
            if (umbrella.isReady()) {
            	g2d.drawString("Umbrella is Ready![UP]", getWidth()-200, 40);
            }
            if(activeEffect == PowerUpEffect.COMET_FREEZE) {
            	g2d.drawString("Comets are frozen!", getWidth()-180,90);
            }
            else if(activeEffect==PowerUpEffect.IMMUNITY) {
            	g2d.drawString("You're immune to damage!", getWidth()-225,90);
            }
            else if (activeEffect==PowerUpEffect.SPEED_BOOST){
            	g2d.drawString("Speed BOOST!", getWidth()-150, 90);
            }
            
            //scorekeeper
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Score: " + score, 10, 20);

            //lives as hearts in top left
            if (heartImage != null) {
                for (int i = 0; i < lives; i++) {
                    g2d.drawImage(heartImage, 100 + i * 30, 100, null); // spaced 30 pixels
                }
            } else {
                g2d.setColor(Color.RED);
                for (int i = 0; i < lives; i++) {
                    g2d.fillOval(100 + i * 30, 100, 20, 20);
                }//will be red circles if not images
            }
        } else if (state == State.GAME_OVER) {//makes text for postgame
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("YOU LOST!", 320, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Final Score: " + score, 320, 250);
            drawLeaderboard(g2d, 300, 300);
        }
    }

    private void drawLeaderboard(Graphics2D g2d, int x, int y) {//leaderboard draw tool
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
            dinosaur.updatePosition(platform, platform1, platform2, platform3, platform4, platform5, platform6, platform7);;
          

            // spawn power-up
            if (powerUp == null && random.nextInt(100) < 1) {
                PowerUpEffect effect = PowerUpEffect.values()[random.nextInt(3) + 1];

                // choose a random platform for powerup spawn
                Rectangle[] platforms = { platform, platform1,platform2,platform3, platform4, platform5, platform6, platform7 };
                Rectangle chosenPlatform = platforms[random.nextInt(platforms.length)];

                //power-up is within platform bounds
                int powerUpWidth = 20;
                int powerUpHeight = 20;
                int minX = chosenPlatform.x;
                int maxX = chosenPlatform.x + chosenPlatform.width - powerUpWidth;
                int x = random.nextInt(maxX - minX + 1) + minX;
                int y = chosenPlatform.y - 2*powerUpHeight; // put it on top

                powerUp = new PowerUp(x, y, powerUpWidth, powerUpHeight, 360, effect);
                
            }
           if (umbrella.isActive()) {//umbrella functionality
                int umbrellaX = dinosaur.x + 10;
                int umbrellaY = dinosaur.y - umbrella.getHeight();
                int umbrellaW = umbrella.getWidth();
                int umbrellaH = umbrella.getHeight();

                for (int i = 0; i < comets.size(); i++) {
                    Comet comet = comets.get(i);

                    int cometX = comet.x;
                    int cometY = comet.y;
                    int cometW = comet.width;
                    int cometH = comet.height;

                    boolean xOverlap = cometX < umbrellaX + umbrellaW && cometX + cometW > umbrellaX;
                    boolean yOverlap = cometY < umbrellaY + umbrellaH && cometY + cometH > umbrellaY;

                    if (xOverlap && yOverlap) {
                        // break comet
                        comets.remove(i);
                        System.out.print("comet BLOCKED");
                        i--;//avoid skipping comet
                    }
                }

                // umbrella auto-disable after 1 sec
                if (System.currentTimeMillis() - umbrella.lastUsedTime > 1000) {
                    umbrella.deactivate();
                }
            }
            
            // spawn comets
            if (random.nextInt(100) < 5) {
                int cometSpeed = (activeEffect == PowerUpEffect.COMET_FREEZE) ? 0 : (3+ (frameCount / 60) / 10);
                int direction = random.nextInt(3);
                comets.add(new Comet(random.nextInt(750), 0, 8, 14, cometSpeed, direction));
            }

            // update comets position and lives if hit
            for (int i = comets.size() - 1; i >= 0; i--) {
                Comet comet = comets.get(i);
                comet.update();
                if (comet.getY() > getHeight()) {
                    comets.remove(i);
                    score++;
                } else if (dinosaur.getBounds().intersects(comet.getBounds()) && activeEffect != PowerUpEffect.IMMUNITY) {
                    comets.remove(i);
                    lives--;
                    playSound(lifeLostSound); // Play life lost sound
                    if (lives <= 0) {
                        playSound(gameOverSound); // Play game over sound
                    }
                }
            }

            // update power-up, checks to see if player touches
            if (powerUp != null) {
                powerUp.update();
                if (powerUp.isExpired()) {
                    powerUp = null;
                } else if (dinosaur.getBounds().intersects(powerUp.getBounds())) {
                    activeEffect = powerUp.getEffect();
                    if (activeEffect == PowerUpEffect.SPEED_BOOST) {
                        dinosaur.incSpeed();
                        score += 5;
                    } else if (activeEffect == PowerUpEffect.COMET_FREEZE) {
                        score += 5;
                    } else if (activeEffect == PowerUpEffect.IMMUNITY) {
                        score += 5;
                        
                    }
                    effectTimer = 300;
                    powerUp = null;
                }
            }

            // effect timer
            if (effectTimer > 0) {
                effectTimer--;
                if (effectTimer <= 0) {
                    activeEffect = PowerUpEffect.NONE;
                    
                }
            }

            //game over
            if (lives <= 0) {
                state = State.GAME_OVER;
                playAgainButton.setVisible(true);
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {//keybinds to move and use umbrella
        if (state == State.GAME) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT && dinosaur.getX() > 0) {
                dinosaur.moveLeft(true);
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT && dinosaur.getX() < getWidth() - dinosaur.getWidth()) {
                dinosaur.moveRight(false);
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                dinosaur.jump();
                playSound(jumpSound);
            }
        
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                umbrella.activate();
            }
        }
    }

    @Override
    
    
    public void keyReleased(KeyEvent e) {//stops moving if not inputting movement keys
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

abstract class Thing {//  class in which meteors and dinosaurs are made to keep track of their position
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
    private final int JUMP_STRENGTH = -15;//jump power
    private final int GROUND_Y = 530;

    public Dinosaur(int x, int y, int width, int height, int speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.movingLeft = false;
    }

    public void moveLeft(boolean left) {//movement methods
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

    public void setSpeed(int speed) {//used to reset speed
        this.speed = speed;
    }

    public void incSpeed() {//used when speed powerup picked up to increase speed permanently
        speed += 5;
    }

    public void jump() {// sends the player upwards
        if (!jumping) {
            velocityY = JUMP_STRENGTH;
            jumping = true;
        }
    }
//checks if player is on top of any platform
    public void updatePosition(Rectangle platform, Rectangle platform1, Rectangle platform2, Rectangle platform3, Rectangle platform4, Rectangle platform5, Rectangle platform6, Rectangle platform7) {
        y += velocityY;
        velocityY += GRAVITY;
        if (y + height >= platform.y && y + height <= platform.y + 10 &&
                x + width > platform.x && x < platform.x + platform.width &&
                velocityY >= 0) {
            y = platform.y - height;
            velocityY = 0;
            jumping = false;
        }
        if (y + height >= platform1.y && y + height <= platform1.y + 10 &&
                x + width > platform1.x && x < platform1.x + platform1.width &&
                velocityY >= 0) {
            y = platform1.y - height;
            velocityY = 0;
            jumping = false;
        }
        if (y + height >= platform2.y && y + height <= platform2.y + 10 &&
                x + width > platform2.x && x < platform2.x + platform2.width &&
                velocityY >= 0) {
            y = platform2.y - height;
            velocityY = 0;
            jumping = false;
        }
        if (y + height >= platform3.y && y + height <= platform3.y + 10 &&
                x + width > platform3.x && x < platform3.x + platform3.width &&
                velocityY >= 0) {
            y = platform3.y - height;
            velocityY = 0;
            jumping = false;
        }
        if (y + height >= platform4.y && y + height <= platform4.y + 10 &&
                x + width > platform4.x && x < platform4.x + platform4.width &&
                velocityY >= 0) {
            y = platform4.y - height;
            velocityY = 0;
            jumping = false;
        }
        if (y + height >= platform5.y && y + height <= platform5.y + 10 &&
                x + width > platform5.x && x < platform5.x + platform5.width &&
                velocityY >= 0) {
            y = platform5.y - height;
            velocityY = 0;
            jumping = false;
        }
        if (y + height >= platform6.y && y + height <= platform6.y + 10 &&
                x + width > platform6.x && x < platform6.x + platform6.width &&
                velocityY >= 0) {
            y = platform6.y - height;
            velocityY = 0;
            jumping = false;
        }
        if (y + height >= platform7.y && y + height <= platform7.y + 10 &&
                x + width > platform7.x && x < platform7.x + platform7.width &&
                velocityY >= 0) {
            y = platform7.y - height;
            velocityY = 0;
            jumping = false;
        }
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
//comets have a variable direction and a constant speed that increases as the game goes on
class Comet extends Thing {
    private int speed;
    private int direction;

    public Comet(int x, int y, int width, int height, int speed, int direction) {
        super(x, y, width, height);
        this.speed = speed;
        this.direction = direction;
    }

    public void update() {
        y += speed;
        if (direction == 1) x -= 0.5 * speed;
        else if (direction == 2) x += 0.5 * speed;
    }

    public int getDirection() {
        return direction;
    }
}

class PowerUp extends Thing {//have a timer to despawn and a specific effect bound to them
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

   

    public PowerUpEffect getEffect() {
        return effect;
    }
    public boolean isExpired() {
    	return timer <= 0;
    }
}
