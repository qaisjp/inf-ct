package ast;

import java.util.HashMap;

public class VarDecl implements ASTNode {
    public final Type varType;
    public final String varName;
    private String genLabel = null;
    private HashMap<String, String> genStructLabel;
    private Integer genStackOffset = null;

    public VarDecl(Type varType, String varName) {
	    this.varType = varType;
	    this.varName = varName;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitVarDecl(this);
    }

    @Override
    public String toString() {
        return varType.toString() + " " + varName;
    }

    public void setGenStackOffset(int genStackOffset) {
        if (isGlobal()) {
            throw new RuntimeException("Can't set offset of global");
        }
        if (this.genStackOffset != null) {
            throw new RuntimeException("Can't set offset for a single variable declaration multiple times");
        }
        this.genStackOffset = genStackOffset;
    }

    public int getGenStackOffset() {
        if (genStackOffset == null) {
            throw new NullPointerException("can't get genStackOffset when not set");
        }
        return genStackOffset;
    }

    public boolean isGlobal() {
        return (genLabel != null) || isGlobalStruct();
    }

    public boolean isGlobalStruct() {
        return genStructLabel != null;
    }

    public void setGlobalLabel(String label) {
        if (isGlobal()) {
            if (isGlobalStruct()) {
                throw new RuntimeException("Can't set label of a struct");
            } else {
                throw new RuntimeException("Can't set label if already set to " + genLabel);
            }
        }
        genLabel = label;
    }

    public String getGlobalLabel() {
        if (!isGlobal()) {
            throw new NullPointerException("Can't get global label if not a global label (genLabel is null)");
        }
        if (isGlobalStruct()) {
            throw new RuntimeException("Can't get global label of a struct");
        }
        return genLabel;
    }

    public void setStructFieldLabel(String field, String label) {
        // ensure vardecl is actually for a struct
        if (!(varType instanceof StructType)) {
            throw new RuntimeException("this variable declaration must be declaring a struct type");
        }

        // ensure label is non-empty
        if (label.isEmpty()) {
            throw new RuntimeException("label can't be empty");
        }

        // initialise hashmap if this is our first time labelling the struct field
        if (genStructLabel == null) {
            genStructLabel = new HashMap<>();
            for (VarDecl v : ((StructType) varType).decl.varDeclList) {
                // initialise each field label to empty str
                genStructLabel.put(v.varName, "");
            }
        }

        // check if the field exists by seeing if the label map contains the key
        if (!genStructLabel.containsKey(field)) {
            throw new RuntimeException("this struct does not have that field");
        }

        // we do not allow relabelling
        if (!genStructLabel.get(field).isEmpty()) {
            throw new RuntimeException("this struct has that field already labelled");
        }

        genStructLabel.put(field, label);
    }

    // returns label for that field, if the vardecl is a struct
    public String getStructFieldLabel(String field) {
        if (genStructLabel == null) {
            throw new RuntimeException("this vardecl does not declare a struct variable");
        }

        if (!genStructLabel.containsKey(field)) {
            throw new RuntimeException("this struct does not have that field " + field);
        }

        String label = genStructLabel.get(field);

        if (label.isEmpty()) {
            throw new RuntimeException("this struct has the field, but it is not labelled. field is " + field);
        }

        return label;
    }
}
