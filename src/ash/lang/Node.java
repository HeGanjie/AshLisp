package ash.lang;


public final class Node extends PersistentList {
	private static final long serialVersionUID = -3802355140695976127L;
	private final Object left;
	private final PersistentList next;
	
	public Node(Object val) { this(val, BasicType.NIL); }

	public Node(Object l, PersistentList n) {
		left = l;
		next = n;
	}

	@Override
	public Object head() { return left; }

	@Override
	public PersistentList rest() { return next; }

}
