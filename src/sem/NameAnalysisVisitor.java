package sem;

import ast.*;

import java.util.Collections;
import java.util.List;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	Scope scope;

	public NameAnalysisVisitor() {
		this(new Scope());
	}

	public NameAnalysisVisitor(Scope scope) {
		this.scope = scope;
	}

	// symbolDeclare will tell the caller if you can declare this symbol here
	public boolean symbolDeclare(String name, boolean isStruct) {
		Symbol s = scope.lookupCurrent(name, isStruct);
		if (s != null) {
			error("Symbol %s already exists!\n", name);
			return false;
		}
//		System.out.printf("%s is declared %s\n", name, isStruct ? "as struct" : "");
		return true;
	}

	public boolean symbolDeclare(String name) {
	    return symbolDeclare(name, false);
    }

	// symbolRequest will request for a symbol of a certain type
	public <S extends Symbol> S symbolRequest(String name, Class<S> ofClass, boolean isStruct) {
		// Developer note:
		// An example of a similar method that derives the return type based
		// on a type parameter is member method Class.getAnnotation
		// (jump using ofClass.getAnnotation)
		//
		// More accurately, the parameter is derived
		// from the return type, but potatoh-potahto.

		// Check the symbol exists & grab symbol
		Symbol s = scope.lookup(name, isStruct);

		// Error if it doesn't exist
		if (s == null) {
			error("Symbol %s does not exist!\n", name);
			return null;
		}

		if (!ofClass.isInstance(s)) {
			error("%s is not a %s\n", name, ofClass);
			return null;
		}

		return ofClass.cast(s);
	}

	public <S extends Symbol> S symbolRequest(String name, Class<S> ofClass) {
	    return symbolRequest(name, ofClass, false);
    }

	@Override
	public Void visitBaseType(BaseType bt) {
		return null; // don't do anything
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
		if (!symbolDeclare(sts.structType.str, true)) {
			return null;
		}

		scope.putStruct(new StructSymbol(sts));

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

	public Void visitBlock(Block b, final boolean newScope) {
		Scope scope = null;

		if (newScope) {
			// Store the original scope so we can go back up
			scope = this.scope;

			// Set the new scope with our old one as the parent
			this.scope = new Scope(scope);
		}

		// Visit each variable declaration, statement in our block
		visitEach(b.varDecls);
		visitEach(b.stmtList);

		if (newScope) {
			assert scope != null;

			// Revert the scope for infinity and beyond
			this.scope = scope;
		}

		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		if (!symbolDeclare(p.name)) {
			return null;
		}

		scope.put(new FunSymbol(p));

		Scope scope = this.scope;
		this.scope = new Scope(scope);

		p.result.accept(this);
		visitEach(p.params);
		visitBlock(p.block, false);

		// Revert scope
		this.scope = scope;

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
		if (!symbolDeclare(vd.varName)) {
			return null;
		}

		vd.varType.accept(this);

		scope.put(new VarSymbol(vd));

		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		VarSymbol s = symbolRequest(v.name, VarSymbol.class);

		if (s != null) {
			// Link variable expression to the variable type
			v.vd = s.vd;
		}

		// Dummy link
		if (v.vd == null) {
			v.vd = new VarDecl(BaseType.VOID, v.name);
		}

		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr f) {
		// Check the symbol exists & grab symbol
		FunSymbol s = symbolRequest(f.name, FunSymbol.class);

		if (s != null) {
			// Link function call to declaration of function
			f.decl = s.funDecl;
		}

		// Dummy link
		if (f.decl == null) {
			List empty = Collections.emptyList();
			f.decl = new FunDecl(BaseType.VOID, f.name, empty, new Block(empty, empty));
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
		StructSymbol s = symbolRequest(structType.str, StructSymbol.class, true);

		if (s != null) {
			// Link function call to declaration of function
			structType.decl = s.decl;
		}

		// Dummy link
		if (structType.decl == null) {
			structType.decl = new StructTypeDecl(new StructType(structType.str), Collections.emptyList());
		}

		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		at.elemType.accept(this);
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
		f.stmt.accept(this);
		if (f.elseStmt != null) {
			f.elseStmt.accept(this);
		}
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
		binOp.x.accept(this);
		binOp.y.accept(this);
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		arrayAccessExpr.expr.accept(this);
		arrayAccessExpr.index.accept(this);
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		fieldAccessExpr.expr.accept(this);
		return null;
	}
}
