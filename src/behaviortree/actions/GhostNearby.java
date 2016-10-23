package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.GHOST;

/**
 * if there is a ghost close enough to the agent in a non-edible state we set it as a target
 * @author A. Alvarez
 *
 */
public class GhostNearby extends Action{
	
	@Override
	public boolean Execute(BlackBoard blackboard) 
	{
		
		for(GHOST ghost : GHOST.values())
		{
			if(blackboard.current_game.getGhostEdibleTime(ghost)==0 && blackboard.current_game.getGhostLairTime(ghost)==0)
			{
				if(blackboard.current_game.getShortestPathDistance(blackboard.pacman_position,
																	blackboard.current_game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
				{
					blackboard.selected_ghost = ghost;
					blackboard.target = blackboard.current_game.getGhostCurrentNodeIndex(ghost);

					return true;
				}
			}	
		}
		
		blackboard.selected_ghost = null;
		blackboard.target = -1;
		
		return false;
			
	}

}
