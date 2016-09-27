package behaviortree.actions;

import java.util.ArrayList;

import behaviortree.BlackBoard;
import pacman.game.Constants.DM;

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