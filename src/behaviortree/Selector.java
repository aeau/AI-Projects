package behaviortree;

public class Selector extends Composite
{

	public Selector(Node n) {
		super(n);
		// TODO Auto-generated constructor stub
	}
	
	public Selector(Object n) {
		super(n);
		// TODO Auto-generated constructor stub
	}

	public Selector()
	{
		super();
	}
	
	@Override
	public NodeState Run(BlackBoard blackboard) {
		
		for(Node n : children)
		{
			if(n.Run(blackboard) == NodeState.Success)
			{
				ChangeState(NodeState.Success);
				return state;
			}
		}
		
		ChangeState(NodeState.Failure);
		return state;
	}

}
