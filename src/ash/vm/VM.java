package ash.vm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bruce.common.functional.Func2;
import bruce.common.functional.LambdaUtils;
import bruce.common.utils.FileUtil;
import ash.compiler.Compiler;
import ash.lang.PersistentList;
import ash.lang.Node;
import ash.parser.Parser;

public final class VM implements Serializable {
	private static final long serialVersionUID = -3115756210819523693L;

	public static final Map<String, Object> tempVar = new HashMap<>();
	protected final Deque<VMFrame> frameStack = new ArrayDeque<>();
	public VMFrame headFrame;
	
	static {
		VM vm = new VM();
		vm.load("meta.scm");
		vm.load("macro.scm");
		vm.load("maths.scm");
		vm.load("lazy.scm");
		vm.load("utils.scm");
		vm.load("jni.scm");
		vm.load("user.scm");
	}

	protected void load(String resName) {
		runInMain(Compiler.astsToInsts(Parser.split(FileUtil.readTextFileForDefaultEncoding(resName))));
	}

	public Object runInMain(PersistentList compiledCodes) {
		List<Instruction> allInstInMain = LambdaUtils.reduce(compiledCodes, new ArrayList<Instruction>(),
				new Func2<List<Instruction>, List<Instruction>, PersistentList>() {
			@Override
			public List<Instruction> call(List<Instruction> insts, PersistentList instList) {
				insts.addAll(((Node) instList.head()).toList(Instruction.class));
				return insts;
			}
		});
		allInstInMain.add(InstructionSetEnum.halt.create()); // not ret because maybe nothing can be return
		pushFrame(new VMFrame(Arrays.asList(InstructionSetEnum.halt.create()), null)); // base frame
		pushFrame(new VMFrame(allInstInMain, null)); // main frame
		return run();
	}
	
	private Object run() {
		while (!frameStack.isEmpty()) {
			headFrame = frameStack.peek();
			headFrame.execUntilStackChange();
			if (headFrame.nextFrame != null)
				pushFrame(headFrame.nextFrame);
			else
				popFrame();
		}
		
		Object mainRtn = headFrame.popWorkingStack();
		headFrame = null;
		return mainRtn;
	}

	private void popFrame() {
		headFrame.prevFrame = null;
		
		frameStack.pop();
		headFrame = frameStack.isEmpty() ? headFrame /* not null, for getting the return value */ : frameStack.peek();
		
		if (headFrame != null)
			headFrame.nextFrame = null;
	}

	private void pushFrame(VMFrame vmFrame) {
		//headFrame.nextFrame = vmFrame; already set by InstructionSetEnum.call
		vmFrame.prevFrame = headFrame;
		
		frameStack.push(vmFrame);
		headFrame = vmFrame;
	}

	public void init() {
		frameStack.clear();
		headFrame = null;
	}
}
