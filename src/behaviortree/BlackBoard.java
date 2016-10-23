package behaviortree;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * global state and variables used through the tree to communicate between nodes
 * @author A. Alvarez
 *
 */
public class BlackBoard 
{
	public Game current_game;
	public GHOST selected_ghost;
	public int selected_power_pill;
	public int selected_pill;
	public int pacman_position;
	public int target;
	public int score;
	
	public MOVE next_move = MOVE.UP;
}