
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import java.util.Scanner;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    public static void main(String[] args) {
    	Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the name of the map you would like to load: ");
        String name = scanner.next();
        GameManager.fname = name;
    	System.out.println("starting the game.");
        new GameManager().run();
    }
    
    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;

    public static String fname;
    private Point pointCache = new Point();
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction moveDown;
    private GameAction shoot;
    private GameAction jump;
    private GameAction exit;


    public void init() {
        super.init();

        // set up input manager
        initInput();

        
		//ALL RELATIVE TO THEIR GAME
        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

 
        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
           resourceManager.loadImage("background.png"));

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("sounds/prize.wav");
        boopSound = soundManager.getSound("sounds/boop2.wav");
        
        
        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
        midiPlayer.getSequence("sounds/music.midi");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();

    }


    /**
        Closes any resources used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        moveDown = new GameAction("moveDown");
        shoot = new GameAction("shoot");
        jump = new GameAction("jump"/*,
            GameAction.DETECT_INITAL_PRESS_ONLY*/);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        

        
        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
        
        inputManager.mapToKey(shoot,  KeyEvent.VK_S);
        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(jump, KeyEvent.VK_UP);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(moveDown, KeyEvent.VK_DOWN);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }

        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
        	float velocityY = 0;
            float velocityX = 0;
            if (moveLeft.isPressed()) {
//            	System.out.println("GameManager.checkInput in moveLeft.isPressed()");
                velocityX-=player.getMaxSpeed();
            }
            if (moveRight.isPressed()) {
//            	System.out.println("GameManager.checkInput in moveRight.isPressed()");
                velocityX+=player.getMaxSpeed();
            }
            if (jump.isPressed()) {
//            	System.out.println("GameManager.checkInput in jump.isPressed()");
            	if((moveRight.isPressed() || moveLeft.isPressed()) && !player.isFlying())
            	{player.jump(false);}
            	if(player.isFlying())
            	{
            		velocityY+=player.getMaxSpeed();
            	}
            	
            }
            else if(moveDown.isPressed())
            {
            	velocityY-=player.getMaxSpeed();
            }
            if (shoot.isPressed()) {
//            	System.out.println("GameManager.checkInput in shoot.isPressed()");
            	player.shooting();
            }
            else if (!shoot.isPressed())
            {
            	player.stopShooting();
            }
            player.setVelocityX(velocityX);
            if(player.isFlying()){
            	player.setVelocityY(velocityY);
            }
        }

    }


    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }
        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }
        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());


//        System.out.println(s1x < s2x + s2.getWidth() &&
//            s2x < s1x + s1.getWidth() &&
//            s1y < s2y + s2.getHeight() &&
//            s2y < s1y + s1.getHeight());
        // check if the  two sprites' boundaries intersect
        if ((s1 instanceof FiredShot && s2 instanceof Player) || (s1 instanceof Player && s2 instanceof FiredShot))
        {
        	FiredShot s = null;
        	if (s1 instanceof FiredShot)
        	{
        		s = (FiredShot) s1;
        	}
        	if (s2 instanceof FiredShot)
        	{
        		s = (FiredShot) s2;
        	}
        	if (s.playerOwned == true)
        	{
        		return false;
        	}
        }
//        else if ((s1 instanceof FiredShot && s2 instanceof Creature) || (s1 instanceof Creature && s2 instanceof FiredShot))
//        {
//        	System.out.println("firedshot and creature");	
//        }
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();

            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }


    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();

        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);
 
        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);

        // update other sprites
        Iterator i = map.getSprites();
  
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                Sprite collisionSprite = getSpriteCollision(creature);
                if (collisionSprite instanceof FiredShot && creature instanceof FiredShot)
                {
                	System.out.println("IGNORE THE SHOTS COLLIDING");
                }
                if(creature instanceof FiredShot && collisionSprite instanceof PowerUp)
                {
                	System.out.println("Ignore shooting shrooms");
                }
                else if (collisionSprite instanceof FiredShot && !(creature instanceof Player))
                {
                	FiredShot shot = (FiredShot)collisionSprite;
                	if (shot.playerOwned == true)
                	{
                		soundManager.play(boopSound);
                		((Player)map.getPlayer()).subHealth(-10);
	                	creature.setState(Creature.STATE_DYING);
	                	((FiredShot)collisionSprite).setState(Creature.STATE_DEAD);
                	}
                }
                
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                    updateCreature(creature, elapsedTime);
                }
            }
            // normal update
            sprite.update(elapsedTime);
        }
  
    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {
    
        // apply gravity
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
//        	TOOK AWAY GRAVITY?
        }

      
        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;

        
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }

    }


    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        }
        else if (collisionSprite instanceof FiredShot)
        {
        	if(((Creature) collisionSprite).getState()==Creature.STATE_NORMAL && !player.getInvincible())
        	{
        		player.subHealth(5);
        		Creature bullet = (Creature)collisionSprite;
        		bullet.setState(Creature.STATE_DEAD);
        	}
        	if(player.getHealth() <= 0 && !player.getInvincible())
        	{
        		soundManager.play(boopSound);
        		player.setState(Creature.STATE_DYING);
        	}
        	
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                // kill the badguy and make player bounce
//            	System.out.println("hear a boop?");
//                soundManager.play(boopSound);
//                badguy.setState(Creature.STATE_DYING);
//                player.setY(badguy.getY() - player.getHeight());
//                player.jump(true);
            }
            else {
                // player dies!
            	if(!player.getInvincible())
            	{
            		player.setState(Creature.STATE_DYING);
            	}
            }
        }
    }


    /**
        Gives the player the speicifed power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);

        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
        	((Player)map.getPlayer()).setInvincible();
        }
        
        else if (powerUp instanceof PowerUp.Mushroom){
        	((Player)map.getPlayer()).subHealth(-5);
        }
        
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
    }
    

}
