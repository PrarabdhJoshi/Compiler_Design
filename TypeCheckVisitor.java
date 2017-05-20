package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
				

		Chain chain = binaryChain.getE0();
		ChainElem chainelem = binaryChain.getE1();
		Token token = binaryChain.getArrow();
		
		chain.visit(this, arg);
		chainelem.visit(this, arg);
		
		if(chain.getType().equals(TypeName.URL) && token.kind.equals(Kind.ARROW) && chainelem.getType().equals(TypeName.IMAGE)){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain.getType().equals(TypeName.FILE) && token.kind.equals(Kind.ARROW) && chainelem.getType().equals(TypeName.IMAGE)){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain.getType().equals(TypeName.FRAME) && token.kind.equals(Kind.ARROW) && chainelem.getClass().equals(FrameOpChain.class) && (chainelem.getFirstToken().kind.equals(Kind.KW_XLOC) || chainelem.getFirstToken().kind.equals(Kind.KW_YLOC))){
			binaryChain.setType(TypeName.INTEGER);
		}else if(chain.getType().equals(TypeName.FRAME) && token.kind.equals(Kind.ARROW) && chainelem.getClass().equals(FrameOpChain.class) && (chainelem.getFirstToken().kind.equals(Kind.KW_SHOW) || chainelem.getFirstToken().kind.equals(Kind.KW_HIDE)|| chainelem.getFirstToken().kind.equals(Kind.KW_MOVE))){
			binaryChain.setType(TypeName.FRAME);
		}else if(chain.getType().equals(TypeName.IMAGE) && token.kind.equals(Kind.ARROW) && chainelem.getClass().equals(ImageOpChain.class) && (chainelem.getFirstToken().kind.equals(Kind.OP_WIDTH) || chainelem.getFirstToken().kind.equals(Kind.OP_HEIGHT))){
			binaryChain.setType(TypeName.INTEGER);
		}else if(chain.getType().equals(TypeName.IMAGE) && token.kind.equals(Kind.ARROW) && chainelem.getType().equals(TypeName.FRAME)){
			binaryChain.setType(TypeName.FRAME);
		}else if(chain.getType().equals(TypeName.IMAGE) && token.kind.equals(Kind.ARROW) && chainelem.getType().equals(TypeName.FILE)){
			binaryChain.setType(TypeName.NONE);
		}else if(chain.getType().equals(TypeName.IMAGE) && (token.kind.equals(Kind.ARROW) || token.kind.equals(Kind.BARARROW)) && chainelem.getClass().equals(FilterOpChain.class) && (chainelem.getFirstToken().kind.equals(Kind.OP_GRAY) || chainelem.getFirstToken().kind.equals(Kind.OP_BLUR)|| chainelem.getFirstToken().kind.equals(Kind.OP_CONVOLVE))){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain.getType().equals(TypeName.IMAGE) && token.kind.equals(Kind.ARROW) && chainelem.getClass().equals(ImageOpChain.class) && (chainelem.getFirstToken().kind.equals(Kind.KW_SCALE))){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain.getType().equals(TypeName.IMAGE) && token.kind.equals(Kind.ARROW) && chainelem.getClass().equals(IdentChain.class) && chainelem.getType().equals(TypeName.IMAGE)){
			binaryChain.setType(TypeName.IMAGE);
		}
		else if(chain.getType().equals(TypeName.INTEGER) && token.kind.equals(Kind.ARROW) && chainelem.getClass().equals(IdentChain.class) && chainelem.getType().equals(TypeName.INTEGER)){
			binaryChain.setType(TypeName.INTEGER);
		}
		else{
			throw new TypeCheckException("Error in visit Binary Chain");
		}
		return null;
		
	}
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression expression1 = binaryExpression.getE0();
		Expression expression2 = binaryExpression.getE1();
		Token token = binaryExpression.getOp();
		
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		

		if(expression1.getType().equals(TypeName.INTEGER) && (token.kind.equals(Kind.PLUS) || token.kind.equals(Kind.MINUS)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.INTEGER);
		}else if(expression1.getType().equals(TypeName.IMAGE) && (token.kind.equals(Kind.PLUS) || token.kind.equals(Kind.MINUS)) && expression2.getType().equals(TypeName.IMAGE)){
			binaryExpression.setType(TypeName.IMAGE);
		}else if(expression1.getType().equals(TypeName.INTEGER) && (token.kind.equals(Kind.TIMES) || token.kind.equals(Kind.DIV)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.INTEGER);
		}else if(expression1.getType().equals(TypeName.INTEGER) && (token.kind.equals(Kind.TIMES)) && expression2.getType().equals(TypeName.IMAGE)){
			binaryExpression.setType(TypeName.IMAGE);
		}else if(expression1.getType().equals(TypeName.IMAGE) && (token.kind.equals(Kind.TIMES)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.IMAGE);
		}else if(expression1.getType().equals(TypeName.INTEGER) && (token.kind.equals(Kind.LT) || token.kind.equals(Kind.GT) || token.kind.equals(Kind.LE) || token.kind.equals(Kind.GE)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.BOOLEAN);
		}else if(expression1.getType().equals(TypeName.BOOLEAN) && (token.kind.equals(Kind.LT) || token.kind.equals(Kind.GT) || token.kind.equals(Kind.LE) || token.kind.equals(Kind.GE)) && expression2.getType().equals(TypeName.BOOLEAN)){
			binaryExpression.setType(TypeName.BOOLEAN);
		}else if((token.kind.equals(Kind.EQUAL) || token.kind.equals(Kind.NOTEQUAL)) && expression1.getType() == expression2.getType()){
			binaryExpression.setType(TypeName.BOOLEAN);
		}
		//NEW CALLS
		else if(expression1.getType().equals(TypeName.BOOLEAN) && (token.kind.equals(Kind.AND)) && expression2.getType().equals(TypeName.BOOLEAN)){
			binaryExpression.setType(TypeName.BOOLEAN);
		}else if((expression1.getType().equals(TypeName.BOOLEAN) ||  expression2.getType().equals(TypeName.BOOLEAN)) && (token.kind.equals(Kind.OR)) ){
			binaryExpression.setType(TypeName.BOOLEAN);
		}else if(expression1.getType().equals(TypeName.INTEGER) &&  expression2.getType().equals(TypeName.INTEGER) && (token.kind.equals(Kind.MOD)) ){
			binaryExpression.setType(TypeName.INTEGER);
		}else if(expression1.getType().equals(TypeName.IMAGE) &&  expression2.getType().equals(TypeName.INTEGER) && ((token.kind.equals(Kind.MOD))||(token.kind.equals(Kind.TIMES))||(token.kind.equals(Kind.DIV)))){
			binaryExpression.setType(TypeName.IMAGE);
		}
		
		
		else{
			throw new TypeCheckException("Error in Binary Expression");
		}
		return null;
		}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println(x);
		symtab.enterScope();
		ArrayList<Dec> dec = block.getDecs();
		ArrayList<Statement> st = block.getStatements();
		
		if(dec != null){
			for(Dec d : dec){
				d.visit(this, arg);
			}
		}
		if(st != null){
			for(Statement s: st){
			/*	if(s.getClass().equals(SleepStatement.class)){
					visitSleepStatement((SleepStatement)s,arg);
				}
				else if(s.getClass().equals(IfStatement.class)){
					visitIfStatement((IfStatement)s, arg);
				}
				else if(s.getClass().equals(WhileStatement.class)){
					visitWhileStatement((WhileStatement)s, arg);
				}
				
				else if(s.getClass().equals(AssignmentStatement.class)){
					
					visitAssignmentStatement((AssignmentStatement)s, arg);
				}
				
				else if(s.getClass().equals(Chain.class)){
					Chain chain = (Chain) s;
					if(chain.getClass().equals(BinaryChain.class)){
						visitBinaryChain((BinaryChain)chain, arg);
					}
					else if(chain.getClass().equals(IdentChain.class)){
						visitIdentChain((IdentChain)chain, arg);
					}
					else if(chain.getClass().equals(FilterOpChain.class)){
						visitFilterOpChain((FilterOpChain)chain, arg);
					}
					else if(chain.getClass().equals(FrameOpChain.class)){
						visitFrameOpChain((FrameOpChain)chain, arg);
					}
					else if(chain.getClass().equals(ImageOpChain.class)){
						visitImageOpChain((ImageOpChain)chain, arg);
					}
				}*/
				
				s.visit(this, arg);	
			}
		}
		
		symtab.leaveScope();
		
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setType(TypeName.BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tuple = filterOpChain.getArg();
		tuple.visit(this, arg);
		if(tuple.getExprList().size()==0){
			filterOpChain.setType(TypeName.IMAGE);
			//tuple.visit(this, arg);
		}
		else {
			throw new TypeCheckException("Invalid filterop");
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Tuple tuple = frameOpChain.getArg();
		tuple.visit(this, arg);
		if(frameOpChain.getFirstToken().kind==KW_SHOW || frameOpChain.getFirstToken().kind==KW_HIDE){
			if(tuple.getExprList().size()==0){
				frameOpChain.setType(TypeName.NONE);
				//tuple.visit(this, arg);
			}
			else
				throw new TypeCheckException("Invalid filterop");
		}
		else if(frameOpChain.getFirstToken().kind==KW_XLOC || frameOpChain.getFirstToken().kind==KW_YLOC){
			if(tuple.getExprList().size()==0){
				frameOpChain.setType(TypeName.INTEGER);
				//tuple.visit(this, arg);
			}
			else{
				throw new TypeCheckException("Invalid filterop");
			}
		}
		else if(frameOpChain.getFirstToken().kind==KW_MOVE){
			if(tuple.getExprList().size()==2){
				frameOpChain.setType(TypeName.NONE);
				//tuple.visit(this, arg);
			}
			else{
				throw new TypeCheckException("Invalid frameop");
			}
			
		}
		else{
			throw new TypeCheckException("Invalid frameop");
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token token = identChain.getFirstToken();
		if(symtab.isvisible(token.getText())!=null){
			//identChain.setType(symtab.lookup(token.getText()).getTypeName());
			identChain.setType(symtab.isvisible(token.getText()).getTypeName());
			identChain.setDec(symtab.isvisible(token.getText()));
			//identChain.visit(this, arg);
		}
		else{
			throw new TypeCheckException("variable not visible");
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("Now in ident expression");
		Token token = identExpression.getFirstToken();
		if(symtab.isvisible(token.getText())!=null){
			//identExpression.dec=symtab.lookup(token.getText());
			identExpression.dec=symtab.isvisible(token.getText());
			//identExpression.setType(symtab.lookup(token.getText()).getTypeName());
			identExpression.setType(Type.getTypeName(identExpression.dec.getType()));
			//System.out.println(identExpression.getType());
			//identExpression.dec = symtab.lookup(token.getText());
		}
		else
			throw new TypeCheckException("visiblity not set for identexpression");
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = ifStatement.getE();
		Block b = ifStatement.getB();
		e.visit(this, arg);
		if(e.getType()!=BOOLEAN){
			throw new TypeCheckException("If must have a boolean expression");
		}
		
		b.visit(this, arg);
		
		
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setType(TypeName.INTEGER);
		//intLitExpression.setType(intLitExpression.getType());
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("now in sleep statement");
		Expression e = sleepStatement.getE();
		e.visit(this, arg);
		if(e.getType()!=INTEGER){
			throw new TypeCheckException("Sleep must have a integer expression");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = whileStatement.getE();
		Block b = whileStatement.getB();
		e.visit(this, arg);
		if(e.getType()!=BOOLEAN){
			throw new TypeCheckException("While must have a boolean expression");
		}
		
		b.visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		declaration.setType(Type.getTypeName(declaration.getFirstToken()));
		Token tp = declaration.getType();
		Token ident = declaration.getIdent();
		
		Dec dec = null;
		dec = symtab.lookup(ident.getText());
		if(dec==null)
			symtab.insert(ident.getText(), declaration);
		
		else 
			throw new TypeCheckException("ident already defined in the scope");
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<ParamDec> Params = program.getParams();
		Block b = program.getB();
		Object object=null;
		if(Params != null){
			for(ParamDec param : Params){
				object = param.visit(this, arg);
			}
		}
		
		if(b!=null){
			object = b.visit(this,arg);
		}
		
		return object;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		IdentLValue I=assignStatement.getVar();
		Expression e = assignStatement.getE();
		//System.out.println(assignStatement.getVar());
		I.visit(this, arg);
		
		//System.out.println(e.getType());
		//System.out.println(e.toString());
		
		e.visit(this, arg);
		if(e.getType()!=I.getType()){
			throw new TypeCheckException("type mismatch for IdentLValue and expression");
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token token = identX.getFirstToken();
		String ident = identX.getText();
		//Dec de=null;
		if(symtab.isvisible(token.getText())!=null){
			//identX.setType(symtab.lookup(token.getText()).getTypeName());
			//identX.setDec(symtab.lookup(token.getText()));
			identX.setDec(symtab.isvisible(token.getText()));
			//System.out.println(identX.getType());
			identX.setType(Type.getTypeName(identX.getDec().getType()));
			
			//System.out.println(identX.getType());
			//identChain.visit(this, arg);
		}
		else
			throw new TypeCheckException("visiblity not set for identexpression");
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token tp = paramDec.getType();
		Token ident = paramDec.getIdent();
		paramDec.setSlot(-1);
		paramDec.setType(Type.getTypeName(paramDec.firstToken));
		Dec d = null;
		d = symtab.lookup(ident.getText());
		if(d==null)
			symtab.insert(ident.getText(), paramDec);
		else 
			throw new TypeCheckException("ident already defined");
		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setType(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tuple = imageOpChain.getArg();
		tuple.visit(this, arg);
		if(imageOpChain.getFirstToken().kind==OP_WIDTH || imageOpChain.getFirstToken().kind==OP_HEIGHT){
			if(tuple.getExprList().size()==0){
				imageOpChain.setType(TypeName.INTEGER);
				//tuple.visit(this, arg);
			}
			else
				throw new TypeCheckException("Invalid filterop");
		}
		else if(imageOpChain.getFirstToken().kind==KW_SCALE){
			if(tuple.getExprList().size()==1){
				imageOpChain.setType(TypeName.IMAGE);
				//tuple.visit(this, arg);
			}
			else
				throw new TypeCheckException("Invalid filterop");
		}
		else{
			throw new TypeCheckException("Invalid filterop");
		}
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<Expression> expressionlist=(ArrayList<Expression>) tuple.getExprList();
		for(Expression exp:expressionlist){
			exp.visit(this, arg);
			if(!exp.getType().equals(TypeName.INTEGER)){
				throw new TypeCheckException("Exceptioon in tuple");
			}
				
		}
		return null;
	}


}
