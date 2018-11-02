package sem;

import ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		return st.structType.accept(this);
	}

	public boolean returnableStmt(Stmt s) {
		return s instanceof Block || s instanceof If || s instanceof While || s instanceof Return;
	}

	@Override
	public Type visitBlock(Block b) {
		// Visit each variable declaration, statement in our block
		visitEach(b.varDecls);

		// Returned types
		List<Type> returns = new ArrayList<>();

		for (Stmt s : b.stmtList) {
			// Visit each statement
			Type returned = s.accept(this);

			// We only care about the types of blocks (and parents of blocks), oh and return
			if (!returnableStmt(s)) {
				continue;
			}

			// If the type returned is nil then there was no `return` forced, so we ignore this block
			if (returned == null) {
				continue;
			}

//			System.out.printf("Block from %s returns %s\n", s, returned);

			returns.add(returned);
		}

		if (returns.isEmpty()) {
			// If returns is empty, this block doesn't return anything
			return null; // no return anywhere
		} else {
			Type a = null;
			boolean same = true;
			for (Type t : returns) {
				if (a == null) {
					a = t;
					continue;
				} else if (!eq(t, a)) {
					same = false;
					break;
				}
			}
			if (!same) {
				// Ensure all returned types are the same type (if not all equal)
				error("Block returns differing types (%s)\n", Arrays.toString(returns.toArray()));
			}
		}

		// Since we have atleast one return value, make the block return something "random" (first return value)
		return returns.get(0);
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		// If inbuilt function don't declaration validity
		if (p.isInbuilt) {
			return p.type;
		}

		// Visit each parameter (this ensures that none are of type void... and maybe more. Check visitVarDecl!)
		visitEach(p.params);

		Type realReturnType = visitBlock(p.block);

		// If no return was called, assume returning void
		if (realReturnType == null) {
			realReturnType = BaseType.VOID;
		}

		// Make sure the right thing is being returned
		if (!eq(p.type, realReturnType)) {
			error("Function %s returns %s when it should be returning %s\n", p.name, realReturnType, p.type);
		}

		// Should you be able to do `return exprThatReturnsVoid` right?
		// Answer according to gcc: yes

		// Return the type declared in the function declaration
		return p.type;
	}

	@Override
	public Type visitProgram(Program p) {
		visitEach(p.structTypeDecls);
		visitEach(p.varDecls);
		visitEach(p.funDecls);

		// Special magic for no reason at all: return type is return type of main
		for (FunDecl f : p.funDecls) {
			if (f.name.equals("main")) {
				return f.type.accept(this);
			}
		}

		return BaseType.VOID;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		if (vd.varType == BaseType.VOID) {
			error("Cannot declare variable %s as VOID\n", vd.varName);
		}

		return vd.varType;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		v.type = v.vd.varType;
		return v.type;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr f) {
		if (!(f.decl instanceof FunDecl)) {
			String reason = "does not exist";
			if (f.decl != null) {
				reason = "has type " + f.decl.toString();
			}
			error("Could not perform funcall %s%s, %s %s\n", f.name, Arrays.toString(f.exprList.toArray()), f.name, reason);
			return BaseType.VOID;
		}

		List<Expr> args = f.exprList;
		List<VarDecl> params = f.decl.params;

		// Set the return type of this function call to the return type of the decl
		f.type = f.decl.type;

		if (args.size() != params.size()) {
			error("Could not call %s, expected %d arguments, got %d\n", f.decl, params.size(), args.size());
			return f.type;
		}

		for (int i = 0; i < args.size(); i++) {
			Expr arg = args.get(i);
			VarDecl param = params.get(i);

			Type argType = arg.accept(this);
			if (!eq(argType, param.varType)) {
				error("Could not call %s, param `%s` was incorrectly given type %s\n", f.decl, param, argType);
			}
		}

		return f.type;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr te) {
		// Only three valid kinds of typecast
		Type castTo = te.castTo;
		Type castFrom = te.expr.accept(this);

		boolean ok = false;

		if (castFrom == BaseType.CHAR && castTo == BaseType.INT) {
			ok = true;
		} else if (castFrom instanceof ArrayType && castTo instanceof PointerType) {
			ArrayType from = (ArrayType) castFrom;
			PointerType to = (PointerType) castTo;
			ok = eq(from.innerType, to.innerType);
		} else if (castFrom instanceof PointerType && castTo instanceof PointerType) {
			ok = true;
		}

		if (!ok) {
			error("Invalid cast from %s to %s\n", castFrom, castTo);
		} /*else {
			System.out.printf("Casted from %s to %s\n", castFrom, castTo);
		}*/

		te.type = castTo;
		return te.type;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		return BaseType.INT;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		vae.type = vae.expr.accept(this);

		if (!(vae.type instanceof PointerType)) {
			error("Expression should have type PointerType, got %s\n", vae.type);
			return BaseType.VOID;
		}

		return ((PointerType)vae.type).innerType;
	}

	@Override
	public Type visitIntLiteral(IntLiteral f) {
		f.type = BaseType.INT;
		return f.type;
	}

	@Override
	public Type visitStrLiteral(StrLiteral f) {
		f.type = new ArrayType(BaseType.CHAR, f.innerType.length() + 1); // +1 for null byte
		return f.type;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral f) {
		f.type = BaseType.CHAR;
		return f.type;
	}

	@Override
	public Type visitPointerType(PointerType f) {
		f.innerType.accept(this);
		return f;
	}

	@Override
	public Type visitStructType(StructType f) {
		return f;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		return at;
	}

	@Override
	public Type visitWhile(While f) {
		Type e = f.expr.accept(this);
		if (e != BaseType.INT) {
			error("Expression should be of type INT, currently of type %s\n", e);
		}

		Type returned = f.stmt.accept(this);

		return returnableStmt(f.stmt) ? returned : null;
	}

	@Override
	public Type visitIf(If f) {
		Type e = f.expr.accept(this);
		if (e != BaseType.INT) {
			error("Expression should be of type INT, currently of type %s\n", e);
		}

		Type a = f.stmt.accept(this);
		Type returned = returnableStmt(f.stmt) ? a : null;
		if (f.elseStmt != null) {
			Type b = f.elseStmt.accept(this);
			boolean bReturnable = returnableStmt(f.elseStmt);

			// If b exists we need to check for equality
			if (b != null) {
				if (returned == null) {
					// If a is not a returnable statement, we set our If return value appropriately
					returned = bReturnable ? b : null;
				} else if (bReturnable && !eq(a, b)) {
					// If b can return, and a can return (we checked this by checking returned==null)
					// Make sure both return types are equal
					error("stmt and elseStmt return differing types, %s and %s respectively\n", a, b);
				}
			}
		}

		return returned;
	}

	@Override
	public Type visitReturn(Return r) {
		if (r.expr == null) {
			return BaseType.VOID;
		}
		return r.expr.accept(this);
	}

	@Override
	public Type visitExprStmt(ExprStmt exprStmt) {
		return exprStmt.expr.accept(this);
	}

	@Override
	public Type visitAssign(Assign assign) {
		Expr lex = assign.lhs;
		if (!(lex instanceof VarExpr || lex instanceof FieldAccessExpr || lex instanceof ArrayAccessExpr || lex instanceof ValueAtExpr)) {
			error("lvalue cannot be `%s` (must be a variable, field access, array access or pointer dereference)\n", assign.lhs);
		}

		Type lhs = assign.lhs.accept(this);
		Type rhs = assign.rhs.accept(this);

		if (lhs == BaseType.VOID || lhs instanceof ArrayType) {
			error("lvalue cannot be %s\n", lhs);
		}

		if (!eq(lhs, rhs)) {
			error("Type mismatch in assignment (%s != %s)\n", lhs, rhs);
		}

		return null;
	}

	@Override
	public Type visitBinOp(BinOp binOp) {
		Type lhs = binOp.lhs.accept(this);
		Type rhs = binOp.rhs.accept(this);

		switch (binOp.op) {
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case OR:
			case AND:
			case GT:
			case LT:
			case GE:
			case LE:
				if (lhs == BaseType.INT && rhs == BaseType.INT) {
					binOp.type = BaseType.INT;
					return binOp.type.accept(this);
				}

				error("Operation %s expects INT and INT, got %s and %s\n", binOp.op, lhs, rhs);
				return BaseType.INT;
			case NE:
			case EQ:
				if (eq(lhs, rhs) && !(lhs instanceof StructType) && !(lhs instanceof ArrayType) && lhs != BaseType.VOID) {
					binOp.type = BaseType.INT;
					return binOp.type.accept(this);
				}

				error("Operation %s expects matching non-void, non-struct, non-array types, got %s and %s\n", binOp.op, lhs, rhs);
				return BaseType.INT;
			default:
				break;
		}

		assert false;
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		Type expr = arrayAccessExpr.expr.accept(this);
		Type index = arrayAccessExpr.index.accept(this);

		if (index != BaseType.INT) {
			error("Expected INT got %s\n", index);
		}

		if (!(expr instanceof ArrayType) && !(expr instanceof PointerType)) {
			error("Expected ArrayType or PointerType, got %s\n", expr);
			return expr;
		}

		Type innerType = null;
		if (expr instanceof ArrayType) {
			innerType = ((ArrayType) expr).innerType;
		} else if (expr instanceof PointerType) {
			innerType = ((PointerType) expr).innerType;
		} else {
			assert false;
		}

		return innerType.accept(this);
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		Type exprType = fieldAccessExpr.expr.accept(this);

		// Make sure our expression returns a struct
		if (!(exprType instanceof StructType)) {
			error("Expression is not a struct\n");
			return BaseType.VOID;
		}

		StructTypeDecl decl = ((StructType) exprType).decl;

		// First get the variable declaration in the struct
		VarDecl varDecl = null;
		for (VarDecl v : decl.varDeclList) {
			if (v.varName.equals(fieldAccessExpr.string)) {
				varDecl = v;
				break;
			}
		}

		// Check if the varDeclaration exists in the first place
		if (varDecl == null) {
			error("Field %s does not exist in struct\n", fieldAccessExpr.string);
			return BaseType.VOID;
		}

		fieldAccessExpr.type = varDecl.varType.accept(this);
		return fieldAccessExpr.type;
	}
}
