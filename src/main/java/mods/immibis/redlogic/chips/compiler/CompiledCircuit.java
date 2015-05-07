package mods.immibis.redlogic.chips.compiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.chips.generated.CCOFactory;
import mods.immibis.redlogic.chips.generated.CompiledCircuitObject;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

public class CompiledCircuit implements ICompileContext {
	
	private int[] numInputWires, numOutputWires;
	private int rotation;
	
	public CompiledCircuit(int[] numInputWires, int[] numOutputWires, int rotation) {
		this.numInputWires = numInputWires;
		this.numOutputWires = numOutputWires;
		this.rotation = rotation;
	}
	
	private MethodVisitor methodVisitor;
	@Override
	public MethodVisitor getCodeVisitor() {
		return methodVisitor;
	}
	
	private int nextLocalIndex = 1;
	@Override
	public int createLocal(String desc) {
		if(desc.equals("J") || desc.equals("D")) {
			nextLocalIndex += 2;
			return nextLocalIndex - 2;
		}
		return nextLocalIndex++;
	}
	
	
	private ClassWriter classWriter;
	private String className, classNameInternal, superNameInternal;
	
	
	private void generateConstructor() {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		
		mv.visitCode();
		
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		for(int k = 0; k < 6; k++)
			mv.visitIntInsn(numInputWires[k] < 256 ? Opcodes.BIPUSH : Opcodes.SIPUSH, numInputWires[k]);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, superNameInternal, "_makearray", "(IIIIII)[I");
		for(int k = 0; k < 6; k++)
			mv.visitIntInsn(numOutputWires[k] < 256 ? Opcodes.BIPUSH : Opcodes.SIPUSH, numOutputWires[k]);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, superNameInternal, "_makearray", "(IIIIII)[I");
		mv.visitIntInsn(Opcodes.BIPUSH, rotation);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superNameInternal, "<init>", "([I[II)V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0); // computed
		
		mv.visitEnd();
	}
	
	public void startEmittingCode() {
		if(className != null) throw new AssertionError("already emitting or already finished");
		
		className = CCOFactory.instance.generateClassName();
		classNameInternal = className.replace('.', '/');
		superNameInternal = CompiledCircuitObject.class.getName().replace('.','/');
		
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		classWriter.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, classNameInternal, null, superNameInternal, null);
		methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "_update", "()V", null, null);
		methodVisitor.visitCode();
	}
	public void finishEmittingCode() {
		methodVisitor.visitInsn(Opcodes.RETURN);
		methodVisitor.visitMaxs(0, 0); // computed
		methodVisitor.visitEnd();
		methodVisitor = null;
		
		generateConstructor();
		generateUnpackInputs();
		generatePackOutputs();
		generateUnpackedIOFields();
		generateAddedFields();
		
		classWriter.visitEnd();
		
		byte[] classBytes = classWriter.toByteArray();
		classWriter = null;
		
		if(Boolean.getBoolean("mods.immibis.redlogic.chips.showbytecode"))
			new ClassReader(classBytes).accept(new CheckClassAdapter(new TraceClassVisitor(new PrintWriter(System.out))), 0);
		
		try {
			CCOFactory.instance.registerClass(className, classBytes);
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void loadInput(int dir, int wire) {
		loadField(getUnpackedIOFieldName(dir, wire, false), "Z");
	}
	
	@Override
	public void storeOutput(int dir, int wire) {
		storeField(getUnpackedIOFieldName(dir, wire, true), "Z");
	}
	
	@Override
	public void loadField(String name, String desc) {
		methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		methodVisitor.visitFieldInsn(Opcodes.GETFIELD, classNameInternal, name, desc);
	}
	
	@Override
	public void storeField(String name, String desc) {
		methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		methodVisitor.visitInsn(Opcodes.SWAP);
		methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, classNameInternal, name, desc);
	}
	
	private String getUnpackedIOFieldName(int dir, int wire, boolean output) {
		return (output ? "__uo" : "__ui") + dir + wire;
	}
	
	private void generateUnpackedIOFields() {
		for(int k = 0; k < 6; k++) {
			int in = numInputWires[k];
			int out = numOutputWires[k];
			
			for(int i = 0; i < in; i++) classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT, getUnpackedIOFieldName(k, i, false), "Z", null, null).visitEnd();
			for(int i = 0; i < out; i++) classWriter.visitField(Opcodes.ACC_PRIVATE, getUnpackedIOFieldName(k, i, true), "Z", null, null).visitEnd();
		}
	}
	
	private void generateUnpackInputs() {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PROTECTED, "_unpackInputs", "()V", null, null);
		mv.visitCode();
		
		for(int dir = 0; dir < 6; dir++) {
			int num = numInputWires[dir];
			for(int wire = 0; wire < num; wire++) {
				mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitFieldInsn(Opcodes.GETFIELD, superNameInternal, "_inputs", "[[Z");
						mv.visitIntInsn(Opcodes.BIPUSH, dir);
							mv.visitInsn(Opcodes.AALOAD);
						mv.visitIntInsn(Opcodes.BIPUSH, wire);
							mv.visitInsn(Opcodes.BALOAD);
						mv.visitFieldInsn(Opcodes.PUTFIELD, classNameInternal, getUnpackedIOFieldName(dir, wire, false), "Z");
			}
		}
		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0); // computed
		mv.visitEnd();
	}
	private void generatePackOutputs() {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PROTECTED, "_packOutputs", "()V", null, null);
		mv.visitCode();
		
		for(int dir = 0; dir < 6; dir++) {
			int num = numOutputWires[dir];
			for(int wire = 0; wire < num; wire++) {
				mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitFieldInsn(Opcodes.GETFIELD, superNameInternal, "_outputs", "[[Z");
					mv.visitIntInsn(Opcodes.BIPUSH, dir);
						mv.visitInsn(Opcodes.AALOAD);
					mv.visitIntInsn(Opcodes.BIPUSH, wire);
						mv.visitVarInsn(Opcodes.ALOAD, 0);
							mv.visitFieldInsn(Opcodes.GETFIELD, classNameInternal, getUnpackedIOFieldName(dir, wire, true), "Z");
							mv.visitInsn(Opcodes.BASTORE);
			}
		}
		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0); // computed
		mv.visitEnd();
	}

	public String getClassName() {
		return className;
	}

	@Override
	public String getClassNameInternal() {
		return classNameInternal;
	}

	private class AddedField {
		String name, desc;
	}
	private List<AddedField> addedFields = new ArrayList<AddedField>();
	
	public void createField(String name, String desc) {
		AddedField af = new AddedField();
		af.name=name;
		af.desc=desc;
		addedFields.add(af);
	}
	
	private void generateAddedFields() {
		for(AddedField af : addedFields)
			classWriter.visitField(Opcodes.ACC_PRIVATE, af.name, af.desc, null, null).visitEnd();
	}

	private int nextAllocatedFieldNumber = 0;
	@Override
	public String createField(String desc) {
		String name = "__af" + (nextAllocatedFieldNumber++);
		createField(name, desc);
		return name;
	}
	
	@Override
	public void pushInt(int i) {
		if(i == 0)
			getCodeVisitor().visitInsn(Opcodes.ICONST_0);
		else if(i == 1)
			getCodeVisitor().visitInsn(Opcodes.ICONST_1);
		else if(i == 2)
			getCodeVisitor().visitInsn(Opcodes.ICONST_2);
		else if(i == 3)
			getCodeVisitor().visitInsn(Opcodes.ICONST_3);
		else if(i == 4)
			getCodeVisitor().visitInsn(Opcodes.ICONST_4);
		else if(i == 5)
			getCodeVisitor().visitInsn(Opcodes.ICONST_5);
		else if(i == -1)
			getCodeVisitor().visitInsn(Opcodes.ICONST_M1);
		else if(i == (byte)i)
			getCodeVisitor().visitIntInsn(Opcodes.BIPUSH, i);
		else if(i == (short)i)
			getCodeVisitor().visitIntInsn(Opcodes.SIPUSH, i);
		else
			getCodeVisitor().visitIntInsn(Opcodes.LDC, i);
	}
	
	@Override
	public void detectRisingEdge() {
		String fieldName = createField("Z");
		// DUP			- IN IN
		// GET OLD		- IN IN OLD
		// SWAP			- IN OLD IN
		// PUT OLD		- IN OLD
		// NOT			- IN NOTOLD
		// AND			- POSEDGE
		getCodeVisitor().visitInsn(Opcodes.DUP);
		loadField(fieldName, "Z");
		getCodeVisitor().visitInsn(Opcodes.SWAP);
		storeField(fieldName, "Z");
		getCodeVisitor().visitInsn(Opcodes.ICONST_1);
		getCodeVisitor().visitInsn(Opcodes.IXOR);
		getCodeVisitor().visitInsn(Opcodes.IAND);
	}
	
	@Override
	public String createField(String desc, ICompilableExpression initializer) {
		throw new UnsupportedOperationException();
	}
		
}
