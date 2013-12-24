package ash.repl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ash.compiler.Compiler;
import ash.lang.BasicType;
import ash.parser.Parser;
import ash.vm.VM;
import bruce.common.utils.CommonUtils;

public final class REPLInVM {

	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		VM vm = new VM();
		
		while (true) {
			try {
				String readIn;
				System.out.print("> ");
				readIn = br.readLine();
				if (readIn == null) break;
				long start = System.currentTimeMillis();
				Object val = vm.runInMain(Compiler.astsToInsts(Parser.split(readIn)));
				System.out.println(BasicType.asString(val));
				reportElapse(System.currentTimeMillis() - start);
			} catch (Exception e) {
				e.printStackTrace();
				vm.init();
				CommonUtils.delay(100);
			}
		}
	}
	
	protected static void reportElapse(long elapse) {
		if (100 < elapse)
			System.out.println(String.format("Eval Elapse: %gs", (double) elapse / 1000));
	}
}
