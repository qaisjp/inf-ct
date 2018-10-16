package sem;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	Scope scope;

	public NameAnalysisVisitor() {
		this(new Scope());
	}

	public NameAnalysisVisitor(Scope scope) {
		this.scope = scope;
	}

	@Override
	public Void visitBaseType(BaseType bt) {
		return null; // don't do anything
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
		// Accept the struct type
		sts.structType.accept(this);

		// Backup and create scope
		Scope scope = this.scope;
		this.scope = new Scope(scope);

		// Visit each var decl
		for (VarDecl varDecl : sts.varDeclList) {
			varDecl.accept(this);
		}

		// Revert the scope
		this.scope = scope;

		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		// Store the original scope so we can go back up
		Scope scope = this.scope;

		// Set the new scope with our old one as the parent
		this.scope = new Scope(scope);

		// Visit each variable declaration in our block
		for (VarDecl varDecl : b.varDecls) {
			varDecl.accept(this);
		}

		// Visit each statement in our block
		for (Stmt stmt : b.stmtList) {
			stmt.accept(this);
		}

		// Revert the scope for infinity and beyond
		this.scope = scope;

		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		Symbol s = scope.lookupCurrent(p.name);
		if (s != null) {
			error("Symbol " + p.name + " already exists!");
		} else {
			scope.put(new FunSymbol(p));
		}
		return null;
	}


	@Override
	public Void visitProgram(Program p) {
		// todo
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		Symbol s = scope.lookupCurrent(vd.varName);
		if (s != null) {
			error("Symbol " + vd.varName + " already exists!");
		} else {
			scope.put(new VarSymbol(vd));
		}

		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		return null; // todo
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
