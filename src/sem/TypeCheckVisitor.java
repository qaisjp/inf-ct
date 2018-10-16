package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		// To be completed...
		return null;
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
		// To be completed...
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
		return null;
	}

	@Override
	public Type visitIntLiteral(IntLiteral f) {
		// todo
		return null;
	}

	@Override
	public Type visitStrLiteral(StrLiteral f) {
		// todo
		return null;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral f) {
		// todo
		return null;
	}

	@Override
	public Type visitPointerType(PointerType f) {
		// todo
		return null;
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
		// todo
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
