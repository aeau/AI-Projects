package behaviortree;

import behaviortree.actions.*;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Tree 
{
	public Node root;
	public BlackBoard my_blackboard;
	
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
	
	public void Execute()
	{
		root.Run(my_blackboard);
	}
	
	public void UpdateGameState(Game game)
	{
		my_blackboard.current_game = game;
		my_blackboard.pacman_position = game.getPacmanCurrentNodeIndex();
	}
	
	public MOVE GetMove()
	{
		return my_blackboard.next_move;
	}
}
