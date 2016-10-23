package mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mcts.UCT.Tactics;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Class to store node information, e.g.
 * state, children, parent, accumulative reward, visited times
 * @author dariusv
 * @modified A. Hartzen
 * @modified A. Alvarez
 */
public class MCTSNode{
	
	public Game 				state;			//state at the moment it's created
	public List<MCTSNode> 		children 		= new ArrayList<MCTSNode>();
	public ArrayList<Integer> 	untried_actions = new ArrayList<Integer>();
	public MCTSNode 			parent 			= null;
	public int 					opposite_parent = -1;
	public float 				reward 			= 0;
	public int 					times_visited 	= 0;
	public int 					maxChild;
	
	//Super important variables for tree and default phase.
	public int 					move 			= 0; 	//movement we do in ordinal
	public MOVE 				pacman_move; 			//movement we do in real MOVE value
	public int 					path_cost 		= 0; 	//cost of moving through this path.
	public int 					destination 	= 0; 	//Supposed destination we should reach
	public MCTSReward 			my_reward; 				//Reward used in selection and backpro.
	
	
	public String 				name 			= "";
	public MOVE 				invalid_child_move;		//final real move done by the node to reach destination
	
	
	MCTSNode(Game state, int range, int action, int destination, String nam, MOVE invalid_child_move){
		this.state = state;
		this.maxChild = range;
		this.move = action;
		this.pacman_move = MOVE.values()[move];
		this.destination = destination;
		this.invalid_child_move = invalid_child_move;
		this.name += "from: " + nam + "; Action: " + pacman_move;
		
		this.my_reward = new MCTSReward();
	}
	
	public MCTSNode()
	{
	}
	
	/**
	 * Set a name to the node, usually for debugging
	 * @param name
	 */
	public MCTSNode(String name)
	{
		this.name = name;
	}
	
	/**
	 * Set the cost to move from parent to this node
	 * @param distance from parent origin to child destination
	 */
	public void SetPathCost(int value)
	{
		path_cost = value;
	}

	/**
	 * If it doesn't have any untried action it means it is fully expanded
	 * @return if the node have been fully expanded
	 */
	public boolean IsFullyExpanded()
	{
		return untried_actions.isEmpty();
	}
	
	/**
	 * Return a random children when children visits are lower than threshold MCTSConstants.CHILD_VISITED_THRESHOLD
	 * @param rnd object so its not neccesary to create a new one
	 * @return random child
	 */
	public MCTSNode GetRandomChild(Random rnd)
	{
		return children.get(rnd.nextInt(children.size()));
	}
	
	/**
	 * Update AVG and MAX rewards of the node.
	 */
	public void UpdateReward()
	{
		my_reward.CalculateAvgFromChildren(children, times_visited);
		my_reward.CalculateMaxFromChildren(children);
	}
	
	/**
	 * Get max reward value for selection
	 * @param current used tactic
	 * @return max reward dependent on tactic
	 */
	public float GetMaxValue(Tactics tactic)
	{
//		UpdateReward();
		switch(tactic)
		{
		case PILL:
			return my_reward.MAX_pill_reward;
//			break;
		case GHOST:
			return my_reward.MAX_ghost_reward;
//			break;
		case SURVIVE:
			return my_reward.MAX_survival_reward;
		case ENDGAME:
			return my_reward.MAX_pill_reward; //Take away this survival part
//			break;
		}
		
		return 0.0f;
	}
	
	/**
	 * Increase reward called from backpropagate
	 * @param reward achievedd in simulation
	 */
	public void IncreaseReward(MCTSReward reward)
	{
		my_reward.AddValues(reward, children, times_visited);
	}
	
	/**
	 * copy constructor
	 * @return a copy of this node
	 */
	public MCTSNode copy()
	{
		MCTSNode copy = new MCTSNode();
		copy.state = state.copy();         
		copy.children = children;	
		copy.untried_actions = untried_actions;
		copy.parent = parent;		
		copy.opposite_parent = opposite_parent;
		copy.reward = reward;
		copy.times_visited = times_visited;
		copy.maxChild = maxChild;      
		copy.move = move;			
		copy.pacman_move = pacman_move; 	
		copy.path_cost = path_cost;		
		copy.destination = destination;	
		copy.my_reward = my_reward; 		
		copy.name = name;		
		copy.invalid_child_move = invalid_child_move;
		return copy;
	}
}
