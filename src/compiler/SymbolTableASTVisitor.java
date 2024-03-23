package compiler;

import java.util.*;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

/*
	La Symbletable è una struttura dati, implementazione come  lista di hash Table
	ArrayList di HasMap,

	SymbolTableASTVisitor ha lo scopo di associare usi a dichiarazioni tramite la symble table
	-dando errori in caso di multiple dichiarazioni e identificatori non dichiarati

	-attaccando alla foglia dell'AST che rappresenta l'uso di un identificatore x la
	symbol table entry (NOI LA CHIAMIAMO STEntry !!), che contiene le informazioni
	 prese dalla dichiarazione di x.

	La visita dell'AST si trasforma in un EAST (enriched AST).
	* */
public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {

	/*symTable è una lista di mappe,
	ogni mappa cosè? è mappa nomi id a palline (info st entry),
	la nostra "pallina" inizia con il nesting level in cui è stata creata
	la dichiarazione.

	L'idea di base è che questa lista è organizzata per scope, noi useremo
	gli indici in cui l'ambiente globale se ne sta a nesting level 0.
	Nella pos=0 ci sta l'ambiente globale, quindi la mappa nella posizione iniziale
	della lista è quella globale, invece la mappa che sta al fronte della lista
	è quella con indice "nesting level" corrente.
	* */
	private List<Map<String, STentry>> symTable = new ArrayList<>();
	private int nestingLevel=0; // Nesting level corrente
	private int decOffset=-2; // counter for offset of local declarations at current nesting level
	int stErrors=0; //tiene il conto degli errori totali

	/*aggiunta della Class Table, a cosa serve?
	Mappa ogni nome di classe nella propria Virtual ,
	serve per preservare le dichiarazioni interne ad una
	classe (campi e metodi) una volta che il visitor ha
	concluso la dichiarazione di una classe.
	* */

	private Map<String, Map<String, STentry>> classTable = new HashMap<>();

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null)
			entry = symTable.get(j--).get(id);
		return entry;
	}

	/*Le visite perchè tornano void?
	L'obbiettivo della visita non è tornare qualcosa ma arricchire l'albero
	e darà degli errori nei casi precedenti (sopra),
	Quando non faccio nulla è lo stesso codice della print praticaamente.

	Se sono nella radice (corpo principale programma) ProgLeInNode cosa faccio?
	Devo creare la tabella per l'ambiente globale, HasMap, l'idea è che li
	ci metto le dichiarazioni dell'ambiente globale, questa hm la metto nella
	symbleTable.
	Successivamente faccio la visita delle dichiarazioni e poi visito
	l'exp che a sua volta visitera dichirazioni ecc.

	L'idea è che la lista di hashMap cresce quando entro negli scope e
	decresce quando esco dagli scope, rimuovo il fronte.
	Finito tutto il programma posso rimuovere tutte le tabelle, perchè
	SymTable serve solo per fare questo lavoro di check e arricchimento delle
	informazioni.
	* */
	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.remove(0);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();
		for (ParNode par : n.parlist) parTypes.add(par.getType());
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level
		decOffset=-2;

		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
		return null;
	}

	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	//////////////////////////// OPERATOR EXTENSION ////////////////////////////////////////////////////////////////////
	@Override
	public Void visitNode(GreaterEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	//////////////////////////// OBJECT ORIENTED EXTENSION /////////////////////////////////////////////////////////////
	@Override
	public Void visitNode(ClassNode n) {
		if (print) printNode(n);
		/*Nella SIMBTABLE di livello 0 viene aggiunto il nome della classe
		mappato ad una nuova STentry, cosa mettiamo nella StEntry?
		Offset = -2 e incrementato, il tipo , nl=0
		Se non eredito il tipo è un nuovo oggetto ClassTypeNode con lista vuota
		(Field e metodi liste).
		La gestisco come in VarNode, il type non c'è l'ho e lo creo
		*/
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> allFields = new ArrayList<>();
		List<ArrowTypeNode> allMethods = new ArrayList<>();
		ClassTypeNode typeNode = new ClassTypeNode(allFields, allMethods);
		STentry entry = new STentry(nestingLevel, typeNode,decOffset--);
		/*
		Nella CLASS TABLE, invece viene aggiunto il nome della classe associato
		ad una nuova VIRTUAL TABLE, che funziona come prima (SymTable).
		Se non eredito la creo vuota.
		* */
		Map<String, STentry> virtualTable = new HashMap<>();
		classTable.put(n.id, virtualTable);
		if (hm.put(n.id, entry) != null){
			System.out.println("Par id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		/*
		Mentre netriamo nella dichiarazione della classe, creo un nuovo livello
		per la symTable ma non vuoto, GLI METTO LA VIRTUAL TABLE di prima.
		* */
		nestingLevel++;
		symTable.add(virtualTable);
		//mi preparo per entrare in un nuovo scope.
		int previosNl = decOffset;
		decOffset = -2;
		int fieldOffset =-1;
		int methodOffset=0;
		/*Una volta entrato nella dichiarazione della classe:
		aggiorno Virtual Table e Class Type Node tutte le volte che si
		incontrano.
		--> dichiarazione di campo (NO visit FieldNode)
		--> dichiarazione di metodo (visit MethodNode)
		n.MethodNode è la methodList

		COME FACCIO AD AGGIORNARE LA VIRTUAL TABLE una volta entrato nella dichiarazione
		della classe?
		Come fatto a lezione, a parte che (metodo brutto ma funzionante)

		1) se trovo nome campo o metodo gia presente non lo considero come errore
		ma come overriding, sostituisco con la nuova stEnty ma con il vecchio
		offset.
		Devo sostituire nella stessa posizione praticamente.
		Non devo consentire però l'overriding Field --> Method e viceversa.

		2) se campo o metodo rimane invariato, uso contatore offset e decremento
		e incremento.

		COME FACCIO AD AGGIORNARE CLASS TYPE NODE ?
		viene fatto nel codice della visita di classNode e
		- per i campu aggionrno array allFields settando -offset-1 al tipo
		converto l'offset in una posizione.
		- per i metodi aggiorno allMethod settando offset (il primo è 0)
		* */
		updateDecOfField(n, virtualTable, fieldOffset, allFields);
		updateDecOfMethod(n, virtualTable, methodOffset, allMethods, symTable);

		/*All'uscita della dichirazione della classe rimuovo il livello corrente
		della SymTable.*/
		symTable.remove(nestingLevel--);
		decOffset = previosNl;
		return null;
	}

	@Override
	public Void visitNode(NewNode n) {
		if (print) printNode(n);
		//controllo che n.id esiste, cioè esiste la classe di cui facciamo new
		if (this.classTable.containsKey(n.id)){
			/* 	STentry della classe ID in campo "entry"
			ID deve essere in Class Table e STentry presa
	 		direttamente da livello 0 della Symbol Table
			**/
			n.stEntry = this.symTable.get(0).get(n.id);
		}
		else{
			System.out.println("Class id" + n.id + " at line "+ n.getLine() +" not declared");
			stErrors++;
		}
		//faccio la visit per ogni argomento passato alla classe
		for (Node node : n.argList) visit(node);
		return null;
	}
	@Override
	public Void visitNode(EmptyNode n) {
		if (print) printNode(n);
		return null;
	}

	/*ID1.ID2 dove id1 è una variabile, quindi la devo andare a cercare
	quindi ricupero la sua STentry, invece id2 dove lo cerco?
	Lo cerco nella virtual table del tipo statico di ID1, come trovo il tipo
	statico? Il tipo statico si trova nell'stEntry ID1 e guardo il type
	che deve essere un RefTypeNode (li dentro c'è il nome della classe)
	Trovato il nome della classe, vado nella class Table e poi raggiungo la
	virtual Table e trovo stEntry di ID2 (il metodo)

	C'é una possibilità di errore perchè se ID1 non è RefTypeNode
	la notazione '.' (punto) è sbagliata.
	* */
	@Override
	public Void visitNode(ClassCallNode n) {
		if (print) printNode(n);

		//cerco la dichirazione della classe in quel livello o in quelli superiori, entryClass non è mai null
		STentry entryClass = stLookup(n.classID.id);
		//cerco la dichiarazione metodo nella virtual table
		Map<String, STentry> virtualTable = classTable.get(((RefTypeNode)entryClass.type).id);
		System.out.println(((RefTypeNode)entryClass.type).id);
		// id è il nome dell'oggetto e non della classe, come mai?
		// n.classID confonde perchè l'ID è dell'oggetto non della classe.
		// Come facciamo a risalire all'ID della classe? potremmo aggiugnere un campo a reftypenode?
		STentry entryMethod = virtualTable.get(n.methodID);

		if (entryMethod == null) {
			System.out.println("Method "+ n.methodID + " of Class " + n.classID + " at line "+ n.getLine() + " not declared");
			stErrors++;
		}
		else {
			// se la trovo attacco la classNode e methodNode all' classCallNode.
			n.stEntry = entryClass; // nesting level 0
			n.methodStentry = entryMethod; // nesting level 1
			n.nestingLevel = nestingLevel; // nesting level dell'uso
		}
		return null;
	}

	//metodo usato in visitClassNode
	private void updateDecOfField(ClassNode n, Map<String, STentry> virtualTable, int fieldOffset, List<TypeNode> allFields) {
		for(FieldNode fieldNode : n.fieldList){
			STentry fieldSt = new STentry(nestingLevel, fieldNode.getType(), fieldOffset--);
			if (virtualTable.put(fieldNode.id, fieldSt)!= null){
				System.out.println("Field id " + fieldNode.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
			allFields.add(fieldNode.getType());
		}
	}

	private void updateDecOfMethod(ClassNode n, Map<String, STentry> virtualTable, int methodOffset, List<ArrowTypeNode> allMethods, List<Map<String, STentry>> symTable) {
		for(MethodNode methodNode : n.methodList){
			List<TypeNode> paramMethodTypes = new ArrayList<>();
			for(ParNode parNode : methodNode.parlist){
				paramMethodTypes.add(parNode.getType());
			}

			MethodTypeNode methodType  = new MethodTypeNode(new ArrowTypeNode(paramMethodTypes,methodNode.retType));
			STentry methodSt = new STentry(nestingLevel, methodType ,methodOffset++);
			if (virtualTable.put(methodNode.id, methodSt) != null){
				System.out.println("Method id " + methodNode.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
			allMethods.add(new ArrowTypeNode(paramMethodTypes, methodNode.retType));
			nestingLevel++;
			Map<String, STentry> hashMapMethod = new HashMap<>();
			symTable.add(hashMapMethod);
			int previousNlMethod = decOffset;
			decOffset = -2;
			int parOffset = 1;
			for (ParNode par : methodNode.parlist) {
				STentry parEntry = new STentry(nestingLevel,par.getType(),parOffset++);
				if (hashMapMethod.put(par.id, parEntry) != null) {
					System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
					stErrors++;
				}
			}
			//ora visito le dichiarazioni dei methodi
			for (Node dec : methodNode.declist){
				visit(dec);
			}
			//visito il corpo del metodo
			visit(methodNode.exp);
			symTable.remove(nestingLevel--);
			decOffset=previousNlMethod;
		}
	}

}
