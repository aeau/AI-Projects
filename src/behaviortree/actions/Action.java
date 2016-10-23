package behaviortree.actions;

import behaviortree.BlackBoard;

/**
 * Encapsulates the action to be performedd in a leaf node
 * @author A. Alvarez
 *
 */
public abstract class Action 
{
	protected static int MIN_DISTANCE		=20;	//if a ghost is this close, run away
	protected static int POWERPILL_MIN_DIST = 60;
	public abstract boolean Execute(BlackBoard blackboard);
}
