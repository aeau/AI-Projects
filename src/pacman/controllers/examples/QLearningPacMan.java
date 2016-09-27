package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import reinforcementlearning.QPacmanState;
import reinforcementlearning.QTable;

public class QLearningPacMan extends Controller<MOVE>{

	QTable q_table = null;
	MOVE next_move;
	//QPacmanState ps;
	
	public QLearningPacMan(QTable q)
	{
		q_table = q;
	}
	
	public boolean GoalReached(int current_pos)
	{
		return current_pos == q_table.target;
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		// TODO Auto-generated method stub
		
		int pacman_pos = game.getPacmanCurrentNodeIndex();
		
		if(GoalReached(pacman_pos))
		{
			q_table.target = -1;
		}
		
		if(q_table.target == -1)
		{
			QPacmanState ps = new QPacmanState(game, game.getPacmanLastMoveMade());
			String info = game.getGameState();
			if(q_table.first_time)
			{
				q_table.first_time = false;
			}
			else
			{
				q_table.updateQvalue(q_table.reward, ps.toString());
			}
			
			int[] juncs = ps.ClosestKJunctions(game, q_table.actionRange, q_table.previous_target);
			q_table.previous_target = pacman_pos;
			q_table.target = juncs[q_table.getNextAction(ps.toString())];
		}
		
		
//		QPacmanState ps = new QPacmanState(game, game.getPacmanLastMoveMade());
//		//String ps = game.getGameState();
//		if(q_table.first_time)
//		{
//			q_table.first_time = false;
//		}
//		else
//		{
//			q_table.updateQvalue(q_table.reward, ps.toString());
//		}
			
		//Current state --- we compute value in table for the next move
		//next_move = MOVE.values()[q_table.getNextAction(ps.toString())];
		next_move = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), q_table.target, DM.PATH);
		return next_move;
	}

}
