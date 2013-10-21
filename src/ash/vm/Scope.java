package ash.vm;

import java.io.Serializable;

public final class Scope implements Serializable {
	private static final long serialVersionUID = 569126524646085504L;
	
	final Scope prevScope;
	final Serializable[] environment;
	
	public Scope(Scope srcScope, Serializable[] env) {
		prevScope = srcScope;
		environment = env;
	}

	public Serializable queryArg(int argIndex) {
		if (argIndex < environment.length)
			return environment[argIndex];
		else
			return prevScope.queryArg(environment.length - argIndex);
	}
}
