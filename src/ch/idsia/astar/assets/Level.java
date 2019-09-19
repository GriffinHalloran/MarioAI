/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.astar.assets;

import ch.idsia.tools.MarioAIOptions;

//TODO Make this cloneable

import java.io.*;

public class Level implements Cloneable
{	

	public static final String[] BIT_DESCRIPTIONS = {//
	        "BLOCK UPPER", //
	        "BLOCK ALL", //
	        "BLOCK LOWER", //
	        "SPECIAL", //
	        "BUMPABLE", //
	        "BREAKABLE", //
	        "PICKUPABLE", //
	        "ANIMATED",//
	};
	
	public static byte[] TILE_BEHAVIORS = new byte[256];

	public static final int BIT_BLOCK_UPPER = 1 << 0;
	public static final int BIT_BLOCK_ALL = 1 << 1;
	public static final int BIT_BLOCK_LOWER = 1 << 2;
	public static final int BIT_SPECIAL = 1 << 3;
	public static final int BIT_BUMPABLE = 1 << 4;
	public static final int BIT_BREAKABLE = 1 << 5;
	public static final int BIT_PICKUPABLE = 1 << 6;
	public static final int BIT_ANIMATED = 1 << 7;


	public int length;
	public int height;
	public int randomSeed;
	public int type;
	public int difficulty;

	public byte[][] map;
	public byte[][] data;
	// Experimental feature: Mario TRACE
	public int[][] marioTrace;

	public SpriteTemplate[][] spriteTemplates;

	public int xExit;
	public int yExit;
	
	public boolean[] isGap;

public Level(int length, int height)
{

    this.length = length;
    this.height = height;

    xExit = 50;
    yExit = 10;
    isGap = new boolean[length];
    for(int i = 0; i<length;i++){
    	isGap[i]=false;
    }
    try
    {
        map = new byte[length][height];
        data = new byte[length][height];
        spriteTemplates = new SpriteTemplate[length][height];

        marioTrace = new int[length][height + 1];
    } catch (OutOfMemoryError e)
    {
        System.err.println("Java: MarioAI MEMORY EXCEPTION: OutOfMemory exception. Exiting...");
        e.printStackTrace();
        System.exit(-3);
    }
	}

	public Object clone() throws CloneNotSupportedException{
		return super.clone(); 
	}
	
	public static void loadBehaviors(DataInputStream dis) throws IOException
	{
	    dis.readFully(Level.TILE_BEHAVIORS);
	}
	
	public static void saveBehaviors(DataOutputStream dos) throws IOException
	{
	    dos.write(Level.TILE_BEHAVIORS);
	}
	
	public static Level load(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
	    Level level = (Level) ois.readObject();
	    return level;
	}
	
	public static void save(Level lvl, ObjectOutputStream oos) throws IOException
	{
	    oos.writeObject(lvl);
	}
	
	/**
	 * Animates the unbreakable brick when smashed from below by Mario
	 */
	public void tick()
	{
	    // TODO:!!H! Optimize this!
	    for (int x = 0; x < length; x++)
	        for (int y = 0; y < height; y++)
	            if (data[x][y] > 0) data[x][y]--;
	}
	
	public byte getBlockCapped(int x, int y)
	{
	    if (x < 0) x = 0;
	    if (y < 0) y = 0;
	    if (x >= length) x = length - 1;
	    if (y >= height) y = height - 1;
	    return map[x][y];
	}
	
	public byte getBlock(int x, int y)
	{
	    if (x < 0) x = 0;
	    if (y < 0) return 0;
	    if (x >= length) x = length - 1;
	    if (y >= height) y = height - 1;
	    return map[x][y];
	}
	
	public void setBlock(int x, int y, byte b)
	{
	    if (x < 0) return;
	    if (y < 0) return;
	    if (x >= length) return;
	    if (y >= height) return;
	    map[x][y] = b;
	}
	
	public void setBlockData(int x, int y, byte b)
	{
	    if (x < 0) return;
	    if (y < 0) return;
	    if (x >= length) return;
	    if (y >= height) return;
	    data[x][y] = b;
	}
	
	public byte getBlockData(int x, int y)
	{
	    if (x < 0) return 0;
	    if (y < 0) return 0;
	    if (x >= length) return 0;
	    if (y >= height) return 0;
	    return data[x][y];
	}
	
	public boolean isBlocking(int x, int y, float xa, float ya)
	{
	    byte block = getBlock(x, y);
	    boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
	    blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
	    blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;
	
	    return blocking;
	}
	
	public SpriteTemplate getSpriteTemplate(int x, int y)
	{
	    if (x < 0) return null;
	    if (y < 0) return null;
	    if (x >= length) return null;
	    if (y >= height) return null;
	    return spriteTemplates[x][y];
	}
	
	public boolean setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate)
	{
	    if (x < 0) return false;
	    if (y < 0) return false;
	    if (x >= length) return false;
	    if (y >= height) return false;
	    spriteTemplates[x][y] = spriteTemplate;
	    return true;
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
	    aInputStream.defaultReadObject();
	}
	
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException
	{
	    aOutputStream.defaultWriteObject();
	}
}
