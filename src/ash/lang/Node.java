package ash.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bruce.common.utils.CommonUtils;

public final class Node implements Iterable<Node>, Serializable {
	private static final long serialVersionUID = -3802355140695976127L;
	
	public static final String T = "t";
	public static final Node NIL = new Node(null, null);
	
	public final Serializable left;
	public final Node next;
	
	public Node(Serializable val) { this(val, NIL); }

	public Node(Node node) { this(node, NIL); }

	public Node(Serializable l, Node n) {
		left = l instanceof String ? BasicType.realType((String) l) : l;
		next = n;
	}

	private String innerToString() {
		StringBuilder sb = new StringBuilder();
		if (left instanceof Node)
			sb.append(left);
		else if (left != null)
			sb.append(BasicType.asString(left));
		
		if (NIL != next && NIL != this) {
			sb.append(' ');
			sb.append(next.innerToString());
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
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (next == null) {
			if (other.next != null)
				return false;
		} else if (!next.equals(other.next))
			return false;
		return true;
	}

	@Override
	public Iterator<Node> iterator() {
		return new Iterator<Node>() {
			Node head = Node.this;
			@Override
			public boolean hasNext() { return NIL != head; }
			@Override
			public Node next() {
				Node curr = head;
				head = head.next;
				return curr;
			}
			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> toList(Class<T> c) {
		List<T> arrayList = new ArrayList<>();
		for (Node n : this) {
			arrayList.add((T) n.left);
		}
		return arrayList;
	}
}
