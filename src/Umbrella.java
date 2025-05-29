/**
 * Represents a protective umbrella that blocks comets.
 * Has a cooldown timer and only activates for a short time.
 */
/**
 * Represents a protective umbrella that blocks comets.
 * Has a cooldown timer and only activates for a short time.
 */
public class Umbrella {

    private boolean active;
    long lastUsedTime;
    private final int cooldown = 10000; // 10 seconds in milliseconds
    private final int width = 40;
    private final int height = 20;

    /**
     * Constructs the Umbrella with default size and inactive state.
     */
    public Umbrella() {
        active = false;
        lastUsedTime = -cooldown; // So it's usable at game start
    }

    /**
     * Checks if the umbrella is ready to be activated again (cooldown complete).
     *
     * @return true if ready, false otherwise
     */
    public boolean isReady() {
        return System.currentTimeMillis() - lastUsedTime >= cooldown;
    }

    /**
     * Activates the umbrella if it's off cooldown.
     */
    public void activate() {
        if (isReady()) {
            active = true;
            lastUsedTime = System.currentTimeMillis();
        }
    }

    /**
     * Deactivates the umbrella manually.
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Checks if the umbrella is currently active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the width of the umbrella.
     *
     * @return the width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the umbrella.
     *
     * @return the height in pixels
     */
    public int getHeight() {
        return height;
    }
}

 
  