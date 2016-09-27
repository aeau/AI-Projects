package behaviortree.actions;

import java.util.ArrayList;

import behaviortree.BlackBoard;
import pacman.game.Constants.DM;

public class NearestPill extends Action{

	@Override
	public boolean Execute(BlackBoard blackboard) {
		
		//Strategy 3: go after the pills and power pills
		int[] pills=blackboard.current_game.getPillIndices();	
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(blackboard.current_game.isPillStillAvailable(i))
				targets.add(pills[i]);	
		
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		blackboard.target = blackboard.current_game.getClosestNodeIndexFromNodeIndex(blackboard.pacman_position,targetsArray,DM.PATH);
		
		return true;
	}

}
