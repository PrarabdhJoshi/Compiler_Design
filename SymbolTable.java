package cop5556sp17;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	
	//TODO  add fields
	int cur_scope;
	int next_scope;
	HashMap<String, ArrayList<HashMap<Integer,Dec>>> symtable;
	Stack<Integer> stack= new Stack<Integer>();
	/** 
	 * to be called when block entered
	 */
	
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		//System.out.println(cur_scope);
		//cur_scope=next_scope+1;
		cur_scope=cur_scope+1;
		stack.push(cur_scope);
		//System.out.println(cur_scope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		//System.out.println(stack.size());
		stack.pop();
		//cur_scope = stack.peek();
		//System.out.println(stack.size());
		
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		if(lookup(ident)!=null){
			return false;
		}
		else{
			if(symtable.containsKey(ident)){
				HashMap<Integer,Dec> hash=new HashMap<Integer,Dec>();
				hash.put(stack.peek(), dec);
				symtable.get(ident).add(hash);
			}
			else{
				ArrayList<HashMap<Integer,Dec>> list = new ArrayList<HashMap<Integer,Dec>>();
				HashMap<Integer,Dec> hash=new HashMap<Integer,Dec>();
				hash.put(stack.peek(), dec);
				list.add(hash);
				symtable.put(ident, list);
			}
			//symtable.put(ident, new HashMap(){{put(cur_scope,dec);}});
			//System.out.println(symtable);
			return true;
		}
		
		
		
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		//int check_scope = stack.peek();
		//Stack<Integer> nested = new Stack<Integer>();
		//nested.addAll(stack);
		//System.out.println("in look up ,current scope ="+cur_scope);
		Dec d = null;
		if(symtable.containsKey(ident)){
			ArrayList<HashMap<Integer,Dec>> local = new ArrayList<HashMap<Integer,Dec>>();
			local = symtable.get(ident);
			for(HashMap<Integer,Dec> allkeys: local){
				if(allkeys.containsKey(stack.peek()))
					return allkeys.get(stack.peek());
			}
		}
		
		return d;
		
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		
		symtable = new HashMap<String, ArrayList<HashMap<Integer,Dec>>>();
		//stack = new Stack();
		cur_scope=0;
		next_scope=0;
		stack.push(0);
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	public Dec isvisible(String ident){
			
			Stack<Integer> check_scope=new Stack();
			check_scope.addAll(stack);
			
			//System.out.println("check mein peek scope ="+check_scope.peek());
			//System.out.println(cur_scope);
			if(symtable.containsKey(ident)){
				
				ArrayList<HashMap<Integer,Dec>> local = new ArrayList<HashMap<Integer,Dec>>();
				local = symtable.get(ident);
				//System.out.println(local.get(0));
				//HashMap<Integer,Dec> hash = new HashMap<Integer,Dec>();
				
				//System.out.println(local.size());
				
					while(!check_scope.isEmpty()){
						for(int i=0;i<local.size();i++){
							//System.out.println("running");
							//System.out.println(local.get(i).containsKey(1));
							if(local.get(i).containsKey(check_scope.peek())){
								return local.get(i).get(check_scope.peek());
							}
						}
							
						check_scope.pop();
					}
				
				//HashMap<Integer, Dec> value = (HashMap<Integer, Dec>)symtable.get(ident);	
				/*while(!check_scope.isEmpty()){
					int x = check_scope.pop();
					if(symtable.get(ident).get(x)!=null){
						return true;
					}
				}*/
			}
		
		return null;
	}
	


}
