package behaviortree;

/**
 * Node that controls the flow of the tree
 * @author A. Alvarez
 *
 */
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
