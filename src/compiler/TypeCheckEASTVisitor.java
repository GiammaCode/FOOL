package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;
import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode,TypeException> {

	TypeCheckEASTVisitor() { super(true); } // enables incomplete tree exceptions 
	TypeCheckEASTVisitor(boolean debug) { super(true,debug); } // enables print for debugging

	//checks that a type object is visitable (not incomplete) 
	private TypeNode ckvisit(TypeNode t) throws TypeException {
		visit(t);
		return t;
	} 
	
	@Override
	public TypeNode visitNode(ProgLetInNode n) throws TypeException {
		if (print) printNode(n);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(ProgNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(FunNode n) throws TypeException {
		if (print) printNode(n,n.id);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		if ( !isSubtype(visit(n.exp),ckvisit(n.retType)) ) 
			throw new TypeException("Wrong return type for function " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(VarNode n) throws TypeException {
		if (print) printNode(n,n.id);
		if ( !isSubtype(visit(n.exp),ckvisit(n.getType())) )
			throw new TypeException("Incompatible value for variable " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(PrintNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(IfNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.cond), new BoolTypeNode())) )
			throw new TypeException("Non boolean condition in if",n.getLine());
		TypeNode t = visit(n.th);
		TypeNode e = visit(n.el);
		if (isSubtype(t, e)) return e;
		if (isSubtype(e, t)) return t;
		throw new TypeException("Incompatible types in then-else branches",n.getLine());
	}

	@Override
	public TypeNode visitNode(EqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isInteger(l) && isInteger(r)))
			throw new TypeException("Incompatible types in greater equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(LessEqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isInteger(l) && isInteger(r)))
			throw new TypeException("Incompatible types in less equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(TimesNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in multiplication",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(DivNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isInteger(l) && isInteger(r)))
			throw new TypeException("Non integers in division",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(PlusNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in sum",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(MinusNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isInteger(l) && isInteger(r)))
			throw new TypeException("Non integers in subtract",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(AndNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isBoolean(l) && isBoolean(r)))
			throw new TypeException("Incompatible types in and operation",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(OrNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isBoolean(l) && isBoolean(r)))
			throw new TypeException("Incompatible types in or operation",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(NotNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode e = visit(n.exp);
		if (!isBoolean(e))
			throw new TypeException("Incompatible types in Not operation",n.getLine());
		return new BoolTypeNode();
	}



	@Override
	public TypeNode visitNode(CallNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry); 
		if ( !(t instanceof ArrowTypeNode) )
			throw new TypeException("Invocation of a non-function "+n.id,n.getLine());
		ArrowTypeNode at = (ArrowTypeNode) t;
		if ( !(at.parlist.size() == n.arglist.size()) )
			throw new TypeException("Wrong number of parameters in the invocation of "+n.id,n.getLine());
		for (int i = 0; i < n.arglist.size(); i++)
			if ( !(isSubtype(visit(n.arglist.get(i)),at.parlist.get(i))) )
				throw new TypeException("Wrong type for "+(i+1)+"-th parameter in the invocation of "+n.id,n.getLine());
		return at.ret;
	}

	@Override
	public TypeNode visitNode(IdNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry); 
		if (t instanceof ArrowTypeNode)
			throw new TypeException("Wrong usage of function identifier " + n.id,n.getLine());
		return t;
	}

	@Override
	public TypeNode visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return new IntTypeNode();
	}

// gestione tipi incompleti	(se lo sono lancia eccezione)
	
	@Override
	public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
		if (print) printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.ret,"->"); //marks return type
		return null;
	}

	@Override
	public TypeNode visitNode(BoolTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(IntTypeNode n) {
		if (print) printNode(n);
		return null;
	}

// STentry (ritorna campo type)

	@Override
	public TypeNode visitSTentry(STentry entry) throws TypeException {
		if (print) printSTentry("type");
		return ckvisit(entry.type); 
	}

	@Override
	public TypeNode visitNode(ClassTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(RefTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(MethodTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(EmptyTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	// STentry (ritorna campo type). Verrà chiamata quando c'è da sapere il tipo all'interno di una pallina e allora tornerà
	// il tipo della dichiarazione.

	// non usato (come ParNode)
	//	@Override
	//	public TypeNode visitNode(FieldNode node) throws TypeException {
	//		return super.visitNode(node);
	//	}


	@Override
	public TypeNode visitNode(MethodNode n) throws TypeException {
		if (print) printNode(n,n.id);

		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) {
			} catch (TypeException e) {
				// per non bloccare tutto alla dichiarazione
				System.out.println("Type checking error in a method declaration: " + e.text);
			}
		// anche qua come nella visita della varNode, controlliamo che il corpo del metodo ritorni un tipo correlato
		// (sottotipo di...) con quello dichiarato (es: se un metodo dichiarato torna int e gli facciamo tornare una stringa
		// dal corpo della funzione allora non andrà bene!).
		if ( !isSubtype(visit(n.exp), ckvisit(n.retType)) )
			throw new TypeException("Wrong return type for method " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(ClassNode n) throws TypeException {
		if (print) printNode(n,n.id);

		for (MethodNode methodNode : n.methodList) {
			try {
				visit(methodNode);
			} catch (IncomplException e) {
			} catch (TypeException e) {
				// per non bloccare tutto alla dichiarazione
				System.out.println("Type checking error in a method declaration: " + e.text);
			}
		}

		return null;
	}

	@Override
	public TypeNode visitNode(ClassCallNode n) throws TypeException {
		if (print) printNode(n,n.classID.id);
		ArrowTypeNode at;

		// recupero tipo (che mi aspetto essere MethodTypeNode) da STentry. In teoria non sarò sempre e solo un
		// methodTypeNode?
		TypeNode t = visit(n.methodStentry);

		if ( !(t instanceof ArrowTypeNode) && !(t instanceof MethodTypeNode) ) {
			throw new TypeException("Invocation of a non-method " + n.methodID, n.getLine());
		}

		if (t instanceof MethodTypeNode) {
			at = ((MethodTypeNode) t).fun;
		} else {
			System.out.println("Sono un arrowtype node");
			at = (ArrowTypeNode) t;
		}

		// errori possibili (che indicano, in ordine, i controlli da fare):
		// Invocation of a non-function [id del CallNode]
		// Wrong number of parameters in the invocation of [id del CallNode]
		// Wrong type for ...-th parameter in the invocation of [id del CallNode]
		if ( !(at.parlist.size() == n.listNode.size()) )
			// caso in cui nella dichiarazione ho un certo numero di argomenti e nell'uso ne ho un numero diverso
			throw new TypeException("Wrong number of parameters in the invocation of "+n.methodID,n.getLine());
		for (int i = 0; i < n.listNode.size(); i++)
			if ( !(isSubtype(visit(n.listNode.get(i)),at.parlist.get(i))) )
				// caso in cui nella dichiarazione ho certi tipi negli argomenti e nell'uso ne ho tipi diversi
				throw new TypeException("Wrong type for "+(i+1)+"-th parameter in the invocation of "+n.methodID,n.getLine());

		// dopo i check restituisco il tipo di ritorno della funzione, perchè vuol dire che tutto è andato bene.
		return at.ret;
	}

	@Override
	public TypeNode visitNode(NewNode n) throws TypeException {

		// recupero tipo e mi aspetto sia RefTypeNode
		TypeNode t = visit(n.stentry);
		if ( !(t instanceof ClassTypeNode) ) {
			throw new TypeException("Invocation of a new non-class " + n.id, n.getLine());
		}

		ClassTypeNode at = (ClassTypeNode) t;

		// errori possibili (che indicano, in ordine, i controlli da fare):
		// Invocation of a new non-class
		// Wrong number of parameters in the invocation of new non-class
		// Wrong type for ...-th parameter in the invocation of new non-class
		if ( !(at.allFields.size() == n.argList.size()) )
			// caso in cui nella dichiarazione ho un certo numero di argomenti e nell'uso ne ho un numero diverso
			throw new TypeException("Wrong number of parameters in the invocation of "+n.id,n.getLine());
		for (int i = 0; i < n.argList.size(); i++)
			if ( !(isSubtype(visit(n.argList.get(i)),at.allFields.get(i))) )
				// caso in cui nella dichiarazione ho certi tipi negli argomenti e nell'uso ne ho tipi diversi
				throw new TypeException("Wrong type for "+(i+1)+"-th parameter in the invocation of "+n.id,n.getLine());

		return new RefTypeNode(n.id);
	}

	@Override
	public TypeNode visitNode(EmptyNode n) throws TypeException {
		return new EmptyTypeNode();
	}

}