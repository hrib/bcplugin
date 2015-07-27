package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.ParameterNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

import cz.vutbr.fit.xhriba01.bc.lib.AbstractNodeVisitor;
import cz.vutbr.fit.xhriba01.bc.lib.DescriptorPart;
import cz.vutbr.fit.xhriba01.bc.lib.NodeClass;
import cz.vutbr.fit.xhriba01.bc.lib.NodeField;
import cz.vutbr.fit.xhriba01.bc.lib.NodeInstruction;
import cz.vutbr.fit.xhriba01.bc.lib.NodeMethod;

public class UserBytecode extends AbstractNodeVisitor {
	
	public static class UserBytecodeDocument extends Document {
		
		private TextPresentation fTextPresentation = new TextPresentation(); 
		
		public static String SPACE = " ";
		
		public static String INDENT = "\t";
		
		public static String LINE = "\n";
		
		public static String START = "{";
		
		public static String END = "}";
		
		private int fIndentCount = 0;
		
		private String fIndent = "";
		
		private Map<Integer, Integer> fLineMap = new HashMap<>();
		
		private int fMaxLineNumber = 1;
		
		private Style STYLE;
		
		public UserBytecodeDocument(Style style) {
			super();
			STYLE = style;
		}
		
		public int getMaxLineNumber() {
			return fMaxLineNumber;
		}
		
		public Map<Integer, Integer> getLineMap() {
			return fLineMap;
		}
		
		public int getCurrentLineNumber() {
			return getTracker().getNumberOfLines();
		}
		
		public void addToLineMap(int javaEditorLine) {
			addToLineMap(getCurrentLineNumber(), javaEditorLine);
		}
		
		public void addToLineMap(int bytecodeViewerLine, int javaEditorLine) {
			if (javaEditorLine == -1) return;
			fLineMap.put(bytecodeViewerLine, javaEditorLine);
			if (javaEditorLine > fMaxLineNumber) {
				fMaxLineNumber = javaEditorLine;
			}
		}
		
		public void increaseIndent() {
			
			fIndentCount++;
			
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < fIndentCount; i++) {
				sb.append(INDENT);
			}
			
			fIndent = sb.toString();
			
		}
		
		public void decreaseIndent() {
			
			if (fIndentCount > 0)
				fIndentCount--;
			
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < fIndentCount; i++) {
				sb.append(INDENT);
			}
			
