import java.util.Timer;

public class FiredShot extends Creature {
	public boolean playerOwned = false;
	private float deathTimer = 0.55f;
	
	public FiredShot(Animation shot, boolean owned)
	{
		super(shot, (Animation)shot.clone(), (Animation)shot.clone(), (Animation)shot.clone());
		playerOwned = owned;
		Timer tim = new Timer();
		tim.schedule(new DeathTimer(), 0);
	}
	
	private class DeathTimer extends java.util.TimerTask
    {
	    public void run()
	    {
	    	try
	    	{
	    		Thread.sleep((long) (deathTimer * 1000));
		    	setState(STATE_DEAD);
	    	}
	    	catch (Exception e)
	    	{
	    		System.out.println("ERROR");
	    	}
	    }
    }
}
