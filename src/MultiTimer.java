import java.util.Timer;

public class MultiTimer extends java.util.TimerTask
{
	float delay = 1f; //seconds
    public void run()
    {
    	while (true)
    	{
	    	try
	    	{
	    		Thread.sleep((long) (delay * 1000));
	    	}
	    	catch (Exception e)
	    	{
	    		System.out.println("ERROR");
	    	}
    	}
    }
}
