package behaviortree;

public abstract class Composite extends Node {

	public Composite(Node n)
	{
		super();
		parent = n;
	}

	public Composite(Object n)
	{
		super();
		parent = (Node)n;
	}
	
	public Composite()
	{
		super();
	}
	
}
