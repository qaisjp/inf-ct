package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		return st.structType;
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
					return binOp.type;
				}

				error("Operation %s expects INT and INT, got %s and %s", binOp.op, lhs, rhs);
				break;
			case NE:
			case EQ:
				if (lhs == rhs && !(lhs instanceof StructType) && !(lhs instanceof ArrayType) && lhs != BaseType.VOID) {
					binOp.type = BaseType.INT;
					return binOp.type;
				}

				error("Operation %s expects matching non-void, non-struct, non-array types, got %s and %s", binOp.op, lhs, rhs);
				break;
			default:
				break;
		}
		assert false;
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		// todo
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		// todo
		return null;
	}
}
