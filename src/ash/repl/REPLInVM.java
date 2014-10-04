package ash.repl;

import ash.util.JavaUtils;
import ash.compiler.Compiler;
import ash.lang.BasicType;
import ash.parser.Parser;
import ash.vm.VM;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class REPLInVM {

	public static void main(String[] args) {
		VM vm = new VM();
		/*if (!VM.debugging)
			vm.batchRunInMain(Compiler.batchCompile(Parser.parse("(load \"repl.scm\")")));
		else*/ {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			while (true) {
				try {
					String readIn;
					System.out.print("> ");
					readIn = br.readLine();
					if (readIn == null) break;
					if (!JavaUtils.isStringNullOrWriteSpace(readIn)) {
						Object val = vm.batchRunInMain(Compiler.batchCompile(Parser.parse(readIn)));
						System.out.println(BasicType.asString(val));
					}
				} catch (Exception e) {
					e.printStackTrace();
					JavaUtils.delay(1000);
					vm = new VM();
				}
			}
		}
	}
}
