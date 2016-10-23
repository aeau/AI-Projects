package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.GHOST;

/**
 * If any ghost is in the edible state, means that we are still in thee effects of the power pill
 * @author A. Alvarez
 *
 */
public class HasPowerPill extends Action {

	@Override
	public boolean Execute(BlackBoard blackboard) {
	
		// TODO Auto-generated method stub
		for(GHOST ghost : GHOST.values())
		{
			if(blackboard.current_game.getGhostEdibleTime(ghost)!=0)
			{
				return true;
			}
			
		}

		return false;
	}

}
