package jminusminus;

import static jminusminus.CLConstants.AALOAD;
import static jminusminus.CLConstants.AASTORE;
import static jminusminus.CLConstants.BALOAD;
import static jminusminus.CLConstants.BASTORE;
import static jminusminus.CLConstants.CALOAD;
import static jminusminus.CLConstants.CASTORE;
import static jminusminus.CLConstants.DALOAD;
import static jminusminus.CLConstants.DUP2;
import static jminusminus.CLConstants.DUP2_X1;
import static jminusminus.CLConstants.DUP_X2;
import static jminusminus.CLConstants.IALOAD;
import static jminusminus.CLConstants.IASTORE;
import static jminusminus.CLConstants.IFEQ;
import static jminusminus.CLConstants.IFNE;
import static jminusminus.CLConstants.LALOAD;
import static jminusminus.CLConstants.LASTORE;
import static jminusminus.CLConstants.DASTORE;
/**
 * The AST for an array indexing operation. It has an expression denoting an array object and an expression denoting
 * an integer index.
 */
class JArrayExpression extends JExpression implements JLhs {
    // The array.
    private JExpression theArray;

    // The array index expression.
    private JExpression indexExpr;

    /**
     * Constructs an AST node for an array indexing operation.
     *
     * @param line      line in which the operation occurs in the source file.
     * @param theArray  the array we're indexing.
     * @param indexExpr the index expression.
     */
    public JArrayExpression(int line, JExpression theArray, JExpression indexExpr) {
        super(line);
        this.theArray = theArray;
        this.indexExpr = indexExpr;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        theArray = theArray.analyze(context);
        indexExpr = indexExpr.analyze(context);
        if (!(theArray.type().isArray())) {
            JAST.compilationUnit.reportSemanticError(line(), "attempt to index a non-array object");
            this.type = Type.ANY;
        }
        indexExpr.type().mustMatchExpected(line(), Type.INT);
        type = theArray.type().componentType();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyzeLhs(Context context) {
        analyze(context);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
       // Generate code to load the array and index onto the stack
    theArray.codegen(output);
    indexExpr.codegen(output);

    // Load the correct array element based on its type
    if (type == Type.INT) {
        output.addNoArgInstruction(IALOAD);
    } else if (type == Type.LONG) {
        output.addNoArgInstruction(LALOAD);
    } else if (type == Type.DOUBLE) {
        output.addNoArgInstruction(DALOAD);
    } else if (type == Type.BOOLEAN) {
        output.addNoArgInstruction(BALOAD);
    } else if (type == Type.CHAR) {
        output.addNoArgInstruction(CALOAD);
    } else if (!type.isPrimitive()) {
        output.addNoArgInstruction(AALOAD); // For objects or non-primitives
    }
}
    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        codegen(output);
        output.addBranchInstruction(onTrue ? IFNE : IFEQ, targetLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void codegenLoadLhsLvalue(CLEmitter output) {
        theArray.codegen(output);
        indexExpr.codegen(output);
    }

    /**
     * {@inheritDoc}
     */
    public void codegenLoadLhsRvalue(CLEmitter output) {
        // Duplicate the array and index on the stack. Use DUP2_X1 for String to keep stack order correct.
    output.addNoArgInstruction(type == Type.STRING ? DUP2_X1 : DUP2);

    // Load the value at the given index based on its type
    if (type == Type.INT) {
        output.addNoArgInstruction(IALOAD);
    } else if (type == Type.LONG) {
        output.addNoArgInstruction(LALOAD);
    } else if (type == Type.DOUBLE) {
        output.addNoArgInstruction(DALOAD);
    } else if (type == Type.BOOLEAN) {
        output.addNoArgInstruction(BALOAD);
    } else if (type == Type.CHAR) {
        output.addNoArgInstruction(CALOAD);
    } else if (!type.isPrimitive()) {
        output.addNoArgInstruction(AALOAD); // For objects
    }
}

    /**
     * {@inheritDoc}
     */
    public void codegenDuplicateRvalue(CLEmitter output) {
        output.addNoArgInstruction(DUP_X2);
    }

    /**
     * {@inheritDoc}
     */
    public void codegenStore(CLEmitter output) {
        if (type == Type.INT) {
            output.addNoArgInstruction(IASTORE);
        } else if (type == Type.LONG) {
            output.addNoArgInstruction(LASTORE);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(DASTORE);
        } else if (type == Type.BOOLEAN) {
            output.addNoArgInstruction(BASTORE);
        } else if (type == Type.CHAR) {
            output.addNoArgInstruction(CASTORE);
        } else if (!type.isPrimitive()) {
            output.addNoArgInstruction(AASTORE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JArrayExpression:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("TheArray", e1);
        theArray.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("TheIndex", e2);
        indexExpr.toJSON(e2);
    }
}