package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;

/**
 * Check for power pills in the the proximity of the agent, if the are ghost in the lair it returns failure
 * @author A. Alvarez
 *
 */
public class PowerPillsAround extends Action{

	@Override
	public boolean Execute(BlackBoard blackboard) {
		
		//int[] activePills = blackboard.current_game.getActivePillsIndices();
		int[] activePowerPills = blackboard.current_game.getActivePowerPillsIndices();
		
		if(activePowerPills.length == 0)
			return false;
		
		for(GHOST ghost : GHOST.values())
		{
			if(blackboard.current_game.getGhostLairTime(ghost)!=0)
			{
				blackboard.target = -1;
				return false;
			}
			
		}
		
		int[] targetNodeIndices = new int[activePowerPills.length];

		
		for(int i=0;i<activePowerPills.length;i++)
			targetNodeIndices[i]=activePowerPills[i];		
		
		if(blackboard.current_game.getShortestPathDistance(blackboard.pacman_position, 
															blackboard.current_game.getClosestNodeIndexFromNodeIndex
															(blackboard.pacman_position,targetNodeIndices,DM.PATH)) < POWERPILL_MIN_DIST)
		{
			blackboard.target = blackboard.current_game.getClosestNodeIndexFromNodeIndex(blackboard.pacman_position,targetNodeIndices,DM.PATH);
			return true;
		}

		
		blackboard.target = -1;
		return false;
	}

}