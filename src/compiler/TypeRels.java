package compiler;

import compiler.AST.*;
import compiler.lib.*;

public class TypeRels {

	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {
		// controlliamo che se a è una classe allora anche b lo dovrebbe essere sennò torno false
		if ( isRefType(a) )  {
			if (isRefType(b)){
				return ((RefTypeNode) a).id.equals(((RefTypeNode) b).id);
			} else {
				return false;
			}
		}
		// se a è emptyTypeNode allora b deve essere sovraclasse ovvero RefTypeNode o EmptyTypeNode
		if (a instanceof EmptyTypeNode) {
			return ( isEmptyType(b) || isRefType(b) );
		}

		if ( isInteger(a)|| isBoolean(a) ) {
			// caso in cui a e b sono dello stesso tipo
			return a.getClass().equals(b.getClass()) || (isBoolean(a)) && isInteger(b);
		}
		System.out.println("Type error");
		return false;
	}

	public static boolean isBoolean(TypeNode e) {return e instanceof BoolTypeNode;}

	public static boolean isInteger(TypeNode e) {return e instanceof IntTypeNode;}

	private static boolean isRefType(TypeNode e) {return e instanceof RefTypeNode;}

	private static boolean isEmptyType(TypeNode e) {return e instanceof EmptyTypeNode;}


}
