package behaviortree;

public class Sequence extends Composite {

	public Sequence(Node n) {
		super(n);
		// TODO Auto-generated constructor stub
	}
	
	public Sequence(Object n) {
		super(n);
		// TODO Auto-generated constructor stub
	}

	
	public Sequence()
	{
		super();
	}

	@Override
	public NodeState Run(BlackBoard blackboard) {
		
		for(Node n : children)
		{
			if(n.Run(blackboard) == NodeState.Failure)
			{
				ChangeState(NodeState.Failure);
				return state;
			}
		}
		
		ChangeState(NodeState.Success);
		return state;
	}

}
