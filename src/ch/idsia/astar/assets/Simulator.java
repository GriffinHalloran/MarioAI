package ch.idsia.astar.assets;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.ForwardJumpingAgent;
import ch.idsia.astar.assets.*;
import ch.idsia.tools.MarioAIOptions;

public class Simulator {
	
	public LevelSceneSnapshot levelScene;
	public LevelSceneSnapshot searchScene;
	
	public Simulator(){
		levelScene = new LevelSceneSnapshot();
	}
	
	public static void main(String[] args){
		Simulator s = new Simulator();
		try {
			s.searchScene = (LevelSceneSnapshot) s.levelScene.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Agent agent = new ForwardJumpingAgent();
	    boolean[] ac = new boolean[5];
    	ac[Mario.KEY_RIGHT] = true;
    	ac[Mario.KEY_SPEED] = true;
        s.levelScene.mario.keys = ac;

		s.levelScene.tick();
		s.levelScene.tick();
		System.out.println(s.levelScene.mario.x);
		System.out.println(s.searchScene.mario.x);
	}
}
