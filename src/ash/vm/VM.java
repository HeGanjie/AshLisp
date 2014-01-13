package ash.vm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ash.compiler.Compiler;
import ash.lang.PersistentList;
import ash.lang.Symbol;
import ash.parser.Parser;
import bruce.common.utils.FileUtil;

public final class VM implements Serializable {
	private static final long serialVersionUID = -3115756210819523693L;

	public static final Map<Symbol, Object> tempVar = new HashMap<>();
	protected final Deque<VMFrame> frameStack = new ArrayDeque<>();
	public VMFrame headFrame;
	
	static {
		new VM().load("loader.scm");
	}

	protected void load(String resName) {
		batchRunInMain(Compiler.batchCompile(Parser.split(FileUtil.readTextFileForDefaultEncoding(resName))));
	}

	public Object batchRunInMain(PersistentList compiledCodes) {
		Iterator<Object> iterator = compiledCodes.iterator();
		Object lastResult = null;
		while (iterator.hasNext()) {
			lastResult = runInMain((PersistentList) iterator.next());
		}
		return lastResult;
	}

	public Object runInMain(PersistentList instSeq) {
		List<Instruction> insts = instSeq.toList(Instruction.class);
		insts.add(InstructionSetEnum.halt.create()); // not ret because maybe nothing can be return
		return runFrame(new VMFrame(insts, null));
	}
	
	public Object runFrame(VMFrame mainFrame) {
		pushFrame(mainFrame);
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
		
		Object mainRtn = headFrame.popReturnValue();
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
}
