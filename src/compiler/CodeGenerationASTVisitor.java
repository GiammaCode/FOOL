package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

    //Questa classe viene utilizzata per creare codice assembly che poi sarà elaborato dalla virtual machine

    CodeGenerationASTVisitor() {
    }

    CodeGenerationASTVisitor(boolean debug) {
        super(false, debug);
    } //enables print for debugging

    @Override
    public String visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        String declCode = null;
        for (Node dec : n.declist) declCode = nlJoin(declCode, visit(dec));
        return nlJoin(
                "push 0",
                declCode, // generate code for declarations (allocation)
                visit(n.exp),
                "halt",
                getCode()
        );
    }

    @Override
    public String visitNode(ProgNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.exp),
                "halt"
        );
    }

    @Override
    public String visitNode(FunNode n) {
        if (print) printNode(n, n.id);
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : n.declist) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (int i = 0; i < n.parlist.size(); i++) popParl = nlJoin(popParl, "pop");
        String funl = freshFunLabel();
        putCode(
                nlJoin(
                        funl + ":",
                        "cfp", // set $fp to $sp value
                        "lra", // load $ra value
                        declCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(n.exp), // generate code for function body expression
                        "stm", // set $tm to popped value (function result)
                        popDecl, // remove local declarations from stack
                        "sra", // set $ra to popped value
                        "pop", // remove Access Link from stack
                        popParl, // remove parameters from stack
                        "sfp", // set $fp to popped value (Control Link)
                        "ltm", // load $tm value (function result)
                        "lra", // load $ra value
                        "js"  // jump to to popped address
                )
        );
        return "push " + funl;
    }

    @Override
    public String visitNode(VarNode n) {
        if (print) printNode(n, n.id);
        return visit(n.exp);
    }

    @Override
    public String visitNode(PrintNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.exp),
                "print"
        );
    }

    @Override
    public String visitNode(IfNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.cond),
                "push 1",
                "beq " + l1,
                visit(n.el),
                "b " + l2,
                l1 + ":",
                visit(n.th),
                l2 + ":"
        );
    }

    @Override
    public String visitNode(CallNode n) {
        if (print) printNode(n, n.id);
        String argCode = null, getAR = null;
        for (int i = n.arglist.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(n.arglist.get(i)));
        for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");
        //controllo che non sia methodNodeType
        if( !(n.entry.type instanceof MethodTypeNode) ) {
            return nlJoin(
                    "lfp", // load Control Link (pointer to frame of function "id" caller)
                    argCode, // generate code for argument expressions in reversed order
                    "lfp", getAR, // retrieve address of frame containing "id" declaration
                    // by following the static chain (of Access Links)
                    "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                    "ltm", // load Access Link (pointer to frame of function "id" declaration)
                    "ltm", // duplicate top of stack
                    "push " + n.entry.offset, "add", // compute address of "id" declaration
                    "lw", // load address of "id" function
                    "js"  // jump to popped address (saving address of subsequent instruction in $ra)
            );
            //se lo è mischiamo un po la chiamata a funzione
            //faccio un deferenziazione in più per raggiungere la dispach table
            //applico l'offset e raggiungo l'indirizzo a cui saltare
        }else {
            return nlJoin("lfp",
                    argCode,
                    "lfp",
                    getAR,
                    "stm",
                    "ltm",
                    "ltm", // fino a qui uguale a sopra nel caso abbiamo una classica chiamata di funzione.
                    "lw", // da quì in giù uguale alla classCallNode: dereferenzio (mi sposto) alla dispatch table dove ho tutti i metodi
                    "push " + n.entry.offset, "add",
                    "lw",
                    "js");
        }
    }
        //invariato perchè risale la catena statica che nel caso di campi o metodi di una classe lo porterà nello heap
    @Override
    public String visitNode(IdNode n) {
        if (print) printNode(n, n.id);
        String getAR = null;
        for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");
        return nlJoin(
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "push " + n.entry.offset, "add", // compute address of "id" declaration
                "lw" // load value of "id" variable
        );
    }

    @Override
    public String visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + (n.val ? 1 : 0);
    }

    @Override
    public String visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + n.val;
    }

    @Override
    public String visitNode(EqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "beq " + l1,
                "push 0",
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"
        );
    }

    @Override
    public String visitNode(TimesNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "mult"
        );
    }

    @Override
    public String visitNode(MinusNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "sub"
        );
    }

    //////////////////////////// OPERATOR EXTENSION ////////////////////////////////////////////////////////////////////
    @Override
    public String visitNode(LessEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "bleq " + l1,   // se l'ultimo operando sullo stack è <= del penultimo vado a l1 e push 1
                "push 0",       //senò push 0
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"

        );
    }

    @Override
    public String visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "sub",          //sottraggo v2-v1 e pusho il ris
                "push -1",      //pusho anche -1 (per il confronto)
                "bleq " + l1,   //-1 è <= del risultato della sottrazione?
                "push 1",       //no, quindi il primo numero (della sub) è >= del secondo restituisco true
                "b " + l2,
                l1 + ":",
                "push 0",       //si, -1  è piu piccolo del risultato della sottrazioni restituisco false
                l2 + ":"
        );
    }

    @Override
    public String visitNode(AndNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "add",          //faccio la somma true=1, false=0 pusho il ris
                "push 2",       //pusho 2 perchè accetto solo due true (1+1)
                "beq " + l1,
                "push 0",       //diverso da due: pusho 0 false (ho avuto un true e un false oppure due false)
                "b " + l2,
                l1 + ":",
                "push 1",       //uguale a 2: pusho 1 true
                l2 + ":"
                );
    }

    @Override
    public String visitNode(OrNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "add",          //faccio la somma true=1, false=0 pusho il ris
                "push 0",       //pusho 0 perchè vado a studiare l'unico caso false (false + false)
                "beq " + l1,
                "push 1",       //diverso da 1: pusho 1 true (ho avuto un true e un false oppure due true)
                "b " + l2,
                l1 + ":",
                "push 0",       //uguale a 0: pusho 0 false
                l2 + ":"
        );
    }

    @Override
    public String visitNode(NotNode n) {
        if (print) printNode(n, n.exp.toString());
        //pusho un 1, poi 1-exp se exp=0, ris=1 faccio il complemento
        return nlJoin(
                "push 1",
                visit(n.exp),
                "sub"
        );
    }

    @Override
    public String visitNode(DivNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "div"
        );
    }

    @Override
    public String visitNode(PlusNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "add"
        );
    }

    //////////////////////////// OBJECT ORIENTED EXTENSION /////////////////////////////////////////////////////////////
    @Override
    public String visitNode(ClassNode n) {
        if(print) printNode(n);
        List<String> dispachTaple = new ArrayList<>();
        //visito tutti i methodNode della classe
        for (MethodNode method : n.methodList){
            visit(method);
            //Inserisco le label dei metodi dentro la dispachTale, l'offeset inserendoli in questo modo risulta corretto
            dispachTaple.add(method.label);
        }
        String heapLabel= null;
        for(String label: dispachTaple){
            // Per ogni label, salviamo l'indirizzo della label nell'heap, successivamente incrementiamo hp.
            heapLabel = nlJoin(heapLabel,
                    "push " + label, // pusho sullo stack l'indirizzo (la label) dell'etichetta
                    "lhp", // pusho sullo stack il contenuto del registro hp (heap pointer)
                    "sw", // store word: poppo i due valori dalla cima dello stack. Metto il secondo all'indirizzo puntato dal primo
                          //metto l'etichetta dell metodo dove puntava l'heap pointer

                    "lhp", //  pusho sullo stack il contenuto del registro hp (heap pointer)
                    "push 1", // aggiungo 1
                    "add", // li sommo assieme
                    "shp"); // poppo il valore di hp aumentato e lo inserisco nuovamente nel registro hp
        }
        return nlJoin(
                "lhp",  // Pusho sullo stack il valore di hp prima di incrementarlo. Questo sarà il punto di inizio
                heapLabel //  Scorro tutta la dispachTable e per ciascuna etichetta la memorizzo a indirizzo hp per poi incrementarlo

        );
    }

    //FieldNode come ParNode non utilizzato.
    //public String visitNode(FieldNode n) {}

    //simile a funNode ma con il campo label e return null
    @Override
    public String visitNode(MethodNode n) {
        if (print) printNode(n, n.id);
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : n.declist) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (int i = 0; i < n.parlist.size(); i++) popParl = nlJoin(popParl, "pop");
        n.label = freshFunLabel();
        putCode(
                nlJoin(
                        n.label + ":",
                        "cfp", // set $fp to $sp value
                        "lra", // load $ra value
                        declCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(n.exp), // generate code for function body expression
                        "stm", // set $tm to popped value (function result)
                        popDecl, // remove local declarations from stack
                        "sra", // set $ra to popped value
                        "pop", // remove Access Link from stack
                        popParl, // remove parameters from stack
                        "sfp", // set $fp to popped value (Control Link)
                        "ltm", // load $tm value (function result)
                        "lra", // load $ra value
                        "js"  // jump to popped address
                )
        );
        return null;
    }

    //id1.id2()
    //assomiglia alla chiamata di metodo di una funzione, deve costruire l'AR del metodo invocato
    //cambia il modo incui setto l'acces link perchè deve puntare allo scope che lo racchiude(object pointer)
    //che trovo grazie a id1 che trovo sommando l'offset e risalendo la catena statica
    //recupero l'op e lo uso per settare l'access link
    //lo uso anche per raggiungere la dispach table usando l'offset di id2
    //per poi saltarci
    @Override
    public String visitNode(ClassCallNode n) {
        if (print) printNode(n, n.methodID);
        String argCode = null, getAR = null;
        for (int i = n.argumentList.size()-1; i>=0; i--) argCode = nlJoin(argCode, visit(n.argumentList.get(i))); //lista di stringhe coi nomi degli argomenti dall'ultimo al primo
        for (int i = 0; i < n.nestingLevel-n.stEntry.nl; i++) getAR = nlJoin(getAR, "lw"); //numero di lw uguale alla differenza di nasting level
        return nlJoin(
                "lfp", //carico il Control Link, puntatore che punta alla funzione chiamante
                argCode, //lista di stringhe coi nomi degli argomenti ordinati al contrario
                "lfp", getAR,// fa tanti lw pari al alla differenza di nestingLevel
                "push "+n.stEntry.offset, "add", //trova la classe aggiungendo l'offeset
                "lw",
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm", // duplicate top of stack
                "lw",
                "push " + n.methodStentry.offset, "add", // compute address of "id" declaration
                "lw", // load address of "id" function
                "js"  // jump to popped address (saving address of subsequent instruction in $ra)
        );
    }

    @Override
    public String visitNode(EmptyNode n) {
        if (print) printNode(n);
        return "push " + -1;
    }

    //la new avrà degli argomenti, che userò per farmi diventare i valori dei campi dell oggetto
    //li mettorò quindi nello heap
    //prima prendo tutti gli argomenti che metteno ciscuno il loro valore nello stack
    //poi prendo i valori degli argomenti uno alla volta e il metto nello heap, incrementando hp ogni volta
    //ad indirizzo(memsize+offset) hp infine inserisco il dispach pointer(puntatore alla dispach table)
   //carico sullo stack il valore di hp(object pointer) e lo incremento
    @Override
    public String visitNode(NewNode n) {

        String fieldsOnStack = null;
        String fieldsOnHeap = null;
        String dispatchPointer;

        for(Node param : n.argList) {
            fieldsOnStack = nlJoin(fieldsOnStack,visit(param)); // mettiamo sullo stack tutti i valori dei campi

            // prende i valori degli argomenti, uno alla volta, dallo stack e li
            // mette nello heap, incrementando $hp dopo ogni singola copia
            fieldsOnHeap = nlJoin(fieldsOnHeap,
                    "lhp", // pusho sullo stack il contenuto del registro hp (heap pointer)
                    "sw", // store word: poppo i due valori dalla cima dello stack. Metto il
                    // secondo all'indirizzo puntato dal primo
                    "lhp", // pusho sullo stack il contenuto del registro hp (heap pointer)
                    "push "+ 1, // aggiungo 1
                    "add", // li sommo assieme
                    "shp"); // poppo il valore di hp aumentato (messo sullo stack e sommato ad 1)
                            // e poi lo ricarico nel registro hp
        }
        //inserimento dispach pointer
        dispatchPointer = nlJoin(
                "push " + ( ExecuteVM.MEMSIZE + n.stEntry.offset) , //punto dove deve essere caricato il dispach pointer(AR ambiente gobale + offset)
                "lw", //carico il suo id
                "lhp",
                "sw" // store word: poppo i due valori dalla cima dello stack. Metto il
                // secondo all'indirizzo puntato dal primo
                //non incremento hp perchè è l'object pointer che io devo tornare
                );

        return nlJoin(
                fieldsOnStack, // prima si richiama su tutti gli argomenti in ordine di apparizione
                // (che mettono ciascuno il loro valore calcolato sullo stack)
                fieldsOnHeap, // prende i valori degli argomenti, uno alla volta, dallo stack e li
                // mette nello heap, incrementando $hp dopo ogni singola copia

                dispatchPointer,		  // scrive a indirizzo $hp il dispatch pointer recuperandolo da
                // contenuto indirizzo MEMSIZE + offset classe ID
                "lhp",	// carica sullo stack il valore di $hp (indirizzo object pointer da ritornare) e incrementa $hp
                "lhp", //devo duplicarlo perchè quando lo addo lo tolgo
                "push 1",
                "add",
                "shp" // poppo il valore di hp aumentato (messo sullo stack e sommato ad 1)
                      // e poi lo ricarico nel registro hp
        );
    }
}