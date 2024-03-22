# FOOL PROJECT
An Object-Oriented Compiler, made for the course "Languages,compilers and computational models" (MASTER'S DEGREE IN 
ENGINEERING AND COMPUTER SCIENCE).


## add antlr-runtime-4.12.0 
- File --> project structure --> modules
- Tab Dependencies
- Click to "+" (downside)
- Select "Library" then "antlr-runtime-4.12.0"
- Click "antlr-runtime-4.12.0" and then ok

## Submission information
l codice presentato deve essere sviluppato da voi in autonomia (essere diverso da quello sviluppato dagli altri gruppi).
Una volta consegnato il progetto ci metteremo d'accordo per una data di discussione nel mio ufficio a cui devono 
partecipare tutti i componenti del gruppo. Per la discussione mi raccomando di riguardare bene tutto il codice da voi
sviluppato in modo da essere in grado di descriverlo (e collegarlo alle cose spiegate a lezione).

Per la consegna inviate una mail a lorenzo.bacchiani2@unibo.it e mario.bravetti@unibo.it, dall'indirizzo istituzionale
di uno di voi, mettendo in copia gli indirizzi istituzionali di tutti gli altri. Allegate alla mail il file 
"Cognome1Cognome2...CognomeN.zip" del progetto (Cognome1, Cognome2, ..., CognomeN sono i cognomi degli N componenti 
del gruppo in qualsiasi ordine), che contenga i 7/8 files indicati nelle istruzioni per la realizzazione del progetto
(messaggio che vi avevo già inviato). In particolare fate attenzione a non mettere nello zip files .class o .jar, 
altrimenti la vostra mail viene bloccata dal server di posta per motivi di sicurezza.

Indicate nella mail:

- il livello di sviluppo del vostro progetto, cioè: senza/con object orientation (indicando, in questo caso, quale 
- dei 3 livelli avete realizzato)

- per ciascuno di voi, la data in cui ha superato lo scritto

Vi suggerisco, infine, di testare il vostro progetto con i file di prova inclusi nelle specifiche (quelli che si 
possono utilizzare, a seconda del livello di sviluppo del vostro progetto; in particolare bankloan.fool deve stampare 
50000). Se per il vostro livello non vi sono files di prova includete un file ".fool" di prova del vostro progetto 
nella directory principale dello zip.

## File da consegnare
In particolare la consegna del progetto consiste nell'invio dei seguenti 7 files del package "compiler", ottenuti
estendendo i corrispondenti files nella directory "versione compilatore finale":
---

AST.java

ASTGenerationSTVisitor.java

SymbolTableASTVisitor.java

PrintEASTVisitor.java

TypeCheckEASTVisitor.java

TypeRels.java

CodeGenerationASTVisitor.java

---

Inoltre, solo qualora NON sia stata realizzata una estensione di FOOL che comprenda l'Object Orientation con
ereditarietà, deve essere inviato anche il file FOOL.g4. Tale file deve essere ottenuto eliminando le parti del 
linguaggio non realizzate dal FOOL.g4 fornitovi per il linguaggio completo FOOL ("Specifica lessicale e sintattica 
del linguaggio FOOL" su Virtuale).

Il compilatore realizzato deve quindi funzionare mettendo i 7 files sopra da consegnare (più eventualmente FOOL.g4)
insieme agli altri files nella directory "versione compilatore finale" (files "STentry.java" e "Test.java" del package
"compiler" e i files di tutti gli altri package), che quindi non dovete modificare.

L'unica eccezione è il file "BaseASTVisitor.java": la versione di tale file da utilizzare per il linguaggio FOOL
completo vi viene fornita ("BaseASTVisitor da usare per lo sviluppo del compilatore di FOOL" su Virtuale).
Qualora realizziate una estensione parziale che non contenga tutti i Node della versione completa basta che commentiate 
i relativi metodi di visita in tale file "BaseASTVisitor.java".

Vi faccio inoltre presente che, nello sviluppo del progetto, il punto non sono tanto le questioni "stilistiche" 
nella scrittura del codice/sua strutturazione. Il punto è, piuttosto, che abbiate capito i concetti relativi allo 
sviluppo del compilatore spiegati a lezione (sue fasi e strutture dati con cui gestirle, ottenute estendendo quelle 
fatte a lezione) ed è su quello che vi dovete concentrare e su cui verrete valutati. Ciò è l'aspetto rilevante dal 
punto di vista didattico. La prova di progetto sarà quindi, prima di tutto, una prova orale sulla parte di compilatori 
delle lezioni: tramite il vostro progetto mostretete inoltre di avere acquisito padronanza dei concetti spiegati e 
di essere in grado di sviluppare autonomamente (a livello di gruppo) codice sulla base di questi. Per la valutazione 
è quindi preferibile piuttosto consegnare un progetto svolto a un livello inferiore, ma che il codice che mi presentiate 
risulti davvero frutto del vostro lavoro autonomo di gruppo.

