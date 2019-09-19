package HalloranBaker.Astar;

/* This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details. */ 

import java.util.ArrayList;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

import HalloranBaker.Astar.Level;
import HalloranBaker.Astar.LevelScene;

// Most of the A* planner code sits in this file.
// Overview:
// SearchNode class: A node is represented by this class, containing an action, a world state, and some more info.
// search(): This function is the core search algorithm, searching for an optimal path
// optimize(): Function controlling the search and extracting plans to return to the API.


public class aStarSimulator 
{
	// LevelScene objects store all the information about the environment,
	// Mario and enemies. 
    public LevelScene levelScene;  		// current world state
    public LevelScene workScene;   		// world state used by the planner (some ticks in the future)
    public SearchNode bestPosition; 	// the current best position found by the planner
    public SearchNode furthestPosition; // the furthest position found by the planner (sometimes different than best)
    float currentSearchStartingMarioXPos;
    ArrayList<SearchNode> posPool;		// the open-list of A*, contains all the unexplored search nodes
    ArrayList<int[]> visitedStates = new ArrayList<int[]>(); // the closed-list of A*
    
    public int timeBudget = 20; // ms
    public static final int visitedListPenalty = 1500; // penalty for being in the visited-states list
    
    private ArrayList<boolean[]> currentActionPlan; // the plan generated by the panner
    int ticksBeforeReplanning = 0; 
    
	// A SearchNode is a node in the A* search, consisting of an action, the world state using this action
    // and information about the parent.
	private class SearchNode
	{
		private int timeElapsed = 0;			// How much ticks elapsed since start of search
		public float remainingTimeEstimated = 0; // Optimal (estimated) time to reach goal
		private float remainingTime = 0;		// Optimal time to reach goal AFTER simulating with the selected action

		public SearchNode parentPos = null;		// Parent node
		public LevelScene sceneSnapshot = null; // World state of this node
		public boolean hasBeenHurt = false;
		public boolean isInVisitedList = false;
		
		boolean[] action;						// the action of this node
		int repetitions;
		public int depth;
		
