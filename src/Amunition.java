import java.util.Timer;

import javax.swing.ImageIcon;


public class Amunition {

	public boolean canShoot = true;
	public boolean gassed = false;
	private boolean dir = false;
	private boolean lastDir = true;
	private float FIRE_RATE = 0.01f;
	private static float FIRE_VEL = 1.0f;
	private Sprite ammo;
	private static TileMap tilemap;
	private String filename = "images/amunition.png";
	public boolean playerOwned = false;
	private boolean triggerPulled = false;
	private boolean tempTriggerPulled = false;
	
	private int shotCount = 0;
	private Sprite sp;
	
	public Amunition(float fire_rate)
	{
		this.FIRE_RATE = fire_rate;	
		Timer tim = new Timer();
		tim.schedule(new TriggerWatcher(), 0);
		Timer t = new Timer();
		t.schedule(new ShortTimer(), 0);
	}
	
	public static void SetMap(TileMap tile)
	{
		tilemap = tile;
	}
	
	
	public void shooting(Sprite sprite)
	{
		lastDir = dir;
    	Timer timer = new Timer();
//    	System.out.println("SHOOTING");
    	if (canShoot)
    	{
    		canShoot = false;
    		timer.schedule(new FireRound(), 0);
    		spawnInstance(sprite);
    	}	
	 }
	
	public void start(Sprite s)
	{ 
		triggerPulled = true;
		tempTriggerPulled = true;
		sp = s;
	}
	
	public void stop()
	{ 
		tempTriggerPulled = false;
	}
	
	private void spawnInstance(Sprite sprite)
	{
		GameManager.playShootSound();
		Animation an = new Animation();
		an.addFrame(new ImageIcon(filename).getImage(), 10);
		an.addFrame(new ImageIcon(filename).getImage(), 10);
		
//		System.out.println("spawning instance");
		boolean owned = false;
		if (sprite instanceof Player)
		{
			owned = true;
		}
		ammo = new FiredShot((Animation)an.clone(), owned);

		
		dir = sprite.getDirection();
		
		if (dir == true)
		{
			ammo.setVelocityX(FIRE_VEL);
		}
		else
		{
			ammo.setVelocityX(-FIRE_VEL);
		}
		int temp = -100;
		if (ammo.getVelocityX() > 0)
		{
			temp = -temp;
		}
		ammo.setX(sprite.getX());
		ammo.setY(sprite.getY() + 50);
		
		if (ammo != null)
		{
			tilemap.addSprite(ammo);
		}
//		System.out.println("finished spawning instance");
		
	}
	
	
    private class FireRound extends java.util.TimerTask
    {
	    public void run()
	    {
//	    	System.out.println("I'M FIRING MY LASER! RAWRRRRRRRR");
	    	try
	    	{
	    		Thread.sleep((long) (FIRE_RATE * 400));
		    	canShoot = true;
	    	}
	    	catch (Exception e)
	    	{
	    		System.out.println("ERROR");
	    	}
//	    	System.out.println("canShoot set to true");
	    	
	    }
    }
    
    private class ShortTimer extends java.util.TimerTask
    {
	    public void run()
	    {
	    	try
	    	{
	    		while (true)
	    		{
		    		Thread.sleep((long) (500));
			    	if (tempTriggerPulled == false)
			    	{
//			    		System.out.println("trigger pulled set false");
			    		triggerPulled = false;
			    	}
	    		}
	    	}
	    	catch (Exception e)
	    	{
	    		System.out.println("ERROR");
	    	}
	    }
    }
    
    private class TriggerWatcher extends java.util.TimerTask
    {
	    public void run()
	    {
	    	try
	    	{
		    	while (!Thread.currentThread().isInterrupted())
		    	{
	//	    		System.out.println("in trigger watch");
		    		Thread.sleep((long) (FIRE_RATE * 100));
		    		
		    		if (shotCount >= 10)
		    		{
//		    			System.out.println("10 found");
		    			Thread.sleep((long) (1000));
		    			shotCount = 0;
		    		}
	//		    	System.out.println("looping! - triggerPulled= " + triggerPulled);
		    		if (gassed)
		    		{
		    			float distance = 0;
		    			float curr = tilemap.getPlayer().getX();
		    			float after = 0;
		    			for (int m = 0; m < 10; m++)
		    			{
		    				Thread.sleep((long) 100);
		    				after = tilemap.getPlayer().getX();
		    				distance += Math.abs(after - curr);
		    				if (distance > 500)
		    				{
		    					System.out.println("10 units from gas!");
		    					break;
		    				}
		    				curr = after;
		    			}
		    			gassed = false;
		    		}
			    	if (triggerPulled)
			    	{
			    		if (canShoot)
			    		{
				    		shooting(sp);
				    		shotCount++;
			    		}
			    	}
			    	else
			    	{
			    		shotCount = 0;
			    	}
			    	
		    	}
	    	}
	    	catch (InterruptedException consumed)
	    	{
	    		System.out.println("ERROR");
	    	}	
	    }
    }
	
	
}
