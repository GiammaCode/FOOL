package compiler;

import java.util.*;
import compiler.lib.*;

/*
 *	accept()  accetta un qualunque baseASTVisitor dato che tutti gli ASTVisitor estendono il base
 *
 */
public class AST {
	
	public static class ProgLetInNode extends Node {
		final List<DecNode> declist;
		final Node exp;
		ProgLetInNode(List<DecNode> d, Node e) {
			declist = Collections.unmodifiableList(d); 
			exp = e;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ProgNode extends Node {
		final Node exp;
		ProgNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class FunNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parlist;
		final List<DecNode> declist; 
		final Node exp;
		FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
	    	id=i; 
	    	retType=rt; 
	    	parlist=Collections.unmodifiableList(pl); 
	    	declist=Collections.unmodifiableList(dl); 
	    	exp=e;
	    }
		
		//void setType(TypeNode t) {type = t;}
		
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ParNode extends DecNode {
		final String id;
		ParNode(String i, TypeNode t) {id = i; type = t;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class VarNode extends DecNode {
		final String id;
		final Node exp;
		VarNode(String i, TypeNode t, Node v) {id = i; type = t; exp = v;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
		
	public static class PrintNode extends Node {
		final Node exp;
		PrintNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IfNode extends Node {
		final Node cond;
		final Node th;
		final Node el;
		IfNode(Node c, Node t, Node e) {cond = c; th = t; el = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class EqualNode extends Node {
		final Node left;
		final Node right;
		EqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class GreaterEqualNode extends Node {
		final Node left;
		final Node right;
		GreaterEqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class LessEqualNode extends Node {
		final Node left;
		final Node right;
		LessEqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class NotNode extends Node {
		final Node exp;
		NotNode(Node e) {exp=e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class AndNode extends Node {
		final Node left;
		final Node right;
		AndNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class OrNode extends Node {
		final Node left;
		final Node right;
		OrNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}


	
	public static class TimesNode extends Node {
		final Node left;
		final Node right;
		TimesNode(Node l, Node r) {left = l; right = r;}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class DivNode extends Node {
		final Node left;
		final Node right;
		DivNode(Node l, Node r) {left = l; right = r;}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class PlusNode extends Node {
		final Node left;
		final Node right;
		PlusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class MinusNode extends Node {
		final Node left;
		final Node right;
		MinusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//							OBJECT ORIENTED EXTENSION
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//nodo classe
	public static class ClassNode extends DecNode {
		final String id;
		final List<FieldNode> fieldList;
		final List<MethodNode> methodList;
		ClassNode(List<FieldNode> fieldList, List<MethodNode> methodList, String id) {
			//la classe una volta dichiarata possiede quei metodi e campi e non è più modificabile
			this.fieldList=Collections.unmodifiableList(fieldList);
			this.methodList =Collections.unmodifiableList(methodList);
			this.id=id;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	//nodo dichirazione del campo di una classe
	public static class FieldNode extends DecNode {
		final String id;
		FieldNode (String id, TypeNode t) {
			this.id=id;
			type=t;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	//nodo dichiarazione di un metodo della classe
	public static class MethodNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parlist;
		final List<DecNode> declist;
		final Node exp;

		public String label; //label creata per la CodeGeneration

		MethodNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
			this.id=i;
			this.retType=rt;
			this.parlist=pl;			//causa ereditarietà non sono immtabili parlist e declist
			this.declist=dl;
			this.exp=e;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	//nodo chiamata di una classe, es: nameClass.setValue(x);
	public static class ClassCallNode extends Node {
		final RefTypeNode classID;
		final List<Node> listNode;
		final String methodID;
		STentry stEntry;
		STentry methodStentry;
		int nestingLevel;
		ClassCallNode (RefTypeNode classID, String methodID, List<Node> listNode) {
			this.classID=classID;
			this.methodID=methodID;
			this.listNode=listNode;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	//nodo relativo alla new ID
	public static class NewNode extends Node {
		final List<Node> argList;
		final String id;
		STentry stentry;
		NewNode (List<Node> argList, String id) {
			this.argList=argList;
			this.id=id;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	//nodo del null, non implementato perchè null.
	public static class EmptyNode extends Node {
		//non implementato per chè null
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	/*
	*	ClassTypeNode che ha come campi:
	*		• ArrayList<TypeNode> allFields (tipi dei campi, inclusi quelli ereditati, in ordine di apparizione)
	*		• ArrayList<ArrowTypeNode> allMethods (tipi funzionali metodi, inclusi ereditati, in ordine apparizione)
	*/
	public static class ClassTypeNode extends TypeNode {


		final ArrayList<TypeNode> allFields;
		final ArrayList<ArrowTypeNode> allMethods;
		ClassTypeNode (ArrayList<TypeNode> allFields, ArrayList<ArrowTypeNode> allMethods) {
			this.allFields = allFields;
			this.allMethods = allMethods;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	/*
	*	MethodTypeNode ha come campo il tipo funzionale:
	*			• final ArrowTypeNode fun
	*/
	public static class MethodTypeNode extends TypeNode {
		final ArrowTypeNode fun;
		MethodTypeNode (ArrowTypeNode fun) {
			this.fun = fun;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class RefTypeNode extends TypeNode {
		final String id;
		RefTypeNode (String id) {   //da rivedere
			this.id=id;
		}
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class EmptyTypeNode extends TypeNode {
		//non implementato perchè null
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//									FINE ESTENSIONE OBJECT ORIENTED
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static class CallNode extends Node {
		final String id;
		final List<Node> arglist;
		STentry entry;
		int nl;
		CallNode(String id, List<Node> p) {
			this.id = id;
			this.arglist = Collections.unmodifiableList(p);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IdNode extends Node {
		final String id;
		STentry entry;
		int nl;
		IdNode(String id) {this.id = id;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolNode extends Node {
		final Boolean val;
		BoolNode(boolean n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IntNode extends Node {
		final Integer val;
		IntNode(Integer n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class ArrowTypeNode extends TypeNode {
		final List<TypeNode> parlist;
		final TypeNode ret;
		ArrowTypeNode(List<TypeNode> p, TypeNode r) {
			parlist = Collections.unmodifiableList(p); 
			ret = r;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class IntTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

}