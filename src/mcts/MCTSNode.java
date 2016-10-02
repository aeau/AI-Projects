package mcts;

import java.util.ArrayList;
import java.util.List;

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
	public int move = 0;
	public MOVE pacman_move;
	
	MCTSNode(Game state, int range, int action){
		this.state = state;
		maxChild = range;
		move = action;
		pacman_move = MOVE.values()[move];
	}

	public boolean IsFullyExpanded()
	{
		return children.size() == maxChild;
	}
}