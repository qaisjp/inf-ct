package sem;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	Scope scope;

	public NameAnalysisVisitor(Scope scope) {
		this.scope = scope;
	}

	@Override
	public Void visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		// To be completed...
		return null;
	}


	@Override
	public Void visitProgram(Program p) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr f) {
		// todo
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr te) {
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr soe) {
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral f) {
		// todo
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral f) {
		// todo
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral f) {
		// todo
		return null;
	}

	@Override
	public Void visitPointerType(PointerType f) {
		// todo
		return null;
	}

	@Override
	public Void visitStructType(StructType f) {
		// todo
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		return null;
	}

	@Override
	public Void visitWhile(While f) {
		// todo
		return null;
	}

	@Override
	public Void visitIf(If f) {
		// todo
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		// todo
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt exprStmt) {
		// todo
		return null;
	}

	@Override
	public Void visitAssign(Assign assign) {
		// todo
		return null;
	}

	@Override
	public Void visitBinOp(BinOp binOp) {
		// todo
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		// todo
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		// todo
		return null;
	}

	// To be completed...


}
