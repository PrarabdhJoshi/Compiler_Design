package cop5556sp17;

import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		
		this.slot_number=1;
		this.slot_num = new HashMap<Integer, Dec>();
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	/* creating a stack */
	Stack<Integer> scope;
	
	public int slot_number;
	//ArrayList<Integer> slot_num;
	public HashMap<Integer, Dec> slot_num;
	MethodVisitor mv; 

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);
		ArrayList<ParamDec> params = program.getParams();
		
		
		//new try int paramdecslotnumber =0 , done next
		int param_slot=0;
		for (ParamDec dec : params) {
			String fieldName = dec.getIdent().getText();
			String fieldType = dec.getTypeName().getJVMTypeDesc();
			dec.setSlot(param_slot++);
			FieldVisitor fv = cw.visitField(0, fieldName, fieldType, null, null);
			fv.visitEnd();
		}
		
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				new String[] { "java/net/MalformedURLException" });
		mv.visitCode();
		
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		
		params = program.getParams();
		for (ParamDec dec : params){
			dec.visit(this, mv);
		}
			
		mv.visitInsn(RETURN);
		
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		
		mv.visitMaxs(1, 1);
		
		mv.visitEnd();
	

		
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				new String[] { "java/net/MalformedURLException" });
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, mv);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		for (Dec d : slot_num.values()) {
			mv.visitLocalVariable(d.getIdent().getText(), classDesc, null, startRun, endRun, d.getSlot());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		
		if(assignStatement.getE().getType().equals(TypeName.IMAGE) && assignStatement.getVar().getDec().getTypeName().equals(TypeName.IMAGE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig,false);
		}
		
		assignStatement.getVar().visit(this, arg);
		
		return null;
	}
	

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		/*MethodVisitor mv =  (MethodVisitor) arg;
		binaryChain.getE0().isLeftSide = true;
		binaryChain.getE0().visit(this, arg);*/
		
		//MethodVisitor mv =  (MethodVisitor) arg;
		
		//Token t = binaryChain.getArrow();
		binaryChain.getE0().visit(this, "left");
		if(binaryChain.getE0().getType().equals(TypeName.URL)){
			mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"readFromURL",PLPRuntimeImageIO.readFromURLSig,false);
		}
		else if(binaryChain.getE0().getType().equals(TypeName.FILE)){
			 mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"readFromFile",PLPRuntimeImageIO.readFromFileDesc,false);
		}
		binaryChain.getE1().visit(this, "right");
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		Expression e0=binaryExpression.getE0();
		e0.visit(this, arg);
		Expression e1=binaryExpression.getE1();
		e1.visit(this, arg);
		//MethodVisitor mv = (MethodVisitor) arg;
		Token token = binaryExpression.getOp();
		Label label = new Label();
		Label label2 = new Label();
		switch(token.kind){
		case PLUS:
			if (binaryExpression.getE0().getType().equals(TypeName.IMAGE) && binaryExpression.getE1().getType().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			} else {
				mv.visitInsn(IADD);
			}
			break;
		case MINUS:
			if (binaryExpression.getE0().getType().equals(TypeName.IMAGE) && binaryExpression.getE1().getType().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}else{
				mv.visitInsn(ISUB);
			}
		    break;
		case TIMES:
			if (binaryExpression.getE0().getType().equals(TypeName.IMAGE) || binaryExpression.getE1().getType().equals(TypeName.IMAGE)) {
				if(binaryExpression.getE1().getType().equals(TypeName.IMAGE)){
					mv.visitInsn(SWAP);
				}
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}else{
				mv.visitInsn(IMUL);
			}
			break;
		case DIV:
			if (binaryExpression.getE0().getType().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			}else{
				mv.visitInsn(IDIV);
			}
			break;
		//case MOD:
			//mv.visitInsn(IREM);
		case AND:
			mv.visitInsn(IAND);
			break;
		case MOD:
			if (binaryExpression.getE0().getType().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			}else{
				mv.visitInsn(IREM);
			}
			break;
		case LT:
			mv.visitJumpInsn(IF_ICMPLT, label);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(label);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(label2);
			//if true, load mv.visitInsn(ICONST_1); 
			break;
		case GT:
			mv.visitJumpInsn(IF_ICMPGT, label);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(label);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(label2);
			break;
		case LE:
			mv.visitJumpInsn(IF_ICMPLE, label);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(label);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(label2);
			break;
		case GE:
			mv.visitJumpInsn(IF_ICMPGE, label);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(label);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(label2);
			break;
		case NOTEQUAL:
			mv.visitJumpInsn(IF_ICMPNE, label);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(label);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(label2);
			break;
		case EQUAL:
			mv.visitJumpInsn(IF_ICMPEQ, label);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(label);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(label2);
			break;
		case OR:
			mv.visitInsn(IOR);
		}
		return null;
	}

	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		//MethodVisitor mv = (MethodVisitor) arg; UC
		//code to enter the scope
		//enterscope();
		
			for(Dec d : block.getDecs()){
				d.visit(this, arg);
			
		}
			for(Statement statement : block.getStatements()){
				statement.visit(this, arg);
				
				if(statement instanceof BinaryChain && !((BinaryChain)statement).getType().equals(TypeName.INTEGER)){
					mv.visitInsn(POP);
				}
			}
		
		
		
		//code to leave the scope
		//exitscope();
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		/*
		if(booleanLitExpression.getValue().equals(true)){
			mv.visitInsn(ICONST_1);
			mv.visitVarInsn(ISTORE, cur_slot);
		} 
		else if(booleanLitExpression.getValue().equals(false)){
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, cur_slot);
		}
		*/
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitLdcInsn(booleanLitExpression.getValue());	
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		//assert false : "not yet implemented";
		
		//MethodVisitor mv = (MethodVisitor) arg; 
		if (constantExpression.firstToken.kind.equals(Kind.KW_SCREENWIDTH)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		} else if (constantExpression.firstToken.kind.equals(Kind.KW_SCREENHEIGHT)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}
		
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.setSlot(this.slot_number);
		slot_num.put(this.slot_number, declaration);
		this.slot_number++;
		//MethodVisitor mv = (MethodVisitor)arg; UC
		switch(declaration.getTypeName()){
		case FRAME:
		case IMAGE:
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
			break;
			
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		filterOpChain.getArg().visit(this, arg);
		//MethodVisitor mv = (MethodVisitor) arg; -- commented now
		mv.visitInsn(ACONST_NULL);
		switch(filterOpChain.firstToken.kind.name()){
		case "OP_BLUR": mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);break;
		case "OP_GRAY": mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);break;
		case "OP_CONVOLVE": mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		break;
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		//BufferedImage image1=null;
	    //PLPRuntimeFrame.createOrSetFrame(,null);
		frameOpChain.getArg().visit(this, arg);
		//MethodVisitor mv = (MethodVisitor)arg; -- commented now	
		switch(frameOpChain.firstToken.kind.name()){
		case "KW_XLOC":mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		break;
		case "KW_YLOC":mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		break;
		case "KW_HIDE":mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		break;
		case "KW_SHOW":mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		break;
		case "KW_MOVE":mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		break;
	
		}
		return null;

	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		Dec dec = identChain.getDec();
		//System.out.println(dec);
		//int slot = dec.getSlot();
		arg = (String)arg;
		if(arg=="left"){
		if(identChain.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD,0);
            mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),identChain.getDec().getTypeName().getJVMTypeDesc());
		}
		else if(identChain.getDec().getTypeName().equals(IMAGE) || identChain.getDec().getTypeName().equals(FRAME)){
			 mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
		}
		else {
            mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
         }
		}
		else if(arg=="right"){
			if(identChain.getDec().getTypeName().equals(TypeName.INTEGER)){
				mv.visitInsn(DUP);
				if(identChain.getDec() instanceof ParamDec){
					mv.visitVarInsn(ALOAD,0);
		        	 mv.visitInsn(SWAP);
		            mv.visitFieldInsn(PUTFIELD, className,identChain.getFirstToken().getText(),identChain.getDec().getTypeName().getJVMTypeDesc());
				}
				else{
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
				}
			}
			
			
			else if (identChain.getDec().getTypeName().equals(TypeName.IMAGE)){
	            mv.visitInsn(DUP);
	            mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());

	         }
			else if(identChain.getDec().getTypeName().equals(TypeName.FRAME)){
				mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
			}else if(identChain.getDec().getTypeName().equals(TypeName.FILE)){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),identChain.getDec().getTypeName().getJVMTypeDesc());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
			}
			else{
				//mv.visitInsn(DUP);
	            mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
	         }
		}
		
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		//MethodVisitor mv = (MethodVisitor) arg;
		Dec d=identExpression.dec;
		
		if(d instanceof ParamDec){
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), d.getTypeName().getJVMTypeDesc());
		}
		else{
			if(identExpression.dec.getTypeName().equals(TypeName.INTEGER) || identExpression.dec.getTypeName().equals(TypeName.BOOLEAN))
				mv.visitVarInsn(ILOAD, d.getSlot());
			else 
				mv.visitVarInsn(ALOAD, d.getSlot());
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		//mv.visitLocalVariable(identX.get, desc, signature, start, end, index);
		
		Dec d = identX.getDec();
		MethodVisitor mv = (MethodVisitor) arg;
		//mv.visitVarInsn(ALOAD, 0);
		if(d instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, d.getIdent().getText(), d.getTypeName().getJVMTypeDesc());
			}
		else{
			
			if (identX.getDec().getTypeName().equals(TypeName.INTEGER) || identX.getDec().getTypeName().equals(TypeName.BOOLEAN)) {
				mv.visitVarInsn(ISTORE, d.getSlot());
			} else {
				mv.visitVarInsn(ASTORE, d.getSlot());
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TO Implement this
		ifStatement.getE().visit(this, arg);
		MethodVisitor mv = (MethodVisitor) arg;
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(l1);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		imageOpChain.getArg().visit(this, arg);
		switch(imageOpChain.firstToken.kind.name()){
		case "KW_SCALE":mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		break;
		case "OP_WIDTH":mv.visitMethodInsn(INVOKESTATIC, "Ljava/awt/image/BufferedImage", "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		break;
		case "OP_HEIGHT":mv.visitMethodInsn(INVOKESTATIC, "Ljava/awt/image/BufferedImage", "getHeight", PLPRuntimeImageOps.getHeightSig, false);
		break;
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		//MethodVisitor mv = (MethodVisitor) arg;
		//mv.visitIntInsn(SIPUSH, intLitExpression.value);
		mv.visitLdcInsn(new Integer(intLitExpression.value));
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		
		
		//-------new additions------------
		
		//PLPRuntimeImageIO.getURL(paramDec.getFirstToken().getText(), paramDec.getSlot());
		//java.io.File.File(" ");
		//mv.visitTypeInsn(NEW, "java/io/File");

		//mv.visitInsn(DUP);

		//mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
		
		//////////This is old
		MethodVisitor mv = (MethodVisitor) arg;
		String type = null;
		//mvvisit(aaload)
		mv.visitVarInsn(ALOAD, 0);
		switch(paramDec.getTypeName().getJVMTypeDesc()){
		case "Ljava/io/File;":
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			break;
		}
		
		mv.visitVarInsn(ALOAD, 1);

	
		switch(paramDec.getSlot()){
		case 0:
			mv.visitInsn(ICONST_0);
			break;
		case 1:
			mv.visitInsn(ICONST_1);
			break;
		case 2:
			mv.visitInsn(ICONST_2);
			break;
		case 3:
			mv.visitInsn(ICONST_3);
			break;
		case 4:
			mv.visitInsn(ICONST_4);
			break;
		case 5:
			mv.visitInsn(ICONST_5);
			break;
		default:
			mv.visitIntInsn(SIPUSH, paramDec.getSlot());
		}
		
		switch(paramDec.getTypeName().getJVMTypeDesc()){
		case "I":
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			
			break;
		case "Z":
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			break;
		case "Ljava/net/URL;":
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			break;
		case "Ljava/io/File;":
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			break;
		}
		
		
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		//assert false : "not yet implemented";
		sleepStatement.getE().visit(this, arg);
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		//assert false : "not yet implemented";
		for (Expression e : tuple.getExprList()) {
			e.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		MethodVisitor mv = (MethodVisitor) arg;
		Label l3 = new Label();
		mv.visitJumpInsn(GOTO, l3);
		Label l4 = new Label();
		mv.visitLabel(l4);
		whileStatement.getB().visit(this,arg);
		mv.visitLabel(l3);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, l4);
		return null;
	}
	
	
}

	