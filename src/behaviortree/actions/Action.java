package behaviortree.actions;

import behaviortree.BlackBoard;
import pacman.game.Game;

public abstract class Action 
{
	protected static int MIN_DISTANCE=20;	//if a ghost is this close, run away
	public abstract boolean Execute(BlackBoard blackboard);
}
