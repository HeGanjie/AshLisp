package ash.vm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bruce.downloader.framework.functional.Func2;
import bruce.downloader.framework.functional.LambdaUtils;
import bruce.downloader.framework.utils.FileUtil;
import ash.compiler.Compiler;
import ash.parser.Node;
import ash.parser.Parser;

public final class VM implements Serializable {
	private static final long serialVersionUID = -3115756210819523693L;

	public static final Map<String, Serializable> tempVar = new HashMap<>();
	protected final Deque<VMFrame> frameStack = new ArrayDeque<>(64);
	public VMFrame headFrame;
	
	public VM(Parser p) {
		if (p == null) return;
		load(p, "maths.scm");
		load(p, "funs.scm");
		load(p, "utils.scm");
	}

	protected void load(Parser parser, String resName) {
		runInMain(Compiler.astsToInsts(parser.split(FileUtil.readTextFileForDefaultEncoding(resName))));
	}

	public Serializable runInMain(Node compiledCodes) {
		List<Instruction> allInstInMain = LambdaUtils.reduce(compiledCodes, new ArrayList<Instruction>(), new Func2<List<Instruction>, List<Instruction>, Node>() {
			@Override
			public List<Instruction> call(List<Instruction> t, Node t2) {
				t.addAll(((Node) t2.left).toList(Instruction.class));
				return t;
			}
		});
		allInstInMain.add(InstructionSetEnum.halt.create()); // not ret because maybe nothing can be return
		pushFrame(new VMFrame(Arrays.asList(InstructionSetEnum.halt.create()), null)); // base frame
		pushFrame(new VMFrame(allInstInMain, null)); // main frame
		return run();
	}
	
	private Serializable run() {
		while (!frameStack.isEmpty()) {
			headFrame = frameStack.peek();
			headFrame.execUntilStackChange();
			if (headFrame.nextFrame != null)
				pushFrame(headFrame.nextFrame);
			else
				popFrame();
		}
		
		Serializable mainRtn = headFrame.popWorkingStack();
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
