package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.GHOST;

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
