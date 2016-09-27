package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Constants.DM;

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