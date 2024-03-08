package compiler;

import compiler.AST.*;
import compiler.lib.*;

public class TypeRels {

	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {
		return a.getClass().equals(b.getClass()) || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode));
	}

	public static boolean isBoolean(TypeNode e) {
		return e.getClass().equals(BoolTypeNode.class);
	}

	public static boolean isInteger(TypeNode e) {
		return e.getClass().equals(IntTypeNode.class);
	}


}
