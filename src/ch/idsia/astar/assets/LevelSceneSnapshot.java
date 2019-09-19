package ch.idsia.astar.assets;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.MarioAIOptions;
import ch.idsia.astar.assets.*;


//A stripped down class with only the necessary LevelScene info
public class LevelSceneSnapshot implements SpriteContext, Cloneable{
	
	private List<Sprite> sprites = new ArrayList<Sprite>();
	private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
	private List<Sprite> spritesToRemove = new ArrayList<Sprite>();
	
	public Level level;
	public Mario mario;
	public float xCam, yCam, xCamO, yCamO;
	
	public int tickCount;
	
	public static final int cellSize = 16;

	public int startTime = 0;
	private int timeLeft;
	private int width;
	private int height;
	
	public int killedCreaturesTotal;
	private MarioAIOptions opt;
	
	private Random randomGen = new Random(0);
	
	final private List<Float> enemiesFloatsList = new ArrayList<Float>();
	final private float[] marioFloatPos = new float[2];
	public int[] marioState = new int[11];
	
	public LevelSceneSnapshot(){
		try
	    {
//	            System.out.println("Java::LevelScene: loading tiles.dat...");
//	            System.out.println("LS: System.getProperty(\"user.dir()\") = " + System.getProperty("user.dir"));
	        Level.loadBehaviors(new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")));
	    } catch (IOException e)
	    {
	        System.err.println("[MarioAI ERROR] : error loading file resources/tiles.dat ; ensure this file exists in ch/idsia/benchmark/mario/engine ");
	        e.printStackTrace();
	        System.exit(0);
	    }
        
        Sprite.spriteContext = this;
        sprites.clear();
        
        level = new Level(1500, 15);

        mario = new Mario(this);
        sprites.add(mario);
        startTime = 1;

        tickCount = 1;
	}
	
	public static List<Sprite> cloneSprites(List<Sprite> sprites) throws CloneNotSupportedException{
		List<Sprite> newSprites = new ArrayList<Sprite>();
		for(Sprite s: sprites){
			newSprites.add((Sprite) s.clone()); 
		}
		return newSprites;
	}
	
	public Object clone() throws CloneNotSupportedException{
		LevelSceneSnapshot clone = (LevelSceneSnapshot) super.clone();
		clone.mario = (Mario) this.mario.clone();
		
		//clone.level = (Level) this.level.clone() TODO
		
		clone.sprites = cloneSprites(this.sprites);
		return clone;
	}
	
	public int fireballsOnScreen = 0;

	List<Shell> shellsToCheck = new ArrayList<Shell>();

	public void checkShellCollide(Shell shell)
	{
	    shellsToCheck.add(shell);
	}

	List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

	public void checkFireballCollide(Fireball fireball)
	{
	    fireballsToCheck.add(fireball);
	}
	
	public void tick()
	{
	    if (GlobalOptions.isGameplayStopped)
	        return;

	    timeLeft--;
	    if (timeLeft == 0)
	        mario.die("Time out!");
	    xCamO = xCam;
	    yCamO = yCam;

	    if (startTime > 0)
	    {
	        startTime++;
	    }

	    float targetXCam = mario.x - 160;

	    xCam = targetXCam;

	    if (xCam < 0) xCam = 0;
	    if (xCam > level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH)
	        xCam = level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH;

	    fireballsOnScreen = 0;

	    for (Sprite sprite : sprites)
	    {
	        if (sprite !=  mario)
	        {
	            float xd = sprite.x - xCam;
	            float yd = sprite.y - yCam;
	            if (xd < -64 || xd > GlobalOptions.VISUAL_COMPONENT_WIDTH + 64 || yd < -64 || yd > GlobalOptions.VISUAL_COMPONENT_HEIGHT + 64)
	            {
	                removeSprite(sprite);
	            } else
	            {
	                if (sprite instanceof Fireball)
	                    fireballsOnScreen++;
	            }
	        }
	    }

	    tickCount++;
	    level.tick();

//		            boolean hasShotCannon = false;
//		            int xCannon = 0;

	    for (int x = (int) xCam / cellSize - 1; x <= (int) (xCam + this.width) / cellSize + 1; x++)
	        for (int y = (int) yCam / cellSize - 1; y <= (int) (yCam + this.height) / cellSize + 1; y++)
	        {
	            int dir = 0;

	            if (x * cellSize + 8 > mario.x + cellSize) dir = -1;
	            if (x * cellSize + 8 < mario.x - cellSize) dir = 1;

	            SpriteTemplate st = level.getSpriteTemplate(x, y);

	            if (st != null)
	            {
//		                        if (st.getType() == Sprite.KIND_SPIKY)
//		                        {
//		                            System.out.println("here");
//		                        }

	                if (st.lastVisibleTick != tickCount - 1)
	                {
	                    if (st.sprite == null || !sprites.contains(st.sprite))
	                        st.spawn(this, x, y, dir);
	                }

	                st.lastVisibleTick = tickCount;
	            }

	            if (dir != 0)
	            {
	                byte b = level.getBlock(x, y);
	                if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
	                {
	                    if ((b % cellSize) / 4 == 3 && b / cellSize == 0)
	                    {
	                        if ((tickCount - x * 2) % 100 == 0)
	                        {
//		                                    xCannon = x;
	                            
	                            addSprite(new BulletBill(this, x * cellSize + 8 + dir * 8, y * cellSize + 15, dir));

//		                                    hasShotCannon = true;
	                        }
	                    }
	                }
	            }
	        }
	    for (Sprite sprite : sprites){
	        sprite.tick();
	        
	    }
	    mario.tick();
	    byte levelElement = level.getBlock(mario.mapX, mario.mapY);
	    if (levelElement == (byte) (13 + 3 * 16) || levelElement == (byte) (13 + 5 * 16))
	    {
	        if (levelElement == (byte) (13 + 5 * 16))
	            mario.setOnTopOfLadder(true);
	        else
	            mario.setInLadderZone(true);
	    } else if (mario.isInLadderZone())
	    {
	        mario.setInLadderZone(false);
	    }


	    for (Sprite sprite : sprites)
	        sprite.collideCheck();

	    for (Shell shell : shellsToCheck)
	    {
	        for (Sprite sprite : sprites)
	        {
	            if (sprite != shell && !shell.dead)
	            {
	                if (sprite.shellCollideCheck(shell))
	                {
	                    if (mario.carried == shell && !shell.dead)
	                    {
	                        mario.carried = null;
	                        mario.setRacoon(false);
	                        //System.out.println("sprite = " + sprite);
	                        shell.die();
	                        ++this.killedCreaturesTotal;
	                    }
	                }
	            }
	        }
	    }
	    shellsToCheck.clear();

	    for (Fireball fireball : fireballsToCheck)
	        for (Sprite sprite : sprites)
	            if (sprite != fireball && !fireball.dead)
	                if (sprite.fireballCollideCheck(fireball))
	                    fireball.die();
	    fireballsToCheck.clear();


	    sprites.addAll(0, spritesToAdd);
	    sprites.removeAll(spritesToRemove);
	    spritesToAdd.clear();
	    spritesToRemove.clear();
	}
	
	public void bump(int x, int y, boolean canBreakBricks)
	{
	    byte block = level.getBlock(x, y);

	    if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
	    {
	        if (block == 1)
	            Mario.gainHiddenBlock();
	        bumpInto(x, y - 1);
	        byte blockData = level.getBlockData(x, y);
	        if (blockData < 0)
	            level.setBlockData(x, y, (byte) (blockData + 1));

	        if (blockData == 0)
	        {
	            level.setBlock(x, y, (byte) 4);
	            level.setBlockData(x, y, (byte) 4);
	        }

	        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
	        {
	            if (randomGen.nextInt(5) == 0 && level.difficulty > 4)
	            {
	                //addSprite(new GreenMushroom(this, x * cellSize + 8, y * cellSize + 8));
	                //++level.counters.greenMushrooms;
	            } else
	            {
	                if (!Mario.large)
	                {
	                    addSprite(new Mushroom(this, x * cellSize + 8, y * cellSize + 8));
	                    //++level.counters.mushrooms;
	                } else
	                {
	                    addSprite(new FireFlower(this, x * cellSize + 8, y * cellSize + 8));
	                    //++level.counters.flowers;
	                }
	            }
	        } else
	        {
	            Mario.gainCoin();
	            //addSprite(new CoinAnim(x, y));
	        }
	    }

	    if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
	    {
	        bumpInto(x, y - 1);
	        if (canBreakBricks)
	        {
	            level.setBlock(x, y, (byte) 0);
	            for (int xx = 0; xx < 2; xx++)
	                for (int yy = 0; yy < 2; yy++){
	                    //addSprite(new Particle(x * cellSize + xx * 8 + 4, y * cellSize + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));

	                }
	        } else
	        {
	            level.setBlockData(x, y, (byte) 4);
	        }
	    }
	}

	public void bumpInto(int x, int y)
	{
	    byte block = level.getBlock(x, y);
	    if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
	    {
	        Mario.gainCoin();
	        level.setBlock(x, y, (byte) 0);
	        //addSprite(new CoinAnim(x, y + 1));
	    }

	    for (Sprite sprite : sprites)
	    {
	        sprite.bumpCheck(x, y);
	    }
	}
	
	public void performAction(boolean[] action){
	    this.mario.keys = action;
	}
	
	public void setLevel(byte[][] data){
		
		int mRow = (int) mario.x/cellSize;
		int mCol = (int) mario.y/cellSize;
		
	    for (int y = this.mario.mapY - mRow, row = 0; y <= this.mario.mapY + (19 - mRow - 1); y++, row++)
	    {
	        for (int x = this.mario.mapX - mCol, col = 0; x <= this.mario.mapX + (19 - mCol - 1); x++, col++)
	        {
	        	level.setBlock(x, y, data[row][col]);
	        }
	    }
	}
	
	
	
	public void setEnemies(float[] enemies){
		
		/*for (int i = 0; i<enemies.length; i+=3){
			int kind = (int) enemies[i];
			float x = enemies[i+1];
			float y = enemies[i+2];
			int type = 0;
			
			Sprite e = new Enemy(this, (int) x, (int) y, -1, kind, false, (int) x/16, (int) y/16);
			sprites.add(e);
		}*/
		boolean requireReplanning = false;
		List<Sprite> newSprites = new ArrayList<Sprite>();
		for (int i = 0; i < enemies.length; i += 3)
		{
			int kind = (int) enemies[i];
			float x = enemies[i+1];
			float y = enemies[i+2];

			if (kind == -1 || kind == 15)
				continue;
	        int type = -1;
	        boolean winged = false;
	        
	        if (type == -1)
	        	continue;
	        
	        
	        // is there already an enemy here?
	        float maxDelta = 2.01f * 1.75f;
	        boolean enemyFound = false;
	        for (Sprite sprite:sprites)
	        {
	        	
	        	// check if object is of same kind and close enough
	        	if (sprite.kind == kind 
	        			&& Math.abs(sprite.x - x) < maxDelta 
	        			&& ((Math.abs(sprite.y - y) < maxDelta)))
	        	{
	        		if (Math.abs(sprite.x - x) > 0)
	        		{
	        			if (sprite.kind == Sprite.KIND_SHELL)
	        				((Shell) sprite).facing *= -1;
	        			else
	        				((Enemy) sprite).facing *= -1;
	        			requireReplanning = true;
		        		sprite.x = x;
	        		}
	        		enemyFound = true;
	        	}
	        	
	        	if (enemyFound)
	        	{
	        		newSprites.add(sprite);
		        	sprite.lastX = x;
		        	sprite.lastY = y;
	        		break;
	        	}
	        }
	        // didn't find a close enemy in our representation of the world,
	        // create a new one.
	        Enemy sprite;
			if (!enemyFound){
	        	requireReplanning = true;

	        	
	        		// Add new enemy to the system.
        		sprite = new Enemy(this, (int) x, (int) y, -1, type, winged, (int) x/16, (int) y/16);
        		sprite.xa = 2;
	        	
	        
	        	sprite.lastX = x;
	        	sprite.lastY = y;
		        sprite.spriteTemplate =  new SpriteTemplate(type);
		        newSprites.add(sprite);
	        }
		}
		newSprites.add(mario);
		
		// add fireballs
		for (Sprite sprite:sprites)
        {
			if (sprite.kind == Sprite.KIND_FIREBALL)
				newSprites.add(sprite);
        }
		sprites = newSprites;
	}

	public void addSprite(Sprite sprite)
    {
        spritesToAdd.add(sprite);
        sprite.tick();
    }

    public void removeSprite(Sprite sprite)
    {
        spritesToRemove.add(sprite);
    }
}
