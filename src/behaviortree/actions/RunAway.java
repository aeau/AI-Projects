package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.DM;

/**
 * if there is a target to flee from, gets the next movement away from the said target 
 * @author A. Alvarez
 *
 */
public class RunAway extends Action{

	@Override
	public boolean Execute(BlackBoard blackboard) {
		
		if(blackboard.target == -1)
		{
			return false;
		}
		
		blackboard.next_move =  blackboard.current_game.getNextMoveAwayFromTarget(
								blackboard.current_game.getPacmanCurrentNodeIndex(),
								blackboard.target, DM.PATH);

		return true;
	}

}