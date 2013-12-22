package ash.lang;

import java.io.Serializable;

public interface ISeq extends Serializable, Iterable<ISeq> {
	Serializable head();
	ISeq rest();
}
