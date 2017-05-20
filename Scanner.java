package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
/*
 * token ::= ident  | keyword | frame_op_keyword | filter_op_keyword | image_op_keyword | boolean_literal
 	| int_literal  | separator  | operator
ident ::= ident_start  ident_part*    (but not reserved)
ident_start ::=  A .. Z | a .. z | $ | _
ident_part ::= ident_start | ( 0 .. 9 )
int_literal ::= 0  |  (1..9) (0..9)*
keyword ::= integer | boolean | image | url | file | frame | while | if | sleep | screenheight | screenwidth 
filter_op_keyword ∷= gray | convolve | blur | scale
image_op_keyword ∷= width | height 
frame_op_keyword ∷= xloc | yloc | hide | show | move
boolean_literal ::= true | false
separator ::= 	;  | ,  |  (  |  )  | { | }
operator ::=   	|  | &  |  ==  | !=  | < |  > | <= | >= | +  |  -  |  *   |  /   |  % | !  | -> |  |-> | <-


 */

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;	//name for constant's value, eof for EOF
			//pass "" for text in IDENT
		}

		final String text;	//this dot text in constructor
		//give any object eg IDENT a variable text

		String getText() {
			return text;	//return string for text
		}
	}
	//writing the STATE enum
	static enum State{
		START, DIGITS, AFTER_MINUS, GOTEQUAL,IN_IDENT, AFTER_PIPE, AFTER_NOT, AFTER_LESS, AFTER_GREATER,COMMENT;
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			//implementing
			/*if(kind.getText().length()==0) return chars.substring(pos, length+pos);
			else
				return kind.getText();
			*/
			if(kind==Kind.IDENT || kind==Kind.INT_LIT){
				return chars.substring(pos, pos+length);
			}
			return kind.getText();
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT
			
			
			return lineno.get(this);
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			try{ if(kind != Kind.INT_LIT){
					throw new NumberFormatException("Number format exception occured");
					}
					return(Integer.parseInt(chars.substring(pos,pos+length)));
					
			}
			
			
			
			
			catch(NumberFormatException e){
				
				throw new NumberFormatException("Ecception Occured");
			}
			//return Integer.parseInt(chars.substring(pos,pos+length));
		}
		
		  @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }


		
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		lineno=new HashMap<>();


	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		State state=State.START;
		int startPos=0;
		int pos = 0; 
		int ch;
		int length=chars.length();
		//TODO IMPLEMENT THIS!!!!
		//Implementing
		while(pos<=length){
			ch=pos<length?chars.charAt(pos):-1;
			switch(state){
			case START: {
				pos=skipWhiteSpaces(pos); 
				ch=pos<length?chars.charAt(pos):-1;
				startPos=pos;
				switch(ch){
				case -1:{tokens.add(new Token(Kind.EOF,pos,0));pos++;column++;
						lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '+':{tokens.add(new Token(Kind.PLUS,startPos,1));pos++;column++;
				lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '*':{tokens.add(new Token(Kind.TIMES, startPos, 1)); pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '=':{state = State.GOTEQUAL;pos++;column++;}break;
				case '0':{tokens.add(new Token(Kind.INT_LIT,startPos, 1)); pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case ';':{tokens.add(new Token(Kind.SEMI,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case ',':{tokens.add(new Token(Kind.COMMA,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '(':{tokens.add(new Token(Kind.LPAREN,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case ')':{tokens.add(new Token(Kind.RPAREN,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '{':{tokens.add(new Token(Kind.LBRACE,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '}':{tokens.add(new Token(Kind.RBRACE,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '|':{state = State.AFTER_PIPE;pos++;column++;}break;
				case '&':{tokens.add(new Token(Kind.AND,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1)); } break;
				case '<':{state = State.AFTER_LESS;pos++;column++;}break;
				case '>':{state = State.AFTER_GREATER;pos++;column++;}break;
				case '-':{state = State.AFTER_MINUS;pos++;column++;}break;
				case '/':{state = State.COMMENT;pos++;column++;}break;
				case '%':{tokens.add(new Token(Kind.MOD,startPos,1));pos++;column++;lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));} break;
				case '!':{state = State.AFTER_NOT;pos++;column++;}break;
				
				
				
				default:{
					if(Character.isDigit(ch)){
						state=State.DIGITS; pos++;column++;
					}
					else if(Character.isJavaIdentifierStart(ch)){
						state=State.IN_IDENT;pos++;column++;
					}
					else{
						throw new IllegalCharException(
								"illegal character" +ch+ "encountered at position" +pos);
					}
				}
				
				}
			}break; //case START
			case IN_IDENT:{
				if(Character.isJavaIdentifierPart(ch)){
					pos++;
					column++;
				}else{
					String id=chars.substring(startPos, pos);
					switch(id){
					case "integer": tokens.add(new Token(Kind.KW_INTEGER, startPos, pos-startPos));break;
					case "boolean": tokens.add(new Token(Kind.KW_BOOLEAN, startPos, pos-startPos));break;
					case "url": tokens.add(new Token(Kind.KW_URL, startPos, pos-startPos));break;
					case "image":tokens.add(new Token(Kind.KW_IMAGE, startPos, pos-startPos));break;
					case "file":tokens.add(new Token(Kind.KW_FILE, startPos, pos-startPos));break;
					case "frame":tokens.add(new Token(Kind.KW_FRAME, startPos, pos-startPos));break;
					case "while":tokens.add(new Token(Kind.KW_WHILE, startPos, pos-startPos));break;
					case "if":tokens.add(new Token(Kind.KW_IF, startPos, pos-startPos));break;
					case "sleep":tokens.add(new Token(Kind.OP_SLEEP, startPos, pos-startPos));break;
					case "screenheight":tokens.add(new Token(Kind.KW_SCREENHEIGHT, startPos, pos-startPos));break;
					case "screenwidth":tokens.add(new Token(Kind.KW_SCREENWIDTH, startPos, pos-startPos));break;
					case "gray":tokens.add(new Token(Kind.OP_GRAY, startPos, pos-startPos));break;
					case "convolve":tokens.add(new Token(Kind.OP_CONVOLVE, startPos, pos-startPos));break;
					case "blur":tokens.add(new Token(Kind.OP_BLUR, startPos, pos-startPos));break;
					case "width":tokens.add(new Token(Kind.OP_WIDTH, startPos, pos-startPos));break;
					case "scale":tokens.add(new Token(Kind.KW_SCALE, startPos, pos-startPos));break;
					case "height":tokens.add(new Token(Kind.OP_HEIGHT, startPos, pos-startPos));break;
					case "xloc":tokens.add(new Token(Kind.KW_XLOC, startPos, pos-startPos));break;
					case "yloc":tokens.add(new Token(Kind.KW_YLOC, startPos, pos-startPos));break;
					case "hide":tokens.add(new Token(Kind.KW_HIDE, startPos, pos-startPos));break;
					case "show":tokens.add(new Token(Kind.KW_SHOW, startPos, pos-startPos));break;
					case "move":tokens.add(new Token(Kind.KW_MOVE, startPos, pos-startPos));break;
					case "true":tokens.add(new Token(Kind.KW_TRUE, startPos, pos-startPos));break;
					case "false":tokens.add(new Token(Kind.KW_FALSE, startPos, pos-startPos));break;
					default: tokens.add(new Token(Kind.IDENT, startPos, pos-startPos));
					
					}
				
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					state=State.START;
				}
			}break; //case IN_IDENT
			case DIGITS:{
				if(Character.isDigit(ch)){
					pos++;
					column++;
				}
				else{
					long num=0;
					for(int i=startPos;i<pos;i++){
						num=num*10 + (chars.charAt(i) -'0');
					}
					if(num<Integer.MIN_VALUE || num>Integer.MAX_VALUE){
						throw new IllegalNumberException("Illegal");
					}
					else{
						tokens.add(new Token(Kind.INT_LIT, startPos, pos-startPos));
						lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
						
						
					}
					
					state=State.START;
				}
			}break; //case DIGITS
			
			case GOTEQUAL:{
				if(ch=='='){
					pos++;
					column++;
					tokens.add(new Token(Kind.EQUAL, startPos, pos-startPos));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					state=State.START;
					
				}
				else{
					throw new IllegalCharException(
							"illegal character" +ch+ "encountered at position" +pos);
				}
			}break; //GOTEQUAL
			
			case AFTER_MINUS:{
				if(ch=='>'){
					pos++;
					column++;
					tokens.add(new Token(Kind.ARROW, startPos, pos-startPos));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					
					
				}
				else{
					tokens.add(new Token(Kind.MINUS, startPos, 1));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));
					
					
				}
				state=State.START;
			}break; //END AFTER_MINUS
			case AFTER_PIPE:{
				if(ch=='-'){
					pos++;
					column++;
					if(chars.charAt(pos)=='>'){
						pos++;
						column++;
						tokens.add(new Token(Kind.BARARROW, startPos, pos-startPos));
						lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
						
						state=State.START;
					}
					else{
						pos=pos-1;
						column=column-1;
						tokens.add(new Token(Kind.OR, startPos, 1));
						lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));
						state=State.START;
					}
				}
				else{
					tokens.add(new Token(Kind.OR, startPos, 1));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));
					
					state=State.START;
				}
			}break;// end AFTER_PIPE
			case AFTER_NOT:{
				if(ch=='='){
					pos++;
					column++;
					tokens.add(new Token(Kind.NOTEQUAL, startPos, pos-startPos));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					state=State.START;
				}
				
				else{
					tokens.add(new Token(Kind.NOT, startPos, 1));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));
					
					state=State.START;
				}
			}break; //end AFTER_NOT
			
			case AFTER_LESS:{
				if(ch=='='){
					pos++;
					column++;
					tokens.add(new Token(Kind.LE, startPos, pos-startPos));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					state=State.START;
				}
				else if(ch=='-'){
					pos++;
					column++;
					tokens.add(new Token(Kind.ASSIGN, startPos, pos-startPos));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					state=State.START;
				}
				else{
					tokens.add(new Token(Kind.LT, startPos, 1));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1));
					
					state=State.START;
				}
			}break; //	end AFTER_LESS
			
			case AFTER_GREATER:{
				if(ch=='='){
					pos++;
					column++;
					tokens.add(new Token(Kind.GE, startPos, pos-startPos));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					state=State.START;
				}
				else{
					tokens.add(new Token(Kind.GT, startPos, pos-startPos));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-(pos-startPos)));
					
					state=State.START;
				}
			}break; //end AFTER_GREATER
			case COMMENT:{
				if(ch=='*'){
					pos++;
					column++;
					while(pos<length){
						if(chars.charAt(pos)=='*' && chars.charAt(pos+1)=='/'){
							pos=pos+2;
							column=column+2;
							break;
						}
						else{
							if(chars.charAt(pos)=='\n'){
								curline++;
								column=-1;
							}
							pos++;
							column++;
						}
					}
					
				}
				else{
					tokens.add(new Token(Kind.DIV,startPos,1));
					lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column-1)); 
					
					
				}
				state=State.START;
			}break; //COMMENT
			
			
			
			
			} // END OF SWITCH(STATE)
			
			
		}
		tokens.add(new Token(Kind.EOF,pos,0));
		lineno.put(tokens.get(tokens.size()-1), new LinePos(curline, column));
		
		return this;  
	}



	final ArrayList<Token> tokens;
	final HashMap<Token, LinePos> lineno;
	final String chars;
	int tokenNum;
	int curline=0;
	int column=0;
	

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	/*
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum+1);		
	}
	*/
	public Token peek() {
	    if (tokenNum >= tokens.size())
	        return null;
	    return tokens.get(tokenNum);
	}
	
	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public int skipWhiteSpaces(int p){
		while (p < chars.length() && Character.isWhitespace(chars.charAt(p))){ /*== ' ' || chars.charAt(p)=='\n' || chars.charAt(p)=='\r' || chars.charAt(p)=='\t'*/ 
			if(chars.charAt(p)=='\n'){
				curline++;
				column=-1;
			}
				
			
			p++;
			column++;
		}
		return p;
		
	}
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		if(t==null)
			return null;
		return t.getLinePos();
	}


}
