package behaviortree;

import java.util.ArrayList;

/**
 * Abstract class that encapsulates individual information for the tree
 * @author A. Alvarez
 *
 */
public abstract class Node 
{

	
	protected ArrayList<Node> 	children; //children nodes
	protected Node 				parent;
	protected NodeState 		state; //current state of the node
	
	public Node()
	{
		children = new ArrayList<Node>();
		parent = null;
		state = NodeState.Idle;
	}
	
	public void AddChild(Node n)
	{
		children.add(n);
	}
	public ArrayList<Node> GetChildren()
	{
		return children;
	}
	public Node GetParent()
	{
		return parent;
	}
	
	public void ChangeState(NodeState new_state)
	{
		state = new_state;
	}
	
	/**
	 * Formatting to save node in txt file.
	 * @return save format
	 */
	public String SaveFormat()
	{
		String result = "";
		
		result += getClass().getSimpleName() + System.getProperty("line.separator");
		result += "{" + System.getProperty("line.separator");
		for(Node n : children)
		{
			result += n.SaveFormat();
		}
		result += "}" + System.getProperty("line.separator");
		return result;
	}
	
	public abstract NodeState Run(BlackBoard blackboard);
	
}
