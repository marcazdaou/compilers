package jminusminus;

import static jminusminus.CLConstants.IADD;
import static jminusminus.CLConstants.LADD;
import static jminusminus.CLConstants.DADD;
import static jminusminus.CLConstants.DSUB;
import static jminusminus.CLConstants.LSUB;
import static jminusminus.CLConstants.ISUB;
import static jminusminus.CLConstants.DMUL;
import static jminusminus.CLConstants.IMUL;
import static jminusminus.CLConstants.LMUL;
import static jminusminus.CLConstants.DDIV;
import static jminusminus.CLConstants.LDIV;
import static jminusminus.CLConstants.IDIV;
import static jminusminus.CLConstants.LREM;
import static jminusminus.CLConstants.DREM;
import static jminusminus.CLConstants.IREM;


/**
 * This abstract base class is the AST node for an assignment operation.
 */
abstract class JAssignment extends JBinaryExpression {
    /**
     * Constructs an AST node for an assignment operation.
     *
     * @param line     line in which the assignment operation occurs in the source file.
     * @param operator the assignment operator.
     * @param lhs      the lhs operand.
     * @param rhs      the rhs operand.
     */
    public JAssignment(int line, String operator, JExpression lhs, JExpression rhs) {
        super(line, operator, lhs, rhs);
    }
}

/**
 * The AST node for an assignment (=) operation.
 */
