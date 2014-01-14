package ash.repl;

import ash.compiler.Compiler;
import ash.parser.Parser;
import ash.vm.VM;

public final class REPLInVM {

	public static void main(String[] args) {
		new VM().batchRunInMain(Compiler.batchCompile(Parser.split("(load \"repl.scm\")")));
	}
}
