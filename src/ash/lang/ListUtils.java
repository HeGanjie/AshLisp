package ash.lang;

import java.io.Serializable;
import java.util.Iterator;


public final class ListUtils {
	public static Node pair(Node keys, Node vals) {
		if (".".equals(keys.left)) // (. x) -> (1 2 3 ...) 
			return new Node(new Node(keys.next.left, new Node(vals)));
		else if (Node.NIL != keys && Node.NIL != vals)
			return new Node(new Node(keys.left, new Node(vals.left)), pair(keys.next, vals.next));
		return Node.NIL;
	}

	public static int count(Node src) {
		if (src == Node.NIL) return 0;
		return 1 + count(src.next);
	}
	
	public static Node take(int count, Node src) {
		if (src == Node.NIL) return Node.NIL;
		return count == 0 ? Node.NIL : new Node(src.left, take(count - 1, src.next));
	}
	
	public static Node drop(int count, Node src) {
		if (src == Node.NIL) return Node.NIL;
		return count == 0 ? src : drop(count - 1, src.next);
	}
	
	public static Node toNode(Iterator<? extends Serializable> tailNodeSeq) {
		if (!tailNodeSeq.hasNext()) return Node.NIL;
		return new Node(tailNodeSeq.next(), toNode(tailNodeSeq));
	}

	public static Node toNode(int skipElems, Serializable... tailNodeSeq) {
		if (tailNodeSeq.length == skipElems) return Node.NIL;
		return new Node(tailNodeSeq[skipElems], toNode(skipElems + 1, tailNodeSeq));
	}
	
	public static Node append(Node left, Node right) {
		if (left == Node.NIL) return right;
		return new Node(left.left, append(left.next, right));
	}
	
	public static Serializable assoc(String varName, Node environment) {
		final Node headNode = (Node) environment.left;
		if (headNode == null)
			return Node.NIL;
		else if (varName.equals(headNode.left))
			return headNode.next.left;
		else
			return assoc(varName, environment.next);
	}
	
	public static int indexOf(Node node, Object targetVal, int skiped) {
		if (node == Node.NIL) {
			return -1;
		} else if (node.left.equals(targetVal)) {
			return skiped;
		} else {
			return indexOf(node.next, targetVal, skiped + 1);
		}
	}
	
	public static Serializable atom(Object evalRes) {
		return evalRes instanceof Node
				? ((Node.NIL == evalRes ? Node.T : Node.NIL))
				: Node.T;
	}
	
	public static Serializable eq(Serializable a, Serializable b) { return a.equals(b) ? Node.T : Node.NIL; }
	
	public static Serializable car(Node arg) { return arg.left; }
	
	public static Node cdr(Node arg) { return arg.next; }

	public static Node cons(Serializable a, Node b) { return new Node(a, b); }
	
	public static final Serializable transformBoolean(boolean bl) { return bl ? Node.T : Node.NIL; }
}
