package sem;

import ast.*;

import java.awt.*;
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

	@Override
	public Type visitBlock(Block b) {
		// Visit each variable declaration, statement in our block
		visitEach(b.varDecls);
		visitEach(b.stmtList);

		// todo

		return BaseType.VOID;
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		visitEach(p.params);
		visitBlock(p.block);

		// todo!
		return null;
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
		if (vd.type == BaseType.VOID) {
			error("Cannot declare variable %s as VOID", vd.varName);
		}

		return vd.type;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		v.type = v.vd.type;
		return v.type;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr f) {
		List<Expr> args = f.exprList;
		List<VarDecl> params = f.decl.params;

		// Set the return type of this function call to the return type of the decl
		f.type = f.decl.type;

		if (args.size() != params.size()) {
			error("Could not call %s, expected %d arguments, got %d", f.name, params.size(), args.size());
			return f.type;
		}

		for (int i = 0; i < args.size(); i++) {
			Expr arg = args.get(i);
			VarDecl param = params.get(i);

			Type argType = arg.accept(this);
			if (argType != param.type) {
				error("Could not call %s, arg %d does not match params", f.name, i+1);
			}
		}
		return null;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr te) {
		// Only three valid kinds of typecast
		Type castTo = te.type;
		Type castFrom = te.expr.accept(this);

		boolean ok = false;

		if (castFrom == BaseType.INT && castTo == BaseType.INT) {
			ok = true;
		} else if (castFrom instanceof ArrayType && castTo instanceof PointerType) {
			ArrayType from = (ArrayType) castFrom;
			PointerType to = (PointerType) castTo;

			ok = from.innerType == to.innerType;
		} else if (castFrom instanceof PointerType && castTo instanceof PointerType) {
			ok = true; // Does pointer to pointer mean the inner types aren't checked? todo
		}

		if (!ok) {
			error("Invalid cast from %s to %s", castFrom, castTo);
		}

		return castTo;
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
			error("Expression should be of type INT, currently of type %s", e);
		}

		f.stmt.accept(this);

		return BaseType.INT;
	}

	@Override
	public Type visitIf(If f) {
		Type e = f.expr.accept(this);
		if (e != BaseType.INT) {
			error("Expression should be of type INT, currently of type %s", e);
		}

		f.stmt.accept(this);
		f.elseStmt.accept(this);
		return BaseType.INT;
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
		Type lhs = assign.lhs.accept(this);
		Type rhs = assign.rhs.accept(this);

		if (lhs == BaseType.VOID || lhs instanceof ArrayType) {
			error("lvalue cannot be void or array");
		}

		if (lhs != rhs) {
			error("Type mismatch in assignment");
		}

		return lhs;
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
