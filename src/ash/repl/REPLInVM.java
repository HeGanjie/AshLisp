package ash.repl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;

import bruce.common.utils.CommonUtils;
import ash.compiler.Compiler;
import ash.parser.Parser;
import ash.vm.VM;

public final class REPLInVM {

	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Parser parser = new Parser();
		VM vm = new VM();
		vm.preload();
		while (true) {
			try {
				String readIn;
				System.out.print("> ");
				readIn = br.readLine();
				if (readIn == null) break;
				long start = System.currentTimeMillis();
				Serializable val = vm.runInMain(Compiler.astsToInsts(parser.split(readIn)));
				System.out.println(val);
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
