package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		return st.structType.accept(this);
	}

	@Override
	public Type visitBlock(Block b) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitProgram(Program p) {
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr f) {
		// todo
		return null;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr te) {
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		return null;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		// todo: this is wrong. you need to make sure it's a PointerType and then return the inner type
		vae.type = vae.expr.accept(this);
		return vae.type;
	}

	@Override
	public Type visitIntLiteral(IntLiteral f) {
		f.type = BaseType.INT;
		return f.type;
	}

	@Override
	public Type visitStrLiteral(StrLiteral f) {
		f.type = new PointerType(BaseType.CHAR);
		return f.type;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral f) {
		f.type = BaseType.CHAR;
		return f.type;
	}

	@Override
	public Type visitPointerType(PointerType f) {
		return f.innerType.accept(this);
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
		// todo
		return null;
	}

	@Override
	public Type visitIf(If f) {
		// todo
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		// todo
		return null;
	}

	@Override
	public Type visitExprStmt(ExprStmt exprStmt) {
		// todo
		return null;
	}

	@Override
	public Type visitAssign(Assign assign) {
		// todo
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

				error("Operation %s expects INT and INT, got %s and %s", binOp.op, lhs, rhs);
				return BaseType.INT;
			case NE:
			case EQ:
				if (lhs == rhs && !(lhs instanceof StructType) && !(lhs instanceof ArrayType) && lhs != BaseType.VOID) {
					binOp.type = BaseType.INT;
					return binOp.type.accept(this);
				}

				error("Operation %s expects matching non-void, non-struct, non-array types, got %s and %s", binOp.op, lhs, rhs);
				return BaseType.INT;
			default:
				break;
		}
		assert false;
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		Type lhs = arrayAccessExpr.lhs.accept(this);
		Type rhs = arrayAccessExpr.rhs.accept(this);

		if (rhs != BaseType.INT) {
			error("Expected INT got %s", rhs);
		}

		if (!(lhs instanceof ArrayType) && !(lhs instanceof PointerType)) {
			error("Expected ArrayType or PointerType, got something else");
			return lhs;
		}

		Type innerType = null;
		if (lhs instanceof ArrayType) {
			innerType = ((ArrayType) lhs).innerType;
		} else if (lhs instanceof PointerType) {
			innerType = ((PointerType) lhs).innerType;
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
			error("Expression is not a struct");
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
			error("Field %s does not exist in struct", fieldAccessExpr.string);
			return BaseType.VOID;
		}

		fieldAccessExpr.type = varDecl.type.accept(this);
		return fieldAccessExpr.type;
	}
}
