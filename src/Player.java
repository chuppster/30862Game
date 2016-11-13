import java.util.Timer;
import java.lang.*;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;
    private static final float FIRE_RATE = 0.25f;
    private ScoreKeep scoreKeeper = new ScoreKeep();
    private Amunition ammo = new Amunition(FIRE_RATE);
    private boolean onGround;
    private int health;
    private float lastposx;
    private float lastposy;
    private int timesincemove;
    private boolean invincible;
    private long sleeptime = 10;
    private boolean isgrav = true;

    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        health = 20;
//        System.out.println("Health set.");
        lastposx=this.getX();
        lastposy=this.getY();
        timesincemove = 0;
        Timer tim = new Timer();
		tim.schedule(new RegenTimer(), 0);
		
		
    }
    
    public void setGravity(boolean grav)
    {
    	this.isgrav = grav;
    }
    
    public boolean isGravity(){
    	return(isgrav);
    }
    
    private void makeInvincible()
    {
    	long startTime = System.currentTimeMillis();
    	invincible = true;//make invincible
    	float startposx = this.getX();
    	//System.out.println("I'M INVINCIBLE!");
    	while((System.currentTimeMillis() - startTime <= 1000) && (Math.abs(this.getX() - startposx) <= 640)) //While less than a second has passed
    	{
    		try{
    			Thread.sleep(10);//wait a bit
    		}catch(Exception e){System.out.println("uh oh.");}
    	}
    	invincible = false;//then no longer invincible
    	//System.out.println("I'M no longer INVINCIBLE!");
    	
    }
    
    public void setInvincible()
    {
    	Timer tim2 = new Timer();
    	tim2.schedule(new invincibleTimer(), 0);
    }
    
    public boolean getInvincible()
    {
    	return this.invincible;
    }
    
    public int getHealth()
    {
    	return health;
    }
    
    public void subHealth(int val)
    {
    	if(health - val >= 40)
    	{
    		health = 40;
    	}
    	else
    	{
    		health -= val;
    	}
    }

    public void regeneration()
    {
    	
    	System.out.println("Begin Regeneration");
    	long starttime = System.currentTimeMillis();
    	float lastpos = this.getX();
    	float timemove = 0;
	    	while(true)
	    	{
	    	
	    	if(Math.abs(this.getX() - lastpos) >= 64)
	    	{
	    		lastpos=this.getX();
	    		if(health >=40)
	    		{
	    			health = 40;
	    		}
	    		else
	    		{
	    			health+=1;
	    		}
	    	}
	    	else if(timemove >= 1200)
	    	{
	    		starttime = System.currentTimeMillis();
	    		if(health >=35)
	    		{
	    			health = 40;
	    		}
	    		else
	    		{
	    			health+=5;
	    		}
	    	}
	    	if(this.getVelocityX() == 0 && this.getVelocityY() == 0)
	    	{
	    		timemove=System.currentTimeMillis() - starttime;
	    	}
	    	try{
	    		Thread.sleep(sleeptime);
	    	}catch(Exception e){}
    	}
    }

    public void collideHorizontal() {
        setVelocityX(0);
    }


    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }


    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
        	if(!ResourceManager.fanList.contains(TileMapRenderer.pixelsToTiles(this.getX())))
        	{
	            onGround = false;
	            setVelocityY(JUMP_SPEED);
        	}
        }
    }
    
    public void shooting(){
    	ammo.start(this);
//    	ammo.shooting(this);
//    	System.out.println("finished shooting?");
    }
    
    public void stopShooting(){
    	ammo.stop();
//    	System.out.println("finished shooting?");
    }
    


    public float getMaxSpeed() {
        return 0.5f;
    }
    
    
    private class RegenTimer extends java.util.TimerTask
    {
	    public void run()
	    {
	    	regeneration();
	    }
    }
    
    private class invincibleTimer extends java.util.TimerTask
    {
    	public void run()
    	{
    		makeInvincible();
    	}
    }

}
