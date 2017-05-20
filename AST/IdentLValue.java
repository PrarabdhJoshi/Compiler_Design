package cop5556sp17.AST;

import static cop5556sp17.AST.Type.TypeName.BOOLEAN;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

public class IdentLValue extends ASTNode {
	TypeName type;
	private Dec dec;
	public Dec getDec() {
		return dec;
	}

	public void setDec(Dec dec) {
		this.dec = dec;
	}

	public IdentLValue(Token firstToken) {
		super(firstToken);
	}
	
	@Override
	public String toString() {
		return "IdentLValue [firstToken=" + firstToken + "]";
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentLValue(this,arg);
	}

	public TypeName getType(){
		return type;
	}
	public void setType(TypeName t){
		type = t;
	}
	
	public String getText(){
		return firstToken.getText(); 
	}

	

}
