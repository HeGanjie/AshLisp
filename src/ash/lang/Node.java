package ash.lang;

import java.io.Serializable;

import bruce.common.utils.CommonUtils;

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
	
	private String innerToString() {
		StringBuilder sb = new StringBuilder();
		if (left instanceof Node)
			sb.append(left);
		else if (left != null)
			sb.append(BasicType.asString(left));
		
		if (BasicType.NIL != next && BasicType.NIL != this) {
			sb.append(' ');
			sb.append(((Node) next).innerToString());
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return CommonUtils.buildString('(', innerToString(), ')');
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((left == null) ? 0 : left.hashCode());
		return prime * result + ((next == null) ? 0 : next.hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Node other = (Node) obj;
		if (left == null) {
			if (other.head() != null)
				return false;
		} else if (!left.equals(other.head()))
			return false;
		if (next == null) {
			if (other.rest() != null)
				return false;
		} else if (!next.equals(other.rest()))
			return false;
		return true;
	}
}
