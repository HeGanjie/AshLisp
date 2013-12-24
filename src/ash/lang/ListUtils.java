package ash.lang;

import java.util.Iterator;


public final class ListUtils {
	public static PersistentList pair(PersistentList keys, PersistentList vals) {
		if (".".equals(keys.head())) // (. x) -> (1 2 3 ...) 
			return new Node(new Node(keys.rest().head(), new Node(vals)));
		else if (BasicType.NIL != keys && BasicType.NIL != vals)
			return new Node(new Node(keys.head(), new Node(vals.head())), pair(keys.rest(), vals.rest()));
		return BasicType.NIL;
	}

	public static int count(PersistentList seq) {
		if (seq == BasicType.NIL) return 0;
		return 1 + count(seq.rest());
	}
	
	public static Node take(int count, PersistentList seq) {
		if (seq == BasicType.NIL) return BasicType.NIL;
		return count == 0 ? BasicType.NIL : new Node(seq.head(), take(count - 1, seq.rest()));
	}
	
	public static PersistentList drop(int count, PersistentList seq) {
		if (seq == BasicType.NIL) return BasicType.NIL;
		return count == 0 ? seq : drop(count - 1, seq.rest());
	}
	
	public static PersistentList toSeq(Iterator<PersistentList> tailNodeSeq) {
		if (!tailNodeSeq.hasNext()) return BasicType.NIL;
		return new Node(tailNodeSeq.next().head(), toSeq(tailNodeSeq));
	}
	
	public static PersistentList toSeq(int skipElems, Object... tailNodeSeq) {
		if (tailNodeSeq.length == skipElems) return BasicType.NIL;
		return new Node(tailNodeSeq[skipElems], toSeq(skipElems + 1, tailNodeSeq));
	}
		
	public static Node append(PersistentList left, Node right) {
		if (left == BasicType.NIL) return right;
		return new Node(left.head(), append(left.rest(), right));
	}
	
	public static Object assoc(String varName, PersistentList environment) {
		final Node headNode = (Node) environment.head();
		if (headNode == null)
			return BasicType.NIL;
		else if (varName.equals(headNode.head()))
			return headNode.rest().head();
		else
			return assoc(varName, environment.rest());
	}
	
	public static int indexOf(PersistentList seq, Object targetVal, int skiped) {
		if (seq == BasicType.NIL) {
			return -1;
		} else if (seq.head().equals(targetVal)) {
			return skiped;
		} else {
			return indexOf(seq.rest(), targetVal, skiped + 1);
		}
	}
	
	public static Object atom(Object evalRes) {
		return evalRes instanceof PersistentList
				? ((BasicType.NIL == evalRes ? BasicType.T : BasicType.NIL))
				: BasicType.T;
	}
	
	public static Object eq(Object a, Object b) { return a.equals(b) ? BasicType.T : BasicType.NIL; }
	
	public static Object car(PersistentList arg) { return arg.head(); }
	
	public static PersistentList cdr(PersistentList arg) { return arg.rest(); }

	public static PersistentList cons(Object a, PersistentList b) { return new Node(a, b); }
	
	public static final Object transformBoolean(boolean bl) { return bl ? BasicType.T : BasicType.NIL; }
}
