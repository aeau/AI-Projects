package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

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
