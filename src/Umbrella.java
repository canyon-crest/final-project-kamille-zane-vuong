
public class Umbrella {


	    private boolean active;
	    long lastUsedTime;
	    private final int cooldown = 10000; // 10 seconds in milliseconds
	    private final int width = 40;
	    private final int height = 20;

	    public Umbrella() {
	        active = false;
	        lastUsedTime = -cooldown; // So it's usable at game start
	    }

	    public boolean isReady() {
	    	
	        return System.currentTimeMillis() - lastUsedTime >= cooldown;
	    }//checks if umbrella cooldown is ready

	    public void activate() {
	        if (isReady()) {
	            active = true;
	            lastUsedTime = System.currentTimeMillis();
	            //makes it active and resets cooldown
	        }
	    }

	    public void deactivate() {
	        active = false;
	    }

	    public boolean isActive() {
	        return active;
	    }

	    public int getWidth() {
	        return width;
	    }

	    public int getHeight() {
	        return height;
	    }
	}



