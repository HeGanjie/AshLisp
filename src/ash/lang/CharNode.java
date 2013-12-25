package ash.lang;


public class CharNode extends PersistentList {
	private static final long serialVersionUID = 2474414112862800452L;
	private final String src;
	private final int pos;

	private CharNode(String srcStr, int readingPos) {
		src = srcStr;
		pos = readingPos;
	}

	@Override
	public Character head() {
		return src.charAt(pos);
	}

	@Override
	public PersistentList rest() {
		int nextPos = pos + 1;
		if (nextPos == src.length())
			return BasicType.NIL;
		return new CharNode(src, nextPos);
	}

	public static PersistentList create(String src) {
		return new CharNode(src, 0);
	}

}
