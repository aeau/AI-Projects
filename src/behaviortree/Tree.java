package behaviortree;

import behaviortree.actions.*;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Tree 
{
	public Node root;// starting point of the tree
	public BlackBoard my_blackboard; //unique blackboard object passed through the tree
	
	public Tree()
	{
		my_blackboard = new BlackBoard();
		
		 // Simple behavior
		 /*
		root = new Selector();
			root.AddChild(new Sequence(root));
				root.GetChildren().get(0).AddChild(new Leaf(root.GetChildren().get(0), new GhostNearby()));
				root.GetChildren().get(0).AddChild(new Leaf(root.GetChildren().get(0), new RunAway()));
			root.AddChild(new Sequence(root));
				root.GetChildren().get(1).AddChild(new Leaf(root.GetChildren().get(1), new NearestPill()));
				root.GetChildren().get(1).AddChild(new Leaf(root.GetChildren().get(1), new GoTo()));
		
		*/
		//More "complicated" behavior
		
		
		root = new Selector();
			root.AddChild(new Sequence(root));
				root.GetChildren().get(0).AddChild(new Leaf(root.GetChildren().get(0), new GhostNearby()));
				root.GetChildren().get(0).AddChild(new Leaf(root.GetChildren().get(0), new RunAway()));
			root.AddChild(new Sequence(root));
				root.GetChildren().get(1).AddChild(new Leaf(root.GetChildren().get(1), new HasPowerPill()));
				root.GetChildren().get(1).AddChild(new Leaf(root.GetChildren().get(1), new GetNearbyScaredGhost()));
				root.GetChildren().get(1).AddChild(new Leaf(root.GetChildren().get(1), new GoTo()));				
			root.AddChild(new Sequence(root));
				root.GetChildren().get(2).AddChild(new Leaf(root.GetChildren().get(2), new PowerPillsAround()));
				root.GetChildren().get(2).AddChild(new Leaf(root.GetChildren().get(2), new GoTo()));
			root.AddChild(new Sequence(root));
				root.GetChildren().get(3).AddChild(new Leaf(root.GetChildren().get(3), new NearestPill()));
				root.GetChildren().get(3).AddChild(new Leaf(root.GetChildren().get(3), new GoTo()));
		
		/*
		root = new Selector();
			root.AddChild(new Sequence(root));
				root.GetChildren().get(0).AddChild(new Leaf(root.GetChildren().get(0), new GhostNearby()));
				root.GetChildren().get(0).AddChild(new Leaf(root.GetChildren().get(0), new RunAway()));
			root.AddChild(new Sequence(root));
				root.GetChildren().get(1).AddChild(new Leaf(root.GetChildren().get(1), new PowerPillsAround()));
				root.GetChildren().get(1).AddChild(new Leaf(root.GetChildren().get(1), new GoTo()));
			root.AddChild(new Sequence(root));
				root.GetChildren().get(2).AddChild(new Leaf(root.GetChildren().get(2), new NearestPill()));
				root.GetChildren().get(2).AddChild(new Leaf(root.GetChildren().get(2), new GoTo()));
		*/
				
		
	}
	
	/**
	 * Run the tree in a depth-first search way
	 */
	public void Execute()
	{
		root.Run(my_blackboard);
	}
	
	/**
	 * Update current state of the game into the blackboard
	 * @param game
	 */
	public void UpdateGameState(Game game)
	{
		my_blackboard.current_game = game;
		my_blackboard.pacman_position = game.getPacmanCurrentNodeIndex();
	}
	
	/**
	 * 
	 * @return next movement to be perform by the agent
	 */
	public MOVE GetMove()
	{
		return my_blackboard.next_move;
	}
}
