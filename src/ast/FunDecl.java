package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FunDecl implements ASTNode {
    public final Type type;
    public final String name;
    public final List<VarDecl> params;
    public final Block block;
    public final boolean isInbuilt;

    public FunDecl(Type type, String name, List<VarDecl> params, Block block) {
	    this.type = type;
	    this.name = name;
	    this.params = params;
	    this.block = block;
	    this.isInbuilt = false;
    }

    // Inbuilts only
    public FunDecl(Type type, String name, Type maybeParam) {
        this.type = type;
        this.name = name;

        this.params = new ArrayList<>();
        if (maybeParam != null) {
            params.add(new VarDecl(maybeParam, "unk"));
        }

        this.block = new Block(Collections.emptyList(), Collections.emptyList());
        this.isInbuilt = true;
    }

    public <T> T accept(ASTVisitor<T> v) {
	return v.visitFunDecl(this);
    }

    @Override
    public String toString() {
        return this.name + Arrays.toString(params.toArray());
    }
}
