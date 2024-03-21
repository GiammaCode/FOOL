package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

public class PrintEASTVisitor extends BaseEASTVisitor<Void,VoidException> {
	/*
	* Facciamo un semplice visita del EAST generato in modo da visualizzarlo
	* stampando in modo indentato, oltre ai suoi nodi (di classe che eredita
	* da Node), anche la sue STentry.
	*
	* Funziona grazie a un visitor che consente di visitare sia Node che Stentry
	* tramite una interfaccia Visitable (contiene il metodo accept), implementata da
	* entrambi.
	*
	* */
	PrintEASTVisitor() { super(false,true); } 

	@Override
	public Void visitNode(ProgLetInNode n) {
		printNode(n);
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(FunNode n) {
		printNode(n,n.id);
		visit(n.retType);
		for (ParNode par : n.parlist) visit(par);
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ParNode n) {
		printNode(n,n.id);
		visit(n.getType());
		return null;
	}

	@Override
	public Void visitNode(VarNode n) {
		printNode(n,n.id);
		visit(n.getType());
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}

	@Override
	public Void visitNode(EqualNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(TimesNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(PlusNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	@Override
	public Void visitNode(MinusNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) {
		printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		printNode(n,n.id+" at nestinglevel "+n.nl); 
		visit(n.entry);
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		printNode(n,n.id+" at nestinglevel "+n.nl); 
		visit(n.entry);
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		printNode(n,n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		printNode(n,n.val.toString());
		return null;
	}
	
	@Override
	public Void visitNode(ArrowTypeNode n) {
		printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.ret,"->"); //marks return type
		return null;
	}

	@Override
	public Void visitNode(BoolTypeNode n) {
		printNode(n);
		return null;
	}

	@Override
	public Void visitNode(IntTypeNode n) {
		printNode(n);
		return null;
	}
	
	@Override
	public Void visitSTentry(STentry entry) {
		printSTentry("nestlev "+entry.nl);
		printSTentry("type");
		visit(entry.type);
		printSTentry("offset "+entry.offset);
		return null;
	}

	// OO Implementation
	@Override
	public Void visitNode(ClassNode n) {
		printNode(n, "Class id: " + n.id);
		for (Node field : n.fieldList) visit(field);
		for (Node method : n.methodList) visit(method);
		return null;
	}

	@Override
	public Void visitNode(FieldNode n) {
		printNode(n, "Field id: " + n.id);
		return null;
	}

	@Override
	public Void visitNode(MethodNode n) {
		//uguale a funNode
		printNode(n, "Method id: " + n.id);
		visit(n.retType);
		for (ParNode par : n.parlist) visit(par);
		for (DecNode dec : n.declist) visit(dec);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode n) {
		printNode(n, "Class id: " + n.classID.id + " Method id " + n.methodID +  " at nesting level: " + n.nestingLevel  );
		visit(n.classID); //stampo il tipo della classe
		visit(n.stEntry); //printo la entry della dichirazione della classe
		visit(n.methodStentry); //printo la entry della dichirazione del metodo
		for(Node node : n.listNode) visit(node);
		return null;
	}

	@Override
	public Void visitNode(NewNode n) {
		printNode(n, "New node with id: " + n.id);
		for (Node arg : n.argList) visit(arg);
		visit(n.stentry);//visitiamo l'stEntry del nuovo nodo
		return null;
	}

	@Override
	public Void visitNode(EmptyNode n) {
		printNode(n,"Empty node");
		return null;
	}

	@Override
	public Void visitNode(ClassTypeNode n) {
		printNode(n);
		for (TypeNode fieldType : n.allFields) visit(fieldType);
		for (ArrowTypeNode methodType : n.allMethods) visit(methodType);
		return null;
	}

	@Override
	public Void visitNode(MethodTypeNode n) {
		printNode(n);
		visit(n.fun);
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode n) {
		printNode(n, "Class refId: " + n.id);
		return null;
	}

	@Override
	public Void visitNode(EmptyTypeNode n) {
		printNode(n);
		return null;
	}
}
