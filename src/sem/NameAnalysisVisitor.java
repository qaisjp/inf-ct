package sem;

import ast.*;

import java.util.List;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	Scope scope;

	public NameAnalysisVisitor() {
		this(new Scope());
	}

	public NameAnalysisVisitor(Scope scope) {
		this.scope = scope;
	}

	public void visitEach(List<? extends ASTNode> list) {
		for (ASTNode l : list) {
			l.accept(this);
		}
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
		visitEach(sts.varDeclList);

		// Revert the scope
		this.scope = scope;

		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		return visitBlock(b, true);
	}

	public Void visitBlock(Block b, boolean newScope) {
		if (newScope) {
			// Store the original scope so we can go back up
			Scope scope = this.scope;

			// Set the new scope with our old one as the parent
			this.scope = new Scope(scope);
		}

		// Visit each variable declaration, statement in our block
		visitEach(b.varDecls);
		visitEach(b.stmtList);

		if (newScope) {
			// Revert the scope for infinity and beyond
			this.scope = scope;
		}

		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		Symbol s = scope.lookupCurrent(p.name);
		if (s != null) {
			error("Symbol " + p.name + " already exists!");
			return null;
		}

		scope.put(new FunSymbol(p));

		Scope scope = this.scope;
		this.scope = new Scope(scope);

		visitEach(p.params);
		visitBlock(p.block, false);

		return null;
	}


	@Override
	public Void visitProgram(Program p) {
		visitEach(p.structTypeDecls);
		visitEach(p.varDecls);
		visitEach(p.funDecls);
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
		// Check the symbol exists & grab symbol
		Symbol s = scope.lookup(f.name);

		if (s == null) {
			// Error if it doesn't exist
			error("Symbol " + f.name + " does not exist!");
		} else if (!s.isFun()) {
			// Error if it's not a function
			error(f.name + " is not a function");
		} else {
			// Link function call to declaration of function
			f.decl = ((FunSymbol) s).funDecl;
		}

		// Visit the argument expressions
		visitEach(f.exprList);
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr te) {
		te.expr.accept(this);
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr soe) {
		soe.typeToCheck.accept(this);
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		vae.expr.accept(this);
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral f) {
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral f) {
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral f) {
		return null;
	}

	@Override
	public Void visitPointerType(PointerType f) {
		f.innerType.accept(this);
		return null;
	}

	@Override
	public Void visitStructType(StructType structType) {
		Symbol s = scope.lookup(structType.str);
		if (s == null) {
			error("Struct " + structType.str + " does not exist!");
			return null;
		}
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		at.innerType.accept(this);
		return null;
	}

	@Override
	public Void visitWhile(While f) {
		f.expr.accept(this);
		f.stmt.accept(this);
		return null;
	}

	@Override
	public Void visitIf(If f) {
		f.expr.accept(this);
		f.stmt.accept(this); // todo: check validity
		f.elseStmt.accept(this);
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		if (r.expr != null) {
			r.expr.accept(this);
		}
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt exprStmt) {
		exprStmt.expr.accept(this);
		return null;
	}

	@Override
	public Void visitAssign(Assign assign) {
		assign.lhs.accept(this);
		assign.rhs.accept(this);
		return null;
	}

	@Override
	public Void visitBinOp(BinOp binOp) {
		binOp.lhs.accept(this);
		binOp.rhs.accept(this);
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		arrayAccessExpr.lhs.accept(this);
		arrayAccessExpr.rhs.accept(this);
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		fieldAccessExpr.expr.accept(this);
		/*
		todo: fieldAccessExpr.string is the field to access.
		todo: at some point you also need to make sure that the field to access
		todo: even exists in the struct (probably typechecker)
		*/
		return null;
	}
}