			fIndent = sb.toString();
			
		}
		
		
		public void start() {
			
			add(START);
			addLine();
			
		}
		
		
		
		public void addIndent() {
			add(fIndent);
		}
		
		public void addWithIndent(TextStyle style, String text) {
			
			addIndent();
			add(style, text);
			
		}
		
		public UserBytecodeDocument(String initialContent) {
			super(initialContent);
		}
		
		public TextPresentation getTextPresentation() {
			return fTextPresentation;
		}
		
		public void add(String text) {
			
			try {
				
				replace(getLength(), 0, text);
				
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private void applyTextStyle(TextStyle textStyle, StyleRange styleRange) {
			
			styleRange.font = textStyle.font;
			styleRange.foreground = textStyle.foreground;
			styleRange.background = textStyle.background;
			styleRange.underline = textStyle.underline;
			styleRange.underlineColor = textStyle.underlineColor;
			styleRange.underlineStyle = textStyle.underlineStyle;
			styleRange.strikeout = textStyle.strikeout;
			styleRange.strikeoutColor = textStyle.strikeoutColor;
			styleRange.borderStyle = textStyle.borderStyle;
			styleRange.borderColor = textStyle.borderColor;
			styleRange.metrics = textStyle.metrics;
			styleRange.rise = textStyle.rise;
			styleRange.data = textStyle.data;
			
		}
		
		public void add(TextStyle style, String text) {
			
			int oldLen = getLength();
			
			add(text);
			
			int newLen = getLength();
			
			if (style == null) return;
			
			StyleRange styleRange = new StyleRange();
			
			applyTextStyle(style, styleRange);
			
			styleRange.start = oldLen;
			styleRange.length = newLen - oldLen;
			
			fTextPresentation.addStyleRange(styleRange);
		}
		
		public void addAndLine(TextStyle style, String text) {
			
			add(style, text);
			addLine();
		}
		
		public void addLine() {
			add(LINE);
			add(LINE);
		}
		
		public void addAndSpace(TextStyle style, String text) {
			if (text.length() == 0) return;
			add(style, text);
			add(SPACE);
		}
		
		public void addAndSpace(String text) {
			if (text.length() == 0) return;
			add(text);
			add(SPACE);
		}
		
		public void startLine() {
			addIndent();
		}
		
		public void endLine() {
			addLine();
		}
		
		public void startContext() {
			addColumn(START);
			addLine();
			increaseIndent();
		}
		
		public void endContext() {
			decreaseIndent();
			addIndent();
			addColumn(END);
		}
		
		public void addColumn(String text) {
			addAndSpace(text);
		}
		
		public void addColumn(TextStyle style, String text) {
			addAndSpace(style, text);
		}
		
		
		public String formatSourceFieldAccessFlags(int flag) {
			return "";
		}
		
		public String formatNonSourceFieldAccessFlags(int flag) {
			return "";
		}
		
		public String formatSourceMethodAccessFlags(int flag) {
			List<String> accesses = new ArrayList<>();
			if ((flag & Opcodes.ACC_PUBLIC) != 0) {
				accesses.add("public");
			}
			if ((flag & Opcodes.ACC_PROTECTED) != 0) {
				accesses.add("protected");
			}
			if ((flag & Opcodes.ACC_PRIVATE) != 0) {
				accesses.add("private");
			}
			if ((flag & Opcodes.ACC_STATIC) != 0) {
				accesses.add("static");
			}
			if ((flag & Opcodes.ACC_ABSTRACT) != 0) {
				accesses.add("abstract");
			}
			if ((flag & Opcodes.ACC_FINAL) != 0) {
				accesses.add("final");
			}
			
			return StringUtils.join(accesses, " ");
				
		}
		
		public String formatNonSourceMethodAccessFlags(int flag) {
			return "";
		}
		
		public String formatSourceClassAccessFlags(int flag) {
			return "";
		}
		
		public String formatNonSourceClassAccessFlags(int flag) {
			return "";
		}
		
		public String addNonSourceMethodAccessFlags(int flag) {
			return "";
		}
		
		public void addType(Type type) {
			
			DescriptorPart returnType =  DescriptorPart.fromAsmType(type);
			
			TextStyle style = null;
			
			if (returnType.fType == DescriptorPart.TYPE.PRIMITIVE || returnType.fType == DescriptorPart.TYPE.ARRAY_PRIMITIVE) {
				style = STYLE.TYPE_PRIMITIVE;
			}
			else {
				style = STYLE.TYPE_OBJECT;
			}
			
			add(style, returnType.toString(false));
			
			if (returnType.isArray()) {
				add(STYLE.ARRAY_DIMENSIONS, returnType.getDimensionsString());
			}
			
		}
		
		public void addMethodReturnValue(MethodNode methodNode) {
			
			addType(Type.getReturnType(methodNode.desc));
			
			add(SPACE);
		}
		
		public void addMethodParameters(NodeMethod nodeMethod) {
			
			
			int isStatic = nodeMethod.isStatic() ? 0 : 1;
			
			MethodNode methodNode = nodeMethod.getAsmMethodNode();
			
			add("(");
			
			Type[] types = Type.getArgumentTypes(methodNode.desc);
			
			int len = types.length;
			
			if (len == 0) {
				add(")");
				return;
			}
			
			List<ParameterNode> parameters = methodNode.parameters;
			
			List<LocalVariableNode> locals = methodNode.localVariables;
			
			for(int i = 0; i < len;) {
				if (i > 0) {
					add(SPACE);
				}
				addType(types[i]);
				if (parameters != null && parameters.size() > i) {
					add(SPACE);
					add(STYLE.PARAMETER_NAME, parameters.get(i).name);
				}
				else if (locals != null && locals.size() > i) {
					LocalVariableNode localNode = locals.get(i + isStatic);
					add(SPACE);
					add(STYLE.PARAMETER_NAME, localNode.name);
				}
				if (++i < len) {
					add(",");
				}
			}
			
			add(")");
			
		}
		
		public void addSignature(MethodNode methodNode) {
			
			String signature = methodNode.signature;
			
			if (signature != null) {
				add(STYLE.COMMENT, "// SIGNATURE: " + signature);
				add(LINE);
				startLine();
			}
			
		}
	}
 	
	private UserBytecodeDocument fDocument;
	
	private Style STYLE;
	
	public UserBytecode(Style style) {
		
		STYLE = style;
		fDocument = new UserBytecodeDocument(style);
	}
	
	public UserBytecodeDocument getDocument() {
		return fDocument;
	}
	
	/**
	 * Called for each node that represents a class (class, interface, enum).
	 * 
	 * @param nodeClass the class node
	 */
	public void visitNodeClass(NodeClass nodeClass) {
		
		fDocument.startLine();
		
		fDocument.addColumn(STYLE.CLASS_NAME, nodeClass.getAsmClassNode().name);
		
		fDocument.startContext();
		
		
	}
	
	/**
	 * Called after the visitNodeClass and after all NodeClass childrens
	 * are processed.
	 * 
	 * @param nodeClass the class node
	 */
	public void afterVisitNodeClass(NodeClass nodeClass) {
		
		fDocument.endContext();
		
		fDocument.endLine();
		
	}
	
	/**
	 * Called for each node that represents a method.
	 * 
	 * @param nodeMethod the method node
	 */
	public void visitNodeMethod(NodeMethod nodeMethod) {
		
		fDocument.startLine();
		
		MethodNode methodNode = nodeMethod.getAsmMethodNode();
		
		//fDocument.addNonSourceMethodAccessFlags(methodNode.access);
		
		fDocument.addSignature(methodNode);
		
		fDocument.addColumn(STYLE.METHOD_ACCESS, fDocument.formatSourceMethodAccessFlags(methodNode.access));
		
		fDocument.addMethodReturnValue(methodNode);
		
		fDocument.add(STYLE.METHOD_NAME, nodeMethod.getAsmMethodNode().name);
		
		fDocument.addToLineMap(nodeMethod.getStartLine());
		
		fDocument.addMethodParameters(nodeMethod);
		
		fDocument.startContext();
	}
	
	/**
	 * Called after the visitNodeMethod and after all NodeMethod childrens
	 * are processed.
	 * 
	 * @param nodeMethod the method node
	 */
	public void afterVisitNodeMethod(NodeMethod nodeMethod) {
		
		fDocument.endContext();
		
		fDocument.endLine();
		
	}
	
	/**
	 * Called for each node that represents a field.
	 * 
	 * @param nodeField the field node
	 */
	public void visitNodeField(NodeField nodeField) {
		
		fDocument.startLine();
		
		FieldNode fieldNode = nodeField.getAsmFieldNode();
		
		fDocument.addColumn(fDocument.formatSourceFieldAccessFlags(fieldNode.access));
		
		fDocument.addType(Type.getType(fieldNode.desc));
		fDocument.add(fDocument.SPACE);
		
		fDocument.add(STYLE.FIELD_NAME, nodeField.getAsmFieldNode().name);
		
		if (nodeField.hasChilds()) {
			fDocument.add(fDocument.SPACE);
			fDocument.startContext();
		}
		else {
			fDocument.add(";");
			fDocument.endLine();
		}
		
	}
	
	public void afterVisitNodeField(NodeField nodeField) {
		
		if (nodeField.hasChilds()) {
			fDocument.endContext();
			fDocument.endLine();
		}
		
	}
	
	/**
	 * When called, indicates that subsequent instructions are generated
	 * from line that is represented by this LineNumberNode.
	 * It is not real jvm instruction.
	 * 
	 * @param node the asm node
	 * @param nodeInstruction the node
 	 */
	protected void visitLineNumberNode(LineNumberNode node, NodeInstruction nodeInstruction) {}
	
	
	/**
	 * Called for each start of new instruction.
	 * 
	 * @param node the asm node
	 * @param nodeInstruction the node
	 */
	protected void visitLabelNode(LabelNode node, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(Integer.toString(node.getLabel().getOffsetInMethod()) + ": ");
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each instruction that manipulates object fields.
	 * <b>Possible instructions:</b>
	 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.getstatic">getstatic</a> 
	 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.putstatic">putstatic</a>
	 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.getfield">getfield</a>
	 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.putfield">putfield</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitFieldInsn(FieldInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each instruction that increments local int variable.
	 * <b>Possible instructions:</b>
	 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.iinc">iinc</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitIincInsn(IincInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.add(fDocument.SPACE);
		fDocument.add("index: ");
		fDocument.add(Integer.toString(insn.var));
		fDocument.add(", value: ");
		fDocument.add(Integer.toString(insn.incr));
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each instruction that has zero operands.
	 * <b>Possible instructions:</b>
	 * @see <a href="">nop</a>
	 * @see <a href="">aconst_null</a>
	 * @see <a href="">iconst_m1</a>
	 * @see <a href="">iconst_0</a>
	 * @see <a href="">iconst_1</a>
	 * @see <a href="">iconst_2</a>
	 * @see <a href="">iconst_3</a>
	 * @see <a href="">iconst_4</a>
	 * @see <a href="">iconst_5</a>
	 * @see <a href="">lconst_0</a>
	 * @see <a href="">lconst_1</a>
	 * @see <a href="">fconst_0</a>
	 * @see <a href="">fconst_1</a>
	 * @see <a href="">fconst_2</a>
	 * @see <a href="">dconst_0</a>
	 * @see <a href="">dconst_1</a>
	 * @see <a href="">iaload</a>
	 * @see <a href="">laload</a>
	 * @see <a href="">faload</a>
	 * @see <a href="">daload</a>
	 * @see <a href="">aaload</a>
	 * @see <a href="">baload</a>
	 * @see <a href="">caload</a>
	 * @see <a href="">saload</a>
	 * @see <a href="">iastore</a>
	 * @see <a href="">lastore</a>
	 * @see <a href="">fastore</a>
	 * @see <a href="">dastore</a>
	 * @see <a href="">aastore</a>
	 * @see <a href="">bastore</a>
	 * @see <a href="">castore</a>
	 * @see <a href="">sastore</a>
	 * @see <a href="">pop</a>
	 * @see <a href="">pop2</a>
	 * @see <a href="">dup</a>
	 * @see <a href="">dup_x1</a>
	 * @see <a href="">dup_x2</a>
	 * @see <a href="">dup2</a>
	 * @see <a href="">dup2_x1</a>
	 * @see <a href="">dup2_x2</a>
	 * @see <a href="">swap</a>
	 * @see <a href="">iadd</a>
	 * @see <a href="">ladd</a>
	 * @see <a href="">fadd</a>
	 * @see <a href="">dadd</a>
	 * @see <a href="">isub</a>
	 * @see <a href="">lsub</a>
	 * @see <a href="">fsub</a>
	 * @see <a href="">dsub</a>
	 * @see <a href="">imul</a>
	 * @see <a href="">lmul</a>
	 * @see <a href="">fmul</a>
	 * @see <a href="">dmul</a>
	 * @see <a href="">idiv</a>
	 * @see <a href="">ldiv</a>
	 * @see <a href="">ddiv</a>
	 * @see <a href="">irem</a>
	 * @see <a href="">lrem</a>
	 * @see <a href="">frem</a>
	 * @see <a href="">drem</a>
	 * @see <a href="">ineg</a>
	 * @see <a href="">lneg</a>
	 * @see <a href="">fneg</a>
	 * @see <a href="">dneg</a>
	 * @see <a href="">ishl</a>
	 * @see <a href="">lshl</a>
	 * @see <a href="">ishr</a>
	 * @see <a href="">lshr</a>
	 * @see <a href="">iushr</a>
	 * @see <a href="">lushr</a>
	 * @see <a href="">iand</a>
	 * @see <a href="">land</a>
	 * @see <a href="">ior</a>
	 * @see <a href="">lor</a>
	 * @see <a href="">ixor</a>
	 * @see <a href="">lxor</a>
	 * @see <a href="">i2l</a>
	 * @see <a href="">i2f</a>
	 * @see <a href="">i2d</a>
	 * @see <a href="">l2i</a>
	 * @see <a href="">l2f</a>
	 * @see <a href="">l2d</a>
	 * @see <a href="">f2i</a>
	 * @see <a href="">f2l</a>
	 * @see <a href="">f2d</a>
	 * @see <a href="">d2i</a>
	 * @see <a href="">d2l</a>
	 * @see <a href="">d2f</a>
	 * @see <a href="">i2b</a>
	 * @see <a href="">i2c</a>
	 * @see <a href="">i2s</a>
	 * @see <a href="">lcmp</a>
	 * @see <a href="">fcmpl</a>
	 * @see <a href="">fcmpg</a>
	 * @see <a href="">dcmpl</a>
	 * @see <a href="">dcmpg</a>
	 * @see <a href="">ireturn</a>
	 * @see <a href="">lreturn</a>
	 * @see <a href="">freturn</a>
	 * @see <a href="">dreturn</a>
	 * @see <a href="">areturn</a>
	 * @see <a href="">return</a>
	 * @see <a href="">arraylenght</a>
	 * @see <a href="">athrow</a>
	 * @see <a href="">monitorenter</a>
	 * @see <a href="">monitorexit</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitInsn(InsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.add(fDocument.LINE);
		
	}
	
	
	/**
	 * Called for each instruction that has single int operand.
	 * <b>Possible instructions:</b>
	 * @see <a href="">bipush</a>
	 * @see <a href="">sipush</a>
	 * @see <a href="">newarray</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitIntInsn(IntInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.add(fDocument.SPACE + Integer.toString(insn.operand));
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each invokedynamic instruction.
	 * <b>Possible instructions:</b>
	 * @see <a href="">invokedynamic</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitInvokeDynamicInsn(InvokeDynamicInsnNode insn, NodeInstruction nodeInstruction) {}
	
	/**
	 * Called for each instruction that may jump to another instruction.
	 * <b>Possible instructions:</b>
	 * @see <a href="">ifeq</a>
	 * @see <a href="">ifne</a>
	 * @see <a href="">iflt</a>
	 * @see <a href="">ifge</a>
	 * @see <a href="">ifgt</a>
	 * @see <a href="">ifle</a>
	 * @see <a href="">if_icmpeq</a>
	 * @see <a href="">if_icmpne</a>
	 * @see <a href="">if_icmplt</a>
	 * @see <a href="">if_icmpge</a>
	 * @see <a href="">if_icmpgt</a>
	 * @see <a href="">if_icmple</a>
	 * @see <a href="">if_acmpeq</a>
	 * @see <a href="">if_acmpne</a>
	 * @see <a href="">goto</a>
	 * @see <a href="">jsr</a>
	 * @see <a href="">ifnull</a>
	 * @see <a href="">ifnonnull</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */	
	protected void visitJumpInsn(JumpInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.add(fDocument.SPACE + Integer.toString(insn.label.getLabel().getOffsetInMethod()));
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each ldc instruction.
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitLdcInsn(LdcInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		
		Object constant = insn.cst;
		
		if (constant instanceof Type) {
			fDocument.add(((Type) constant).getDescriptor());
		}
		else {
			
			if (constant instanceof Integer) {
				fDocument.add(STYLE.TYPE_PRIMITIVE, "int");
			}
			else if (constant instanceof Float) {
				fDocument.add(STYLE.TYPE_PRIMITIVE, "float");
			}
			else if (constant instanceof String) {
				fDocument.add(STYLE.TYPE_OBJECT, String.class.getName().replace('.', '/'));
			}
			else if (constant instanceof Long) {
				fDocument.add(STYLE.TYPE_PRIMITIVE, "long");
			}
			else if (constant instanceof Double) {
				fDocument.add(STYLE.TYPE_PRIMITIVE, "double");
			}
			fDocument.add("(");
			if(constant instanceof String) {
				fDocument.add("\"");
			}
			fDocument.add(constant.toString());
			if(constant instanceof String) {
				fDocument.add("\"");
			}
			fDocument.add(")");
		}
		
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each lookupswitch instruction.
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitLookupSwitchInsn(LookupSwitchInsnNode insn, NodeInstruction nodeInstruction) {
		
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.startContext();
			
		List<Integer> key = insn.keys;
		List<LabelNode> labels = insn.labels;
		
		int len = key.size();
		
		for (int i = 0; i < len; i++) {
			fDocument.startLine();
			fDocument.add("[" + key.get(i).toString() + "] : ");
			fDocument.add(Integer.toString(labels.get(i).getLabel().getOffsetInMethod()));
			fDocument.add(fDocument.LINE);
		}
		
		fDocument.startLine();
		
		fDocument.add(STYLE.KEYWORD, "default : ");
		fDocument.add(Integer.toString(insn.dflt.getLabel().getOffsetInMethod()));
		fDocument.add(fDocument.LINE);
		
		fDocument.endContext();
		
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each instruction that represents instruction for method invocation.
	 * <b>Possible instructions:</b>
	 * @see <a href="">invokevirtual</a>
	 * @see <a href="">invokespecial</a>
	 * @see <a href="">invokestatic</a>
	 * @see <a href="">invokeinterface</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitMethodInsn(MethodInsnNode insn, NodeInstruction nodeInstruction) {
		
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each multianewarray instruction.
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitMultiANewArrayInsn(MultiANewArrayInsnNode insn, NodeInstruction nodeInstruction) {
	
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.addType(Type.getType(insn.desc));
		fDocument.add(fDocument.SPACE + insn.dims);
		fDocument.add(fDocument.LINE);
		
		
	}
	
	/**
	 * Called for each tableswitch instruction.
	 * 
	 * @param insn the asm instriction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitTableSwitchInsn(TableSwitchInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.startContext();
			
		int min = insn.min;
		int max = insn.max;
		List<LabelNode> labels = insn.labels;
		
		int len = labels.size();
		
		for (int i = 0; i < len; i++) {
			fDocument.startLine();
			fDocument.add("[" + (min+i) + "] : ");
			fDocument.add(Integer.toString(labels.get(i).getLabel().getOffsetInMethod()));
			fDocument.add(fDocument.LINE);
		}
		
		fDocument.startLine();
		
		fDocument.add(STYLE.KEYWORD, "default : ");
		fDocument.add(Integer.toString(insn.dflt.getLabel().getOffsetInMethod()));
		fDocument.add(fDocument.LINE);
		
		fDocument.endContext();
		
		fDocument.add(fDocument.LINE);
		
		
	}
	
	/**
	 * Called for each instruction that takes a type descriptor as parameter.
	 * <b>Possible instructions:</b>
	 * @see <a href="">new</a>
	 * @see <a href="">anewarray</a>
	 * @see <a href="">checkcast</a>
	 * @see <a href="">instanceof</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitTypeInsn(TypeInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.addType(Type.getObjectType(insn.desc));
		fDocument.add(fDocument.LINE);
		
	}
	
	/**
	 * Called for each instruction that loads or stores the value of a local variable.
	 * <b>Possible instructions:</b>
	 * @see <a href="">iload</a>
	 * @see <a href="">lload</a>
	 * @see <a href="">fload</a>
	 * @see <a href="">dload</a>
	 * @see <a href="">aload</a>
	 * @see <a href="">istore</a>
	 * @see <a href="">lstore</a>
	 * @see <a href="">fstore</a>
	 * @see <a href="">dstore</a>
	 * @see <a href="">astore</a>
	 * @see <a href="">ret</a>
	 * 
	 * @param insn the asm instruction node
	 * @param nodeInstruction the instruction node
	 */
	protected void visitVarInsn(VarInsnNode insn, NodeInstruction nodeInstruction) {
		
		fDocument.startLine();
		fDocument.add(STYLE.INSTRUCTION, Printer.OPCODES[insn.getOpcode()]);
		fDocument.add(fDocument.SPACE);
		fDocument.addToLineMap(nodeInstruction.getStartLine());
		fDocument.add(Integer.toString(insn.var));
		fDocument.add(fDocument.LINE);
		
	}
	
	@Override
	public void visitTryCatchBlocks(List<TryCatchBlockNode> tcbBlocks) {
		
		/*
		fDocument.startLine();
		fDocument.add(KEYWORD, "try ");
		fDocument.startContext();
		*/
	}
	
}
