package ash.lang;

import java.io.Serializable;

public final class Node extends PersistentList {
	private static final long serialVersionUID = -3802355140695976127L;
	private final Serializable left;
	private final PersistentList next;
	
	public Node(Serializable val) { this(val, BasicType.NIL); }

	public Node(Node node) { this(node, BasicType.NIL); }

	public Node(Serializable l, PersistentList n) {
		left = l instanceof String ? BasicType.realType((String) l) : l;
		next = n;
	}

	@Override
	public Serializable head() { return left; }

	@Override
	public PersistentList rest() { return next; }
	
}
