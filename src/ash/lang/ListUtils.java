package ash.lang;

import java.io.Serializable;
import java.util.Iterator;


public final class ListUtils {
	public static ISeq pair(ISeq keys, ISeq vals) {
		if (".".equals(keys.head())) // (. x) -> (1 2 3 ...) 
			return new Node(new Node(keys.rest().head(), new Node(vals)));
		else if (BasicType.NIL != keys && BasicType.NIL != vals)
			return new Node(new Node(keys.head(), new Node(vals.head())), pair(keys.rest(), vals.rest()));
		return BasicType.NIL;
	}

	public static int count(ISeq seq) {
		if (seq == BasicType.NIL) return 0;
		return 1 + count(seq.rest());
	}
	
	public static Node take(int count, ISeq seq) {
		if (seq == BasicType.NIL) return BasicType.NIL;
		return count == 0 ? BasicType.NIL : new Node(seq.head(), take(count - 1, seq.rest()));
	}
	
	public static ISeq drop(int count, ISeq seq) {
		if (seq == BasicType.NIL) return BasicType.NIL;
		return count == 0 ? seq : drop(count - 1, seq.rest());
	}
	
	public static Node toNode(Iterator<? extends Serializable> tailNodeSeq) {
		if (!tailNodeSeq.hasNext()) return BasicType.NIL;
		return new Node(tailNodeSeq.next(), toNode(tailNodeSeq));
	}

	public static Node toNode(int skipElems, Serializable... tailNodeSeq) {
		if (tailNodeSeq.length == skipElems) return BasicType.NIL;
		return new Node(tailNodeSeq[skipElems], toNode(skipElems + 1, tailNodeSeq));
	}
	
	public static Node append(ISeq left, Node right) {
		if (left == BasicType.NIL) return right;
		return new Node(left.head(), append(left.rest(), right));
	}
	
	public static Serializable assoc(String varName, ISeq environment) {
		final Node headNode = (Node) environment.head();
		if (headNode == null)
			return BasicType.NIL;
		else if (varName.equals(headNode.head()))
			return headNode.rest().head();
		else
			return assoc(varName, environment.rest());
	}
	
	public static int indexOf(ISeq seq, Object targetVal, int skiped) {
		if (seq == BasicType.NIL) {
			return -1;
		} else if (seq.head().equals(targetVal)) {
			return skiped;
		} else {
			return indexOf(seq.rest(), targetVal, skiped + 1);
		}
	}
	
	public static Serializable atom(Object evalRes) {
		return evalRes instanceof Node
				? ((BasicType.NIL == evalRes ? BasicType.T : BasicType.NIL))
				: BasicType.T;
	}
	
	public static Serializable eq(Serializable a, Serializable b) { return a.equals(b) ? BasicType.T : BasicType.NIL; }
	
	public static Serializable car(Node arg) { return arg.head(); }
	
	public static ISeq cdr(Node arg) { return arg.rest(); }

	public static Node cons(Serializable a, Node b) { return new Node(a, b); }
	
	public static final Serializable transformBoolean(boolean bl) { return bl ? BasicType.T : BasicType.NIL; }
}
