package behaviortree;

/**
 * Different states a node can be (only using success and failure at the moment)
 * @author A. Alvarez
 *
 */
public enum NodeState {

	Success,
	Failure,
	Running,
	Idle
}
