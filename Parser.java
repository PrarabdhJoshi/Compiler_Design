package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p=null;
		p=program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException {
		//expression ∷= term ( relOp term)*
		//Kind kind= t.kind;
		Expression e0=null;
		Expression e1=null;
		e0=term();				
		while(relop()){	//under the hood (kind==LT || kind==LE || kind==GT || kind==EQUAL || kind==NOTEQUAL)
			Token op=t;
			consume();
			e1=term();
			e0=new BinaryExpression(e0.firstToken,e0,op,e1);
		}
		
		//throw new UnimplementedFeatureException();
		// should return the thing returned by term
		return e0;
	}

	Expression term() throws SyntaxException { //term ∷= elem ( weakOp  elem)*
		Expression e0=null;
		Expression e1=null;
		e0=elem();
		//Kind kind=t.kind;
		while(weakop()){				//under the hood kind==PLUS || kind==MINUS || kind==OR
			Token op= t;
			consume();
			e1=elem();
			e0=new BinaryExpression(e0.firstToken,e0,op,e1);
		}
		//throw new UnimplementedFeatureException();
		//should return a thing returned by elem
		return e0;
	}

	Expression elem() throws SyntaxException {
		//elem ∷= factor ( strongOp factor)*
		Expression e0=null;
		Expression e1=null;
		e0=factor();
		//Kind kind=t.kind;
		while(strongop()){					// under the hood kind==TIMES || kind==DIV || kind==AND || kind==MOD 
			Token op = t;
			consume();
			
			e1=factor();
			e0=new BinaryExpression(e0.firstToken,e0,op,e1);
		}
		//throw new UnimplementedFeatureException();
		//should return a thing returned by factor
		return e0;
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Expression e=null;
		switch (kind) {
		case IDENT: {
			//save token
			e=new IdentExpression(t);
			consume();
			//return new IdentExpression(t);
			
		}
			break;
		case INT_LIT: {	
			e=new IntLitExpression(t);
			consume();
			//return new IntLitExpression(t);
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e=new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e=new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e=expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		/*
		 * if(case matched was int_lit)
		 * 	this should return an instance of int_litexpression
		 * else if(case matched was ident)
		 * 	factor should return an instance of identexpression
		 * else if(case matched was true or false)
		 *  factor should return boolean_lit expression
		 * else if(case matched was screen)
		 * 	factor should return constant expression
		 * else
		 * 	factor should return binaryexpression
		 * 
		 */
		return e;
	}

	Block block() throws SyntaxException {
		//block ::= { ( dec | statement) * }
		Kind kind=t.kind;
		ArrayList<Dec> decList=new ArrayList<Dec>();
		ArrayList<Statement> statementList=new ArrayList<Statement>();
		Token op;
		switch(kind){
		case LBRACE:{
			op=t;
			consume();
			
			while(t.kind==KW_INTEGER || t.kind==KW_BOOLEAN || t.kind==KW_IMAGE ||t.kind==KW_FRAME || t.kind==OP_SLEEP || t.kind==KW_WHILE || t.kind==KW_IF || t.kind==IDENT || filterop() || frameop() || imageop()){
				if(t.kind==KW_INTEGER || t.kind==KW_BOOLEAN || t.kind==KW_IMAGE ||t.kind==KW_FRAME){
					decList.add(dec());
			}
				else if(t.kind==OP_SLEEP || t.kind==KW_WHILE || t.kind==KW_IF || t.kind==IDENT || filterop() || frameop() || imageop() )
				{
						statementList.add(statement());
				}
			}
			
			
			/*
			else{
				throw new SyntaxException("error in block after LBrace");
			}
			*/
			match(RBRACE);
		}	break;
		default:{
			throw new SyntaxException("Expected Left Brace in block()");
		}
		}
		//throw new UnimplementedFeatureException();
		return new Block(op,decList,statementList);
	}

	Program program() throws SyntaxException {
		//program ::=  IDENT block 
		//program ::=  IDENT param_dec ( , param_dec )*   block
		Token op;
		Kind kind=t.kind;
		Block b=null;
		Program p=null;
		ArrayList<ParamDec> params=new ArrayList<ParamDec>();
		ParamDec pd;
		if(kind==IDENT){
			op=t;
			consume();
			if(t.kind==LBRACE)
				b=block();
			else if(t.kind==KW_URL || t.kind==KW_FILE || t.kind==KW_INTEGER || t.kind==KW_BOOLEAN){
				pd=paramDec();
				params.add(pd);
				while(t.kind==COMMA){
					consume();
					 if(t.kind==KW_URL || t.kind==KW_FILE || t.kind==KW_INTEGER || t.kind==KW_BOOLEAN){
						 pd=paramDec();
					 	params.add(pd);
					 }
					 else 
						 throw new SyntaxException("expected KW after , in program");
				}
				b=block();
			}
				
		}
		else{
			throw new SyntaxException("program must start with an identifier");
		}
		
		//throw new UnimplementedFeatureException();
		p=new Program(op,params,b);
		return p;
	}

	ParamDec paramDec() throws SyntaxException {
		//paramDec ::= ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN)   IDENT
		Token op;
		Token id;
		Kind kind=t.kind;
		ParamDec p;
		if(kind==KW_URL || kind==KW_FILE || kind==KW_INTEGER ||kind==KW_BOOLEAN){
			op=t;
			consume();
			id=match(IDENT);
		}
		//throw new UnimplementedFeatureException();
		else
			throw new SyntaxException("expected KW in paramDec");
		p=new ParamDec(op,id);
		return p;
	}

	Dec dec() throws SyntaxException {
		//(  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME)    IDENT
		Token op;
		Token id;
		Kind kind=t.kind;
		if(kind==KW_INTEGER || kind==KW_BOOLEAN || kind==KW_IMAGE ||kind==KW_FRAME){
			op=t;
			consume();
			id=match(IDENT);
		}
		//throw new UnimplementedFeatureException();
		else
			throw new SyntaxException("missing KW in dec");
		return new Dec(op,id);
	}

	Statement statement() throws SyntaxException {
		//statement ::=   OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
		//Statement ∷= SleepStatement | WhileStatement | IfStatement | Chain | AssignmentStatement
		Statement s;
		Expression e;
		Token first=t;
		Kind kind=t.kind;
		
		switch(kind){
		case OP_SLEEP:{
			
			consume();
			e=expression();
			s=new SleepStatement(first,e);
			match(SEMI);
		}break;
		case KW_WHILE:{
			s=whilestatement();
			
		}break;
		case KW_IF:{
			s=ifstatement();
		}break;
		case IDENT:{
			
			if(scanner.peek().kind==ASSIGN){
				s=assign();
				match(SEMI);
			}
			else {				//previous: elif(scanner.peek().kind==ARROW || scanner.peek().kind==BARARROW)
				s=chain();
				match(SEMI);
			}
		}break;
		
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE:
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:{
			s=chain();
			match(SEMI);
			
		}break;
		default:{
			throw new SyntaxException("Statement has Illegal token ="+t.getText());
		}
			
		}
		//throw new UnimplementedFeatureException();
		return s;
	}

	Chain chain() throws SyntaxException {
		//chain ::=  chainElem arrowOp chainElem ( arrowOp  chainElem)*
		Chain e0=null;
		Token first = t;
		
		ChainElem e1=null;
		//Token op;
		Token arrow;
		e0=chainElem();
		//ce=chain();
		if(arrowop()){
			arrow = t;
			consume();
			e1=chainElem();
			e0=new BinaryChain(first,e0,arrow,e1);
			while(arrowop()){
				arrow=t;
				consume();
				e1=chainElem();
				e0=new BinaryChain(first,e0,arrow,e1);
			}
		}
		else{
			throw new SyntaxException("Arrow or bararrow missing in chain");
		}
		
		//return new Chain(op);
		return e0;
		
		//throw new UnimplementedFeatureException();
	}

	ChainElem chainElem() throws SyntaxException {
		//chainElem ::= IDENT | filterOp arg | frameOp arg | imageOp arg
		Tuple a=null;
		ChainElem c=null;
		Token first;
		if(t.kind==IDENT){
			first=t;
			consume();
			c=new IdentChain(first);
		}
			
		else if(filterop() || frameop() || imageop() ){
			first=t;
			if(filterop()){
				consume();
				a=arg();
				c=new FilterOpChain(first,a);
			}
				
			else if(frameop()){
				consume();
				a=arg();
				c=new FrameOpChain(first,a);
			}
				
			else if(imageop()){
				consume();
				a=arg();
				c=new ImageOpChain(first,a);
			}
				
		}
		else{
			throw new SyntaxException("Illegal Chain Element");
		}
		//throw new UnimplementedFeatureException();
		
		return c;
	}

	Tuple arg() throws SyntaxException {
		// ε | ( expression (   ,expression)* )
		List<Expression> exprList=new ArrayList<Expression>();
		//exprList=null;
		Expression e=null;
		Tuple tuple=null;
		Token first =t;
		
		Kind kind=t.kind;
		switch(kind){
		
		case LPAREN:{
			consume();
			e=expression();
			exprList.add(e);
			if(t.kind==COMMA){
				while(t.kind==COMMA){
					consume();
					e=expression();
					exprList.add(e);
				}
			}
			//tuple=new Tuple(first,exprList);
			match(RPAREN);
			
		}break;
		
		}
		
		tuple = new Tuple(first, exprList);
		return tuple;
	}
	
	AssignmentStatement assign() throws SyntaxException{
		//assign ::= IDENT ASSIGN expression
		Token first=t;
		Expression e=null;
		AssignmentStatement a;
		IdentLValue i=new IdentLValue(first);
		consume();
		if(t.kind==ASSIGN){
			consume();
			e=expression();
			
		}
		else{
			throw new SyntaxException("Illegal assignment");
		}
		a=new AssignmentStatement(first,i,e);
		return a;
	}
	WhileStatement whilestatement() throws SyntaxException{
		Expression e;
		Block b;
		WhileStatement w;
		Token first =t;
		if(t.kind==KW_WHILE){   // while (true) \n {x -> show |-> move (x,y) ;} 
			consume();
			if(t.kind==LPAREN){
				consume();
				e=expression();
				match(RPAREN);
			}
			else{
				throw new SyntaxException("Expected ( after while");
			}
			b=block();
		}
		else{
			throw new SyntaxException("Missing Keyword while");
		}
		w=new WhileStatement(first,e,b);
		return w;
	}
	
	IfStatement ifstatement() throws SyntaxException{
		Expression e;
		Block b;
		IfStatement s;
		Token first=t;
		if(t.kind==KW_IF){
			consume();
			if(t.kind==LPAREN){
				consume();
				e=expression();
				match(RPAREN);
			}
			else{
				throw new SyntaxException("Expected ( after if");
			}
			b=block();
		}
		else{
			throw new SyntaxException("Missing Keyword if");
		}
		s=new IfStatement(first, e, b);
		return s;
	}
	boolean arrowop() throws SyntaxException{
		if(t.kind==ARROW || t.kind==BARARROW)
			return true;
		else
			return false;
	}
	boolean filterop() throws SyntaxException{
		if(t.kind==OP_BLUR || t.kind==OP_GRAY || t.kind==OP_CONVOLVE){
			return true;
		}
		else
			return false;
		
	}
	
	boolean frameop() throws SyntaxException{
		if(t.kind==KW_SHOW || t.kind==KW_HIDE || t.kind==KW_MOVE || t.kind==KW_XLOC || t.kind==KW_YLOC){
			return true;
		}
		else
			return false;
		
	}
	
	boolean imageop() throws SyntaxException{
		if(t.kind==OP_WIDTH || t.kind==OP_HEIGHT || t.kind==KW_SCALE){
			return true;
		}
		else
			return false;
		
	}
	boolean relop() throws SyntaxException{
		if(t.kind==LT || t.kind==LE || t.kind==GT || t.kind==GE || t.kind==EQUAL || t.kind==NOTEQUAL ){
			return true;
		}
		else
			return false;
		
	}
	
	boolean weakop() throws SyntaxException{
		if(t.kind==PLUS || t.kind==MINUS || t.kind==OR ){
			return true;
		}
		else
			return false;
		
	}
	
	boolean strongop() throws SyntaxException{
		if(t.kind==TIMES || t.kind==DIV || t.kind==AND || t.kind==MOD){
			return true;
		}
		else
			return false;
		
	}
	
	
	

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind==EOF) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind==kind) {												//t.kind == to existing token, kind= expected token.. like )
			return consume();
		}
		else
			throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}
	

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}
	

}