		public SearchNode(boolean[] action, int repetitions, SearchNode parent, int depth)
		{
	    	this.parentPos = parent;
<<<<<<< HEAD
=======
	    	this.action = action;
	    	this.repetitions = repetitions;
	    	this.depth = depth;
>>>>>>> 14341817d015b351c941b0e054c3e5ce51f7216e
	    	if (parent != null)
	    	{
	    		this.remainingTimeEstimated = parent.estimateRemainingTimeChild(action, repetitions);
	    		timeElapsed = parent.timeElapsed + repetitions;
<<<<<<< HEAD
=======
	    		try {
					sceneSnapshot = (LevelSceneSnapshot) parent.sceneSnapshot.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		//sceneSnapshot.mario.keys = action;
				//sceneSnapshot.tick();
>>>>>>> 14341817d015b351c941b0e054c3e5ce51f7216e
	    	}
	    	else
	    	{
	    		this.remainingTimeEstimated = calcRemainingTime(levelScene.mario.x, 0);
	    		timeElapsed = 0;
	    	}
	    	this.action = action;
	    	this.repetitions = repetitions;
		}
		
		// returns the estimated remaining time to some arbitrary distant target
		public float calcRemainingTime(float marioX, float marioXA)
		{
		    float maxMarioSpeed = 10.9090909f;

			return (100000 - (maxForwardMovement(marioXA, 1000) + marioX)) 
				/ maxMarioSpeed - 1000;
		}
		
		public float getRemainingTime()
		{
			if (remainingTime > 0) 
				return remainingTime;
			else
				return remainingTimeEstimated;
		}
		
		// estimate the time remaining to the goal for a child (that uses action)
		public float estimateRemainingTimeChild(boolean[] action, int repetitions)
		{
			float[] childbehaviorDistanceAndSpeed = estimateMaximumForwardMovement(
					levelScene.mario.xa, action, repetitions);
			return calcRemainingTime(levelScene.mario.x + childbehaviorDistanceAndSpeed[0],
					childbehaviorDistanceAndSpeed[1]);			
		}
		
<<<<<<< HEAD
=======
		public void advanceScene(boolean[] action)
		{
			sceneSnapshot.mario.keys = action;
			sceneSnapshot.tick();
		}
		
		private int getMarioDamage(){
	    	return sceneSnapshot.mario.collisionsWithCreatures;
	    }
		
>>>>>>> 14341817d015b351c941b0e054c3e5ce51f7216e
		// Simulate the world state after we applied the action of this node, using the parent world state
		public float simulatePos()
		{
	    	// set state to parents scene
			levelScene = parentPos.sceneSnapshot;
			parentPos.sceneSnapshot = backupState();
			
			int initialDamage = getMarioDamage();
	    	for (int i = 0; i < repetitions; i++)
	    	{
	    		/* This is the graphical line output, it has been disabled for the competition
	    		if (debugPos < 1000)
	    		{
	    			GlobalOptions.Pos[debugPos][0] = (int) levelScene.mario.x;
	    			GlobalOptions.Pos[debugPos][1] = (int) levelScene.mario.y;
	    			debugPos++;
	    		}*/
	    		
	    		// Run the simulator
	    		advanceStep(action);
	    		
	    		/*if (debugPos < 1000)
	    		{
	    			GlobalOptions.Pos[debugPos][0] = (int) levelScene.mario.x;
	    			GlobalOptions.Pos[debugPos][1] = (int) levelScene.mario.y;
	    			debugPos++;
	    		}
	    		if (debugPos > 1000)
	    			debugPos = 0;
	    		*/
	    	}
	    	
	    	// set the remaining time after we've simulated the effects of our action,
	    	// penalising it if we've been hurt.
	    	remainingTime = calcRemainingTime(sceneSnapshot.mario.x, sceneSnapshot.mario.xa)
	    	 	+ (getMarioDamage() - initialDamage) * (1000000 - 100 * timeElapsed);
	    	System.out.println(timeElapsed);
	    	remainingTime = sceneSnapshot.mario.x*1000 - getMarioDamage()*100;
	    	if (isInVisitedList)
	    		remainingTime += visitedListPenalty;
	    	hasBeenHurt = (getMarioDamage() - initialDamage) != 0;
	    	sceneSnapshot = backupState();
	    			
	    	return remainingTime;			
		}
		
		public ArrayList<SearchNode> generateChildren()
		{
			ArrayList<SearchNode> list = new ArrayList<SearchNode>();
			ArrayList<boolean[]> possibleActions = createPossibleActions(this);
			
			for (boolean[] action: possibleActions)
			{
				SearchNode s = new SearchNode(action, repetitions, this, this.depth+1);
				s.advanceScene(action);
				s.advanceScene(action);
				/*for(boolean a:action){
					System.out.print(a);
				}*/
				list.add(s);
				//list.add(new SearchNode(action, repetitions, this));
			}	
			//System.out.println(list.get(0).sceneSnapshot.mario.x==list.get(1).sceneSnapshot.mario.x);
			return list;
		}
		
		public int heuristic(){
			//return (int) sceneSnapshot.mario.x*1000 - getMarioDamage()*100;
			//hack to prefer jumping
			int x;
			if (this.action[Mario.KEY_JUMP]){
				x = 50;
			}else{
				x = 0;
			}
			return (int) this.sceneSnapshot.mario.x*100 - (int) this.sceneSnapshot.mario.y*3 + x;
		}
		
	}
	
    
    public aStarSimulator()
    {
    	initialiseSimulator();
    }
    
    // Does the application of the jump action make any difference in the given world state?
    // if not, we don't need to consider it for child positions of nodes
    public boolean canJumpHigher(SearchNode currentPos, boolean checkParent)
    {
    	// This is a hack to allow jumping one tick longer than required 
    	// (because we're planning two steps ahead there might be some odd situations where we might need that)
    	if (currentPos.parentPos != null && checkParent
    			&& canJumpHigher(currentPos.parentPos, false))
    			return true;
    	return currentPos.sceneSnapshot.mario.mayJump() || (currentPos.sceneSnapshot.mario.jumpTime > 0);
    }
    