class JAssignOp extends JAssignment {
    /**
     * Constructs the AST node for an assignment (=) operation.
     *
     * @param line line in which the assignment operation occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JAssignOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(lhs instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line(), "illegal lhs for assignment");
        } else {
            lhs = ((JLhs) lhs).analyzeLhs(context);
            rhs = rhs.analyze(context);
            rhs.type().mustMatchExpected(line(), lhs.type());
            type = rhs.type();
            if (lhs instanceof JVariable) {
                Defn defn = ((JVariable) lhs).iDefn();
                if (defn != null) {
                    // Local variable; consider it to be initialized now.
                    ((LocalVariableDefn) defn).initialize();
                }
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        ((JLhs) lhs).codegenLoadLhsLvalue(output);
        rhs.codegen(output);
        if (!isStatementExpression) {
            ((JLhs) lhs).codegenDuplicateRvalue(output);
        }
        ((JLhs) lhs).codegenStore(output);
    }
}

/**
 * The AST node for a plus-assign (+=) operation.
 */
class JPlusAssignOp extends JAssignment {
    /**
     * Constructs the AST node for a plus-assign (+=) operation.
     *
     * @param line line in which the assignment operation occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JPlusAssignOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "+=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(lhs instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line(), "illegal lhs for assignment");
            return this;
        } else {
            lhs = ((JLhs) lhs).analyzeLhs(context);
        }
        rhs = rhs.analyze(context);
        if (lhs.type().equals(Type.STRING)) {
            rhs = (new JStringConcatenationOp(line, lhs, rhs)).analyze(context);
            type = Type.STRING;
        } else {

            if (lhs.type() == Type.INT) {
                rhs.type().mustMatchExpected(line(), lhs.type());
                type = Type.INT;
            }
            else if (lhs.type() == Type.LONG) {
                rhs.type().mustMatchExpected(line(), lhs.type());
                type = Type.LONG;
            }
            else if (lhs.type() == Type.DOUBLE) {
                rhs.type().mustMatchExpected(line(), lhs.type());
                type = Type.DOUBLE;
            }            
        }
        return this;
    }


    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        ((JLhs) lhs).codegenLoadLhsLvalue(output);
        if (lhs.type().equals(Type.STRING)) {
            rhs.codegen(output);
        } else if (lhs.type().equals(Type.INT)) {
            ((JLhs) lhs).codegenLoadLhsRvalue(output);
            rhs.codegen(output);
            output.addNoArgInstruction(IADD);
        } else if (lhs.type().equals(Type.LONG)) {
            ((JLhs) lhs).codegenLoadLhsRvalue(output);
            rhs.codegen(output);
            output.addNoArgInstruction(LADD);
        } else if (lhs.type().equals(Type.DOUBLE)) {
            ((JLhs) lhs).codegenLoadLhsRvalue(output);
            rhs.codegen(output);
            output.addNoArgInstruction(DADD);
        }
        if (!isStatementExpression) {
            ((JLhs) lhs).codegenDuplicateRvalue(output);
        }
        ((JLhs) lhs).codegenStore(output);
    }
}

/**
 * The AST node for a minus-assign (-=) operation.
 */
class JMinusAssignOp extends JAssignment {
    /**
     * Constructs the AST node for a minus-assign operation.
     *
     * @param line line in which the assignment operation occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JMinusAssignOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "-=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // Make sure lhs is a valid assignment target
        if (!(lhs instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line(), "Left side of -= should be a variable");
            return this;
        }

        lhs = ((JLhs) lhs).analyzeLhs(context); // Analyze left side
        rhs = rhs.analyze(context);             // Analyze right side

        // Match types and set resulting type
        if (lhs.type().equals(Type.INT)) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type().equals(Type.LONG)) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type().equals(Type.DOUBLE)) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        } else {
            JAST.compilationUnit.reportSemanticError(line(), "Type " + lhs.type() + " not valid for -=");
        }

        return this;
    }

    // Generates bytecode for -= operation
    public void codegen(CLEmitter output) {
        // Load the address of lhs
        ((JLhs) lhs).codegenLoadLhsLvalue(output);

        // Load the current value of lhs and value of rhs, then subtract
        ((JLhs) lhs).codegenLoadLhsRvalue(output);
        rhs.codegen(output);

        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(ISUB);
        } else if (type.equals(Type.LONG)) {
            output.addNoArgInstruction(LSUB);
        } else {
            output.addNoArgInstruction(DSUB);
        }

        // If the result is used in an expression, duplicate it
        if (!isStatementExpression) {
            ((JLhs) lhs).codegenDuplicateRvalue(output);
        }

        // Store the result back into lhs
        ((JLhs) lhs).codegenStore(output);
    }
}
    

/**
 * The AST node for a star-assign (*=) operation.
 */
class JStarAssignOp extends JAssignment {
    /**
     * Constructs the AST node for a star-assign operation.
     *
     * @param line line in which the assignment operation occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JStarAssignOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "*=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(lhs instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line(), "Left side of *= must be a variable");
            return this;
        }

        lhs = ((JLhs) lhs).analyzeLhs(context);
        rhs = rhs.analyze(context);

        if (lhs.type().equals(Type.INT)) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type().equals(Type.LONG)) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type().equals(Type.DOUBLE)) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        } else {
            JAST.compilationUnit.reportSemanticError(line(), "Invalid type for *=: " + lhs.type());
        }

        return this;
    }

    // Generate bytecode for *= operation
    public void codegen(CLEmitter output) {
        ((JLhs) lhs).codegenLoadLhsLvalue(output);
        ((JLhs) lhs).codegenLoadLhsRvalue(output);
        rhs.codegen(output);

        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(IMUL);
        } else if (type.equals(Type.LONG)) {
            output.addNoArgInstruction(LMUL);
        } else {
            output.addNoArgInstruction(DMUL);
        }

        if (!isStatementExpression) {
            ((JLhs) lhs).codegenDuplicateRvalue(output);
        }

        ((JLhs) lhs).codegenStore(output);
    }
}

/**
 * The AST node for a div-assign (/=) operation.
 */
class JDivAssignOp extends JAssignment {
    /**
     * Constructs the AST node for a div-assign operation.
     *
     * @param line line in which the assignment operation occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JDivAssignOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "/=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(lhs instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line(), "Left side of /= should be a variable");
            return this;
        }

        lhs = ((JLhs) lhs).analyzeLhs(context);
        rhs = rhs.analyze(context);

        if (lhs.type().equals(Type.INT)) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type().equals(Type.LONG)) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type().equals(Type.DOUBLE)) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        } else {
            JAST.compilationUnit.reportSemanticError(line(), "Invalid type for /=: " + lhs.type());
        }

        return this;
    }

    // Generate bytecode for /= operation
    public void codegen(CLEmitter output) {
        ((JLhs) lhs).codegenLoadLhsLvalue(output);
        ((JLhs) lhs).codegenLoadLhsRvalue(output);
        rhs.codegen(output);

        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(IDIV);
        } else if (type.equals(Type.LONG)) {
            output.addNoArgInstruction(LDIV);
        } else {
            output.addNoArgInstruction(DDIV);
        }

        if (!isStatementExpression) {
            ((JLhs) lhs).codegenDuplicateRvalue(output);
        }

        ((JLhs) lhs).codegenStore(output);
    }
}
/**
 * The AST node for a rem-assign (%=) operation.
 */
class JRemAssignOp extends JAssignment {
    /**
     * Constructs the AST node for a rem-assign operation.
     *
     * @param line line in which the assignment operation occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JRemAssignOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "%=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(lhs instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line(), "Left side of %= must be a variable");
            return this;
        }

        lhs = ((JLhs) lhs).analyzeLhs(context);
        rhs = rhs.analyze(context);

        if (lhs.type().equals(Type.INT)) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type().equals(Type.LONG)) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type().equals(Type.DOUBLE)) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        } else {
            JAST.compilationUnit.reportSemanticError(line(), "Invalid type for %=: " + lhs.type());
        }

        return this;
    }

    // Generate bytecode for %= operation
    public void codegen(CLEmitter output) {
        ((JLhs) lhs).codegenLoadLhsLvalue(output);      // Load where to store
        ((JLhs) lhs).codegenLoadLhsRvalue(output);      // Load current lhs value
        rhs.codegen(output);                            // Load rhs value

        // Do the correct remainder operation based on type
        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(IREM);
        } else if (type.equals(Type.LONG)) {
            output.addNoArgInstruction(LREM);
        } else {
            output.addNoArgInstruction(DREM);
        }

        // If it's part of a larger expression, duplicate the result
        if (!isStatementExpression) {
            ((JLhs) lhs).codegenDuplicateRvalue(output);
        }

        // Store result back into lhs
        ((JLhs) lhs).codegenStore(output);
    }
}