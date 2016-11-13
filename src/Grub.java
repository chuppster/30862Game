import java.util.Timer;
import java.math.*;

/**
    A Grub is a Creature that moves slowly on the ground.
*/
public class Grub extends Creature {

	private float reactionTime = 0.5f;
	private static TileMap map;
	private long playerDist = 350;
	private boolean shootingPlayer = false;
	private float FIRE_RATE = 0.5f;
	private Amunition ammo = new Amunition(FIRE_RATE);
	
    public Grub(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    	Timer tim = new Timer();
		tim.schedule(new PlayerWatcher(), 0);
		ammo.playerOwned = false;
    }
    
    public static void SetMap(TileMap m)
    {
    	map = m;
    }
    
    public void stopShootingDeath()
    {
    	ammo.stop();
    }
    
    private void callShooting()
    {
    	if (getState() != Creature.STATE_DYING)
    	{
    		ammo.start(this);
    	}
    }
    
    private class PlayerWatcher extends java.util.TimerTask
    {
	    public void run()
	    {
	    	boolean shootNext = false;
	    	while (true)
	    	{
//	    		System.out.println("running playerWatch");
	    		if (getState() == Creature.STATE_DYING)
	    		{
	    			ammo.stop();
	    			break;
	    		}
		    	try
		    	{	
		    		Thread.sleep((long) (reactionTime * 1000));
		    	}
		    	catch (Exception e)
		    	{
		    		System.out.println("ERROR");
		    	}
		    	Player p = (Player)map.getPlayer();
		    	double distx = p.getX() - getX();
//		    	double disty = p.getY() - getY();
//		    	System.out.println("p.getX() = " + p.getX() + "   getX() = " + getX() + "     distx = " + distx);
		    	
		    	if (shootNext == true)
		    	{
		    		shootingPlayer = true;
		    		callShooting();
		    		//begin shooting player
		    		break;
		    	}
		    	
		    	if (Math.abs(distx) < playerDist)
		    	{
		    		System.out.println("WITHIN DIST");
		    		if (shootingPlayer == false)
		    		{
		    			shootingPlayer = true;
		    			//begin shooting player
		    			callShooting();
		    			break;
		    		}
		    	}
		    	else if (Math.abs(distx) < playerDist + 50)
		    	{
		    		System.out.println("not close enough to fire immediately");
		    		if (shootingPlayer == false)
		    		{
		    			shootNext = true;
		    		}
		    	}
	    	}    
	    }
    }


    public float getMaxSpeed() {
        return 0.05f;
    }

}
