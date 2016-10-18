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
 *
 */
public class MCTSNode{
	
	public Game state;
	public List<MCTSNode> children = new ArrayList<MCTSNode>();
	public ArrayList<Integer> untried_actions = new ArrayList<Integer>();
	public MCTSNode parent = null;
	public int opposite_parent = -1;
	//public int my_action;
	public float reward = 0;
	public int times_visited = 0;
	public int maxChild;
	
	//Super important variables for tree and default phase.
	public int move = 0; //movement we do in ordinal
	public MOVE pacman_move; //movement we do in real Move
	public int path_cost = 0; //cost of moving through this path.
	public int destination = 0; //Supposed destination we should reach
	public MCTSReward my_reward; //Reward used in selection and backpro.
	public String name = "";
	public MOVE invalid_child_move;
	
	public int[] my_path = null;
	
	//FOR DEBUGGING PURPOSES
	public int[] safe_path = null;
	
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
	
	public void SetPath(int... path)
	{
		safe_path = path;
	}
	
	public void SetPathCost(int value)
	{
		path_cost = value;
	}

	public boolean IsFullyExpanded()
	{
		return untried_actions.isEmpty();
	}
	
	public MCTSNode GetRandomChild(Random rnd)
	{
		return children.get(rnd.nextInt(children.size()));
	}
	
	public void UpdateReward()
	{
		my_reward.CalculateAvgFromChildren(children, times_visited);
		my_reward.CalculateMaxFromChildren(children);
	}
	
	//PROBLEM WITH AVG AND MAX VALUES
	public float GetMaxValue(Tactics tactic)
	{
//		UpdateReward();
		switch(tactic)
		{
		case PILL:
			return my_reward.MAX_pill_reward * my_reward.MAX_survival_reward;
//			break;
		case GHOST:
			return my_reward.MAX_ghost_reward * my_reward.MAX_survival_reward;
//			break;
		case SURVIVE:
			return my_reward.MAX_survival_reward;
		case ENDGAME:
			return my_reward.MAX_pill_reward;
//			break;
		}
		
		return 0.0f;
	}
	
	public void IncreaseReward(MCTSReward reward)
	{
		my_reward.AddValues(reward, children, times_visited);
		
//		my_reward.pill_reward += reward.pill_reward;
//		my_reward.ghost_reward += reward.ghost_reward;
//		my_reward.survival_reward += reward.survival_reward;
	}
}
