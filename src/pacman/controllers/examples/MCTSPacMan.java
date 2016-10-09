package pacman.controllers.examples;

import java.awt.Color;

import mcts.MCTSNode;
import mcts.UCT;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MCTSPacMan extends Controller<MOVE>
{
	public UCT mcts = new UCT();
	public int previous_action = 0;
	boolean reverse = false;
	
	public MCTSPacMan()
	{
		
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) 
	{
//		System.out.println("WTF");
		mcts.SetGame(game);
		MOVE move = MOVE.NEUTRAL;
		boolean just_ate = false;
		
		if(mcts.helper.IsIntersection(game.getPacmanCurrentNodeIndex()) ||
				mcts.helper.IsJunction(game.getPacmanCurrentNodeIndex()) 	)
		{
			if(!game.wasPacManEaten())
			{
				try {
					move = mcts.runUCT(previous_action, timeDue);
					move = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), mcts.target, DM.PATH);
					reverse = false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				move = MOVE.NEUTRAL;
			}
		}
		else
		{
			mcts.current_pacman_path = game.getShortestPath(game.getPacmanCurrentNodeIndex(), mcts.target);
			
			//CHECK THIS SHIT
			if(!reverse)
			{
				if(!mcts.helper.IsPathSafe(game, mcts.current_pacman_path) || mcts.helper.WillGhostsArriveFirst(game, mcts.target))
				{
					reverse = true;
					int aux = mcts.target;
					mcts.target = mcts.previous_target;
					mcts.previous_target = game.getPacmanCurrentNodeIndex();
					mcts.current_pacman_path = game.getShortestPath(mcts.previous_target, mcts.target);
				}
//				else
//				{
//					
//					int go = -1;
//					int min_dist = Integer.MAX_VALUE;
//					for(GHOST ghost : GHOST.values())
//					{
//						if(game.wasGhostEaten(ghost))
//						{
//							just_ate = true;
//						}
//						
//						if(game.isGhostEdible(ghost) && game.getGhostLairTime(ghost)==0)
//						{
//							just_ate = true;
//							int dist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost));
//							if(dist < min_dist)
//							{
//								go = game.getGhostCurrentNodeIndex(ghost);
//								min_dist = dist;
//							}
//						}
//					}
//					
//					if(game.wasPowerPillEaten() || just_ate)
//					{
//						if(go != -1)
//						{
//							mcts.target = go;
//							mcts.previous_target = game.getPacmanCurrentNodeIndex();
//							mcts.current_pacman_path = game.getShortestPath(mcts.previous_target, mcts.target);
//	//						reverse = true;
//						}
//					}
//				}
			}
			//move = MOVE.values()[previous_action];
			move = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), mcts.target, DM.PATH);
		}
		
		previous_action = move.ordinal();
		
		if(move == MOVE.NEUTRAL)
		{
			System.out.println("NO TIME FOR MCTS");
		}
		
		GameView.addPoints(game,Color.CYAN, mcts.GetSelectedJuncs());
		GameView.addPoints(game,Color.BLUE, mcts.target);
		//GameView.addPoints(game, Color.ORANGE, mcts.current_pacman_path);
		
		if(mcts.rootNode != null)
		{
			for(MCTSNode c : mcts.rootNode.children)
			{
				float a = c.reward;
				
				if(a > 1.0f)
				{
					a = 1.0f;
				}
				else if(a < 0.0f)
				{
					a = 0.0f;
				}
				Color col = new Color(a,0.5f,0.8f);
				if(c.safe_path != null && mcts.helper.IsPathSafe(game, c.safe_path) )
					GameView.addPoints(game,Color.GREEN, c.safe_path);
			}
		}
//		GameView.addPoints(game,Color.CYAN, mcts.helper.GetIntersections());
		// TODO Auto-generated method stub
		return move;
	}

}
