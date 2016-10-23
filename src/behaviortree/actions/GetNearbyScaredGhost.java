package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.GHOST;

/**
 * If the agent have an edible ghost close enough, it set the ghost as a target
 * @author A. Alvarez
 *
 */
public class GetNearbyScaredGhost extends Action{

	@Override
	public boolean Execute(BlackBoard blackboard) 
	{
		
		int closest_ghost = 40;
		GHOST selected_ghost = null;
		
		for(GHOST ghost : GHOST.values())
		{
			if(blackboard.current_game.getGhostEdibleTime(ghost)!=0 && blackboard.current_game.getGhostLairTime(ghost)==0)
			{
				int short_path = blackboard.current_game.getShortestPathDistance(blackboard.pacman_position,
						blackboard.current_game.getGhostCurrentNodeIndex(ghost));
				if(short_path < closest_ghost)
				{
					selected_ghost = ghost;
					closest_ghost = short_path;
				}
			}	
		}
		
		if(selected_ghost != null)
		{
			blackboard.selected_ghost = selected_ghost;
			blackboard.target = blackboard.current_game.getGhostCurrentNodeIndex(selected_ghost);

			return true;
		}
		else
		{
			blackboard.selected_ghost = null;
			blackboard.target = -1;
			
			return false;
		}
	}

}