    // Create a list of (almost) all valid actions possible in our node
    private ArrayList<boolean[]> createPossibleActions(SearchNode currentPos)
    {
    	ArrayList<boolean[]> possibleActions = new ArrayList<boolean[]>();
    	
    	// jump
    	if (canJumpHigher(currentPos, true)) possibleActions.add(createAction(false, false, false, true, false));
    	if (canJumpHigher(currentPos, true)) possibleActions.add(createAction(false, false, false, true, true));
    	
    	// run right
    	possibleActions.add(createAction(false, true, false, false, true));
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(false, true, false, true, true));
    	possibleActions.add(createAction(false, true, false, false, false));
<<<<<<< HEAD
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(false, true, false, true, false));
 	
    	// run left
    	possibleActions.add(createAction(true, false, false, false, false));
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(true, false, false, true, false));
    	possibleActions.add(createAction(true, false, false, false, true));
    	if (canJumpHigher(currentPos, true))  possibleActions.add(createAction(true, false, false, true, true));
=======
    	possibleActions.add(createAction(false, true, false, true, false));
    	
    	//run left
    	possibleActions.add(createAction(true, false, false, false, false));
>>>>>>> 14341817d015b351c941b0e054c3e5ce51f7216e
    	
    	return possibleActions;
    }
    
    private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean speed)
    {
    	boolean[] action = new boolean[5];
    	action[Mario.KEY_DOWN] = down;
    	action[Mario.KEY_JUMP] = jump;
    	action[Mario.KEY_LEFT] = left;
    	action[Mario.KEY_RIGHT] = right;
    	action[Mario.KEY_SPEED] = speed;
    	return action;
    }
    
    // estimate the optimal forward movement for a fixed amount of ticks, given a speed and an action
    // This is a bit hacky
    public float[] estimateMaximumForwardMovement(float currentAccel, boolean[] action, int ticks)
    {
    	float dist = 0;
    	float runningSpeed =  action[Mario.KEY_SPEED] ? 1.2f : 0.6f;
    	int dir = 0;
    	if (action[Mario.KEY_LEFT]) dir = -1;
    	if (action[Mario.KEY_RIGHT]) dir = 1;
    	for (int i = 0; i < ticks; i++)
    	{
    		currentAccel += runningSpeed * dir;
    		dist += currentAccel;
    		// Slow down
    		currentAccel *= 0.89f;
    	}    	
    	float[] ret = new float[2];
    	ret[0] = dist;
    	ret[1] = currentAccel;
    	return ret;
    }
    
    // distance covered at maximum acceleration with initialSpeed for ticks timesteps 
    // this is the closed form of the above function, found using Matlab 
    private float maxForwardMovement(float initialSpeed, int ticks)
    {
    	float y = ticks;
    	float s0 = initialSpeed;
    	return (float) (99.17355373 * Math.pow(0.89,y+1)
    	  -9.090909091*s0*Math.pow(0.89,y+1)
    	  +10.90909091*y-88.26446282+9.090909091*s0);
    }
    
    private int getMarioDamage()
    {
    	// early damage at gaps: Don't even fall 1 px into them.
    	if (levelScene.level.isGap[(int) (levelScene.mario.x/16)] &&
    			levelScene.mario.y > levelScene.level.gapHeight[(int) (levelScene.mario.x/16)]*16)
    	{
     		levelScene.mario.damage+=5;
    	}
    	return levelScene.mario.damage;
    }
    
    private int getMaxNode(){
    	int max = 0;
    	int maxVal = 0;
    	for(int i=0; i<posPool.size();i++){
    		if (posPool.get(i).heuristic()>maxVal){
    			maxVal = posPool.get(i).heuristic();
    			max = i;
    		}
    	}
    	return max;
    }
    
    public boolean[] search2(int depth){
    	//SearchNode startPos = new SearchNode(null, 2, null,0);
    	SearchNode startPos = new SearchNode(createAction(false,false,false,false,false), 2, null,0);
    	startPos.sceneSnapshot = backupState();
    	
    	posPool = new ArrayList<SearchNode>();
    	//visitedStates.clear();
    	posPool.addAll(startPos.generateChildren());
    	//for(SearchNode i : posPool){
    	//	System.out.print(i.heuristic());
    	//}
    	//System.out.println();
    	SearchNode current;
    	bestPosition = null;
    	int bestVal = -1000;
    	while(posPool.size()>0){
    		current = posPool.remove(getMaxNode());
    		System.out.println(current.heuristic());
    		if(current.heuristic()>bestVal){
    			bestPosition = current;
    			bestVal = current.heuristic();
    		}
    		
    		if (current.depth<depth){
    			posPool.addAll(current.generateChildren());
    		}
       	}
    	return extractMove();
    }
    
    
    // main search function
    private void search(long startTime)
    {
    	SearchNode current = bestPosition;
    	boolean currentGood = false;		// is the current node good (= we're not getting hurt)
    	int ticks = 0;
    	int maxRight = 176;					// distance to plan to the right
    	
    	// Search until we've reached the right side of the screen, or if the time is up.
    	while(posPool.size() != 0 
    			&& ((bestPosition.sceneSnapshot.mario.x - currentSearchStartingMarioXPos < maxRight) || !currentGood) 
    			&& (System.currentTimeMillis() - startTime < 40)) 
    	{
    		ticks++;
    		
    		// Pick the best node from our open list
    		current = pickBestPos(posPool);
    		currentGood = false;
    		
    		// Simulate the consequences of the action associated with the chosen node
    		float realRemainingTime = current.simulatePos();
    		
    		// Now act on what we get as remaining time (to some distant goal)
    		
    		if (realRemainingTime < 0)
    		{
    			// kick out negative remaining time (shouldnt happen)
    			continue;
    		}
    		else if  (!current.isInVisitedList 
    				&& isInVisited((int) current.sceneSnapshot.mario.x, (int) current.sceneSnapshot.mario.y, current.timeElapsed))
	   		{
    			// if the position & time of the node is already in the closed list
    			// (i.e., has been explored before), put some penalty on it and put it 
    			// back into the pool. The closed list works approximately: nodes too close
    			// to an item in the closed list are considered visited, even though they're a bit different.
    			
    			realRemainingTime += visitedListPenalty;
    			current.isInVisitedList = true;
    			current.remainingTime = realRemainingTime;
    			current.remainingTimeEstimated = realRemainingTime;
	   			
    			posPool.add(current); 
	   		}
    		else if (realRemainingTime - current.remainingTimeEstimated > 0.1)
    		{
    			// current node is not as good as anticipated. put it back in pool and look for best again
    			current.remainingTimeEstimated = realRemainingTime;
    			posPool.add(current);
    		}
    		else
    		{
    			// accept the node, its estimated time is as good as its real time.
    			currentGood = true;
    			
    			// put it into the visited list
    			visited((int) current.sceneSnapshot.mario.x, (int) current.sceneSnapshot.mario.y, current.timeElapsed);
    			
    			// put all children into the open list
    			posPool.addAll(current.generateChildren());    			
    		}
    		if (currentGood) 
    		{
    			// the current node is the best node (property of A*)
    			bestPosition = current;
    			
    			// if we're not over a gap, accept it also as the furthest pos.
    			// the furthest position is a work-around to avoid falling into gaps
    			// when the search is stopped (by time-out) while we're over a gap
    			if (current.sceneSnapshot.mario.x > furthestPosition.sceneSnapshot.mario.x
    					&& !levelScene.level.isGap[(int)(current.sceneSnapshot.mario.x/16)])
    				furthestPosition = current;
    		}
    	}
    	if (levelScene.mario.x - currentSearchStartingMarioXPos < maxRight
    			&& furthestPosition.sceneSnapshot.mario.x > bestPosition.sceneSnapshot.mario.x + 20
    			&& (levelScene.mario.fire ||
    					levelScene.level.isGap[(int)(bestPosition.sceneSnapshot.mario.x/16)]))
    	{
    		// Couldnt plan till end of screen, take furthest (in some situations)
    		bestPosition = furthestPosition;
    	}
    	
    	if (levelScene.verbose > 1) System.out.println("Search stopped. Remaining pool size: "+ posPool.size() + " Current remaining time: " + current.remainingTime);

    	levelScene = current.sceneSnapshot;
    }
    
    // initialise the planner
    private void startSearch(int repetitions)
    {    	
<<<<<<< HEAD
    	if (levelScene.verbose > 1) System.out.println("Started search.");
    	SearchNode startPos = new SearchNode(null, repetitions, null);
=======
    	SearchNode startPos = new SearchNode(null, repetitions, null,0);
>>>>>>> 14341817d015b351c941b0e054c3e5ce51f7216e
    	startPos.sceneSnapshot = backupState();
    	
    	posPool = new ArrayList<SearchNode>();
    	visitedStates.clear();
    	posPool.addAll(startPos.generateChildren());
    	currentSearchStartingMarioXPos = levelScene.mario.x; 
   	
    	/*
		for(int i = 0; i < 1000; i++)
		{
			GlobalOptions.Pos[i][0] = 0;
			GlobalOptions.Pos[i][1] = 0;
		}
    	debugPos = 0;
    	*/
    	bestPosition = startPos;
    	furthestPosition = startPos;
    	
    }
    
    private boolean[] extractMove(){
    	if (bestPosition == null)
    	{
    		return createAction(false, true, false, false, true);
    	}
    	SearchNode current = bestPosition;
    	ArrayList<boolean[]>moves = new ArrayList<boolean[]>();
    	while (current.parentPos != null)
    	{
    		moves.add(0,current.action);
    		current = current.parentPos;
    	}
		return moves.get(0);
    }
    
    // Extract the plan by taking the best node and going back to the root, 
    // recording the actions at each step.
    private ArrayList<boolean[]> extractPlan()
    {
    	ArrayList<boolean[]> actions = new ArrayList<boolean[]>();
    	
    	// just move forward if no best position exists
    	if (bestPosition == null)
    	{
    		if (levelScene.verbose > 1) System.out.println("NO BESTPOS!");
    		for (int i = 0; i < 10; i++)
    		{
    			actions.add(createAction(false, true, false, false, true));        		
    		}
    		return actions;
    	}
    	if (levelScene.verbose > 2) System.out.print("Extracting plan (reverse order): ");
    	SearchNode current = bestPosition;
    	while (current.parentPos != null)
    	{
    		for (int i = 0; i < current.repetitions; i++)
    			actions.add(0, current.action);
    		if (levelScene.verbose > 2) 
    			System.out.print("[" 
    				+ (current.action[Mario.KEY_DOWN] ? "d" : "") 
    				+ (current.action[Mario.KEY_RIGHT] ? "r" : "")
    				+ (current.action[Mario.KEY_LEFT] ? "l" : "")
    				+ (current.action[Mario.KEY_JUMP] ? "j" : "")
    				+ (current.action[Mario.KEY_SPEED] ? "s" : "") 
    				+ (current.hasBeenHurt ? "-" : "") + "]");
    		current = current.parentPos;
    	}
    	if (levelScene.verbose > 2) System.out.println();
		return actions;
    }
    
    public String printAction(boolean[] action)
    {
    	String s = "";
    	if (action[Mario.KEY_RIGHT]) s+= "Forward ";
    	if (action[Mario.KEY_LEFT]) s+= "Backward ";
    	if (action[Mario.KEY_SPEED]) s+= "Speed ";
    	if (action[Mario.KEY_JUMP]) s+= "Jump ";
    	if (action[Mario.KEY_DOWN]) s+= "Duck";
    	return s;
    }
    
    // pick the best node out of the open list, using the typical A* decision
    // method, which is fitness = elapsed time + estimated time to goal
    private SearchNode pickBestPos(ArrayList<SearchNode> posPool)
    {
    	SearchNode bestPos = null;
    	float bestPosCost = 10000000;
    	for (SearchNode current: posPool)
    	{	    		
    		// slightly bias towards furthest positions
<<<<<<< HEAD
    		float currentCost = current.getRemainingTime()
    			+ current.timeElapsed * 0.90f;  
=======

    		float currentCost = (176 - current.sceneSnapshot.mario.x)*1000
        			+ current.timeElapsed * 0.90f;  

>>>>>>> 14341817d015b351c941b0e054c3e5ce51f7216e
    		if (currentCost < bestPosCost)
    		{
    			bestPos = current;
    			bestPosCost = currentCost;
    		}
    	}
    	posPool.remove(bestPos);
    	return bestPos;
    }
        
	public void initialiseSimulator()
	{
		levelScene = new LevelScene();
		levelScene.init();	
		// increase max level length here if you want to run longer levels
		levelScene.level = new Level(1500,15);
	}
	
	public void setLevelPart(byte[][] levelPart, float[] enemies)
	{
    	levelScene.setLevelScene(levelPart);
    	levelScene.setEnemies(enemies);
	}
	
	// make a clone of the current world state (copying marios state, all enemies, and some level information)
	public LevelScene backupState()
	{
		LevelScene sceneCopy = null;
		try
		{
			sceneCopy = (LevelScene) levelScene.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		
		return sceneCopy;
	}
	
	
	
	public void restoreState(LevelScene l)
	{
		levelScene = l;
	}
	
	public void advanceStep(boolean[] action)
	{
		levelScene.mario.setKeys(action);
		if (levelScene.verbose > 8) System.out.print("[" 
				+ (action[Mario.KEY_DOWN] ? "d" : "") 
				+ (action[Mario.KEY_RIGHT] ? "r" : "")
				+ (action[Mario.KEY_LEFT] ? "l" : "")
				+ (action[Mario.KEY_JUMP] ? "j" : "")
				+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
		levelScene.tick();
	}

	// main optimisation function, this calls the A* planner and extracts and returns the optimal action.
	public boolean[] optimise()
	{
		long startTime = System.currentTimeMillis();
        LevelScene currentState = backupState();
        if (workScene == null)
        	workScene = levelScene;
        
        // How many ticks to plan ahead into the future (can be tweaked)
        int planAhead = 2;
        
        // How many actions Mario makes for each search step (can be tweaked)
        int stepsPerSearch = 2;
        
        ticksBeforeReplanning--;
        if (ticksBeforeReplanning <= 0 || currentActionPlan.size() == 0)
        {
        	// We're done planning, extract the plan and prepare the planner for the
        	// next planning iteration (which starts planAhead ticks in the future)
        	currentActionPlan = extractPlan(); 
        	if (currentActionPlan.size() < planAhead)
        	{
        		if (levelScene.verbose > 2) System.out.println("Warning!! currentActionPlan smaller than planAhead! plansize: "+currentActionPlan.size());
        		planAhead = currentActionPlan.size();
        	}
        	
        	// simulate ahead to predicted future state, and then plan for this future state 
        	if (levelScene.verbose > 3) System.out.println("Advancing current state ... ");
        	for (int i = 0; i < planAhead; i++)
        	{
        		advanceStep(currentActionPlan.get(i));        		
        	}
        	workScene = backupState();
        	startSearch(stepsPerSearch);
        	ticksBeforeReplanning = planAhead;
        }
        // load (future) world state used by the planner
        restoreState(workScene);
		search(startTime);
    	workScene = backupState();
        
    	// select the next action from our plan
		boolean[] action = new boolean[5];
        if (currentActionPlan.size() > 0)
        	action = currentActionPlan.remove(0);
        
		long e = System.currentTimeMillis();
		if (levelScene.verbose > 0) System.out.println("Simulation took "+(e-startTime)+"ms.");
		restoreState(currentState);       
        return action;
	}
	
	private void visited(int x, int y, int t)
	{
		visitedStates.add(new int[]{x,y,t});
	}
	
	private boolean isInVisited(int x, int y, int t)
	{
		// is the (x, y, time) triple too close to a triple in the visited states list?
		
		// these values can be tweaked
		int timeDiff = 5;
		int xDiff = 2;
		int yDiff = 2;
		for(int[] v: visitedStates)
		{
			if (Math.abs(v[0] - x) < xDiff
					&& Math.abs(v[1] - y) < yDiff
					&& Math.abs(v[2] - t) < timeDiff
					&& t >= v[2])
			{
				return true;
			}
		}
		return false;	
	}
}