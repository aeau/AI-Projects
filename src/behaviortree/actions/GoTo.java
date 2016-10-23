package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.DM;

/**
 * If there is a target set when this action is prompted, it will get next move towards the said target
 * @author A. Alvarez
 *
 */
public class GoTo extends Action{

	@Override
	public boolean Execute(BlackBoard blackboard) {
		
		if(blackboard.target == -1)
		{
			return false;
		}
		
		blackboard.next_move = blackboard.current_game.getNextMoveTowardsTarget(
								blackboard.pacman_position,
								blackboard.target,DM.PATH);
		
		return true;
	}

}