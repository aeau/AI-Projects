package behaviortree;

import java.util.ArrayList;

import behaviortree.actions.Action;

public class Leaf extends Node {
	
	public Action action;
	
	public Leaf(Node n)
	{
		super();
		parent = n;
	}

	public Leaf(Node n, Action a)
	{
		super();
		parent = n;
		action = a;
	}
	
	public Leaf(Action a)
	{
		super();
		action = a;
	}
	
	public Leaf(Node n, Object a)
	{
		super();
		parent = n;
		action = (Action)a;
	}
	
	@Override
	public NodeState Run(BlackBoard blackboard) {
		// TODO Auto-generated method stub
		if(action.Execute(blackboard))
		{
			ChangeState(NodeState.Success);
		}
		else
		{
			ChangeState(NodeState.Failure);
		}
		
		return state;
	}
	
	@Override
	public String SaveFormat()
	{
		String result = "";
		
		result += getClass().getSimpleName() + ";";
		result += action.getClass().getSimpleName() + System.getProperty("line.separator");
		
		return result;
	}

}
