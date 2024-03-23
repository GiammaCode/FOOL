package compiler;

import java.lang.reflect.Array;
import java.util.*;

import com.sun.jdi.ClassType;
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
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

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
	public Void visitNode(TimesNode n) {
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
	public Void visitNode(MinusNode n) {
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

	// Implementazione Object Oriented

	@Override
	public Void visitNode(ClassNode n){
		if (print) printNode(n);

		// prendo symtable al livello attuale come per la VarNode.
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> allFields = new ArrayList<>();
		List<ArrowTypeNode> allMethods = new ArrayList<>();

		// creo la pallina. Gestisco la dichiarazione di classe: quindi metto il nesting level e creo il classtypenode
		// (tipo funzionale) della classe (allFields, allMethods). I tipi dei campi finisco in due posti: qua nella dichiaraizone
		// del tipo della classe e nella dichiarazione proprio dei campi stessi di cui dichiariamo anche il tipo.
		STentry entry = new STentry(nestingLevel, new ClassTypeNode(allFields, allMethods), decOffset--);


		//aggiungo nuova mappa. Virtual table della Class Table.
		Map<String, STentry> hmn = new HashMap<>();
		// metto nella class table il nome della classe dichiarata e poi la virtual table che andrò a riempire
		classTable.put(n.id, hmn);

		// inserimento di ID nella symtable. RICORDA CHE IL NOME DELLA classe E QUINDI LA classe, VIENE INSERITA
		// NELLO SCOPE ESTERNO NON IN QUELLO INTERNO, QUINDI METTO L'ID NELLO SCOPE ESTERNO E POI NE CREO UN ALTRO
		// DOVE AVVERRÀ IL RESTO DELLA ROBA.
		if (hm.put(n.id, entry) != null) {
			System.out.println("Class id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}

		// creare una nuova hashmap per la symTable. entro in un nuovo scope
		nestingLevel++;
		symTable.add(hmn);

		//entro in un nuovo scope, creo un nuovo AR e quindi devo ripartire da -2, salvandomi l'offset da cui son partito.
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level
		// dato che entriamo in un nuovo scope dobbiamo re-inizializzare l'offset globale.
		decOffset=-2;

		int fieldOffset=-1;

		// riempiamo le liste totali da aggiungere alla STEntry del livello globale, quindi da aggiungere alla ClassTypeNode
		// prendiamo i campi e li salviamo
		for (FieldNode field : n.fieldList){
			/*
			 * Notare che non facciamo la visitFieldNode perchè è da fare qua! E' implicita nella visita della dichiarazione di classe.
			 * In pratica il pezzo di codice qui sotto è la visitFieldNode.
			 *
			 * fieldOffset è il contatore dell'offset dei campi
			 */
			if (hmn.put(field.id, new STentry(nestingLevel, field.getType(), fieldOffset--)) != null) {
				System.out.println("Field id " + field.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}

			// allFields per la STEntry del livello globale. In allFields si setta la posizione del campo a -offset-1. Ad
			// esempio: il primo campo avrà offset -1 quindi in allFields sarà in posizione 0 (-offset-1 = -(-1) -1 = +1 -1 = 0)
			allFields.add(field.getType());
		}

		int methodOffset=0;



		// prendiamo i metodi e per ognuno di questi prendiamo i tipi parametri ed il tipo di ritorno e per ognuno di questi
		// ci facciamo un arrowtypenode (che in pratica sono tante dichiarazioni di funzioni quindi bisogna fare come
		// in fundec). Un volta creato questo arrowtypenode lo aggiungiamo alla lista di arrowtyopenode.
		for (MethodNode methodNode : n.methodList) {

			List<TypeNode> paramMethodTypes = new ArrayList<>();
			for (ParNode param : methodNode.parlist) paramMethodTypes.add(param.getType());

			if (hmn.put(methodNode.id, new STentry(nestingLevel, new MethodTypeNode(new ArrowTypeNode(paramMethodTypes,
					methodNode.retType)), methodOffset++)) != null) {
				System.out.println("Method id " + methodNode.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}

			allMethods.add(new ArrowTypeNode(paramMethodTypes, methodNode.retType));

			nestingLevel++;
			Map<String, STentry> hmd = new HashMap<>();
			symTable.add(hmd);
			int prevNLDecOffsetMethod=decOffset; // stores counter for offset of declarations at previous nesting level
			decOffset=-2;

			int parOffset=1;
			for (ParNode par : methodNode.parlist) {
				STentry parEntry = new STentry(nestingLevel,par.getType(),parOffset++);
				if (hmd.put(par.id, parEntry) != null) {
					System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
					stErrors++;
				}

			}

			// visito le dichiarazione all'interno del metodo. Quando visito queste dichiarazioni posso incontrare
			// anche dei funNode.
			for (Node dec : methodNode.declist) {
				visit(dec); // qui ripartono da -2 gli offset
			}

			// visito il corpo del metodo
			visit(methodNode.exp);

			symTable.remove(nestingLevel--);
			decOffset=prevNLDecOffsetMethod; // restores counter for offset of declarations at previous nesting level

		}

		//ho visitato tutto il corpo e allora rimuovo la hashmap corrente poiche' esco dallo scope.
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level . Ripristino l'offset in cui ero.
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
			n.stentry = this.symTable.get(0).get(n.id);
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

		//cerco la dichirazione della classe in quel livello o in quelli superiori
		STentry entryClass = stLookup(n.classID.id);
		//cerco la dichiarazione metodo nella virtual table
		Map<String, STentry> virtualTable = classTable.get(((RefTypeNode)entryClass.type).id);
		System.out.println(((RefTypeNode)entryClass.type).id);
		// id è il nome dell'oggetto e non della classe, come mai?
		// n.classID confonde perchè l'ID è dell'oggetto non della classe.
		// Come facciamo a risalire all'ID della classe? potremmo aggiugnere un campo a reftypenode?
		STentry entryMethod = virtualTable.get(n.methodID);


		if (entryClass == null){
			System.out.println("Instance of Class " + n.classID + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else if (entryMethod == null) {
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

}
