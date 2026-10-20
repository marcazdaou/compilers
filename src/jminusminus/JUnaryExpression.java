package jminusminus;

import static jminusminus.CLConstants.LNEG;  // lneg
import static jminusminus.CLConstants.DNEG;   // dneg 
import static jminusminus.CLConstants.GOTO;
import static jminusminus.CLConstants.IADD;
import static jminusminus.CLConstants.ICONST_0;
import static jminusminus.CLConstants.ICONST_1;
import static jminusminus.CLConstants.INEG;
import static jminusminus.CLConstants.ISUB;

/**
 * This abstract base class is the AST node for a unary expression --- an expression with a single operand.
 */
abstract class JUnaryExpression extends JExpression {
    /**
     * The unary operator.
     */
    protected String operator;

    /**
     * The operand.
     */
    protected JExpression operand;

    /**
     * Constructs an AST node for a unary expression.
     *
     * @param line     line in which the unary expression occurs in the source file.
     * @param operator the unary operator.
     * @param operand  the operand.
     */
    protected JUnaryExpression(int line, String operator, JExpression operand) {
        super(line);
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JUnaryExpression:" + line, e);
        e.addAttribute("operator", operator);
        e.addAttribute("type", type == null ? "" : type.toString());
        JSONElement e1 = new JSONElement();
        e.addChild("Operand", e1);
        operand.toJSON(e1);
    }
}

/**
 * The AST node for a logical NOT (!) expression.
 */
class JLogicalNotOp extends JUnaryExpression {
    /**
     * Constructs an AST for a logical NOT expression.
     *
     * @param line line in which the logical NOT expression occurs in the source file.
     * @param arg  the operand.
     */
    public JLogicalNotOp(int line, JExpression arg) {
        super(line, "!", arg);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        operand = operand.analyze(context);
        operand.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String falseLabel = output.createLabel();
        String trueLabel = output.createLabel();
        this.codegen(output, falseLabel, false);
        output.addNoArgInstruction(ICONST_1); // true
        output.addBranchInstruction(GOTO, trueLabel);
        output.addLabel(falseLabel);
        output.addNoArgInstruction(ICONST_0); // false
        output.addLabel(trueLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        operand.codegen(output, targetLabel, !onTrue);
    }
}

/**
 * The AST node for a unary negation (-) expression.
 */
class JNegateOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a negation expression.
     *
     * @param line    line in which the negation expression occurs in the source file.
     * @param operand the operand.
     */
    public JNegateOp(int line, JExpression operand) {
        super(line, "-", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        operand = operand.analyze(context);

        if (operand.type() == Type.INT) {
            type = Type.INT;
        } else if (operand.type() == Type.LONG) {
            type = Type.LONG;
        } else if (operand.type() == Type.DOUBLE) {
            type = Type.DOUBLE;
        } else {
            type = Type.ANY; // Optional: flag as invalid or unexpected
        }

        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        operand.codegen(output);

        if (type == Type.INT) {
            output.addNoArgInstruction(INEG);
        } else if (type == Type.LONG) {
            output.addNoArgInstruction(LNEG);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(DNEG);
        }
    }
}

/**
 * The AST node for a post-decrement (--) expression.
 */
class JPostDecrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a post-decrement expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPostDecrementOp(int line, JExpression operand) {
        super(line, "-- (post)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "Operand to -- meeds LValue.");
            type = Type.ANY;
        } else {
            operand = operand.analyze(context);
            operand.type().mustMatchExpected(line, Type.INT);
            type = Type.INT;
        }
        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        if (operand instanceof JVariable) {
            // Local variable case
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            if (!isStatementExpression) {
                operand.codegen(output); // Load original value
            }
            output.addIINCInstruction(offset, -1);
        } else {
            // General LHS case
            JLhs lhs = (JLhs) operand;
            lhs.codegenLoadLhsLvalue(output);
            lhs.codegenLoadLhsRvalue(output);
            if (!isStatementExpression) {
                lhs.codegenDuplicateRvalue(output); // Duplicate before modification
            }
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(ISUB);
            lhs.codegenStore(output);
        }
    }
}


/**
 * The AST node for pre-increment (++) expression.
 */
class JPreIncrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a pre-increment expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPreIncrementOp(int line, JExpression operand) {
        super(line, "++ (pre)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "operand to ++ needs an LValue.");
            type = Type.ANY;
        } else {
            operand = operand.analyze(context);
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (operand instanceof JVariable) {
            // A local variable; 
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            output.addIINCInstruction(offset, 1);
            if (!isStatementExpression) {
                // Loading r
                operand.codegen(output);
            }
        } else {
            ((JLhs) operand).codegenLoadLhsLvalue(output);
            ((JLhs) operand).codegenLoadLhsRvalue(output);
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(IADD);
            if (!isStatementExpression) {
                // Loading rvalue.
                ((JLhs) operand).codegenDuplicateRvalue(output);
            }
            ((JLhs) operand).codegenStore(output);
        }
    }
}


/**
 * The AST node for a unary plus (+) expression.
 */
class JUnaryPlusOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a unary plus expression.
     *
     * @param line    line in which the unary plus expression occurs in the source file.
     * @param operand the operand.
     */
    public JUnaryPlusOp(int line, JExpression operand) {
        super(line, "+", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // Analyze the operand
        operand = (JExpression) operand.analyze(context);
        if (operand.type() == Type.INT) {
            type = Type.INT;
        }
        else if (operand.type() == Type.LONG) {
            type = Type.LONG;
        }
        else if (operand.type() == Type.DOUBLE) {
            type = Type.DOUBLE;
        }
        
        return this;
    }
       

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        operand.codegen(output);  // dont need to change
    }
}

/**
 * The AST node for post-increment (++) expression.
 */
class JPostIncrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a post-increment expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPostIncrementOp(int line, JExpression operand) {
        super(line, "++ (post)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "Operand to ++ must have an LValue.");
            type = Type.ANY;
        } else {
            operand = (JExpression) operand.analyze(context);
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }


    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (operand instanceof JVariable) {
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            if (!isStatementExpression) {
                
                operand.codegen(output);
            }
            output.addIINCInstruction(offset, 1);
        } else {
            ((JLhs) operand).codegenLoadLhsLvalue(output);
            ((JLhs) operand).codegenLoadLhsRvalue(output);
            if (!isStatementExpression) {
             
                ((JLhs) operand).codegenDuplicateRvalue(output);
            }
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(IADD);
            ((JLhs) operand).codegenStore(output);
        }
    }
}
/**
 * The AST node for a pre-decrement (--) expression.
 */
class JPreDecrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a pre-decrement expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPreDecrementOp(int line, JExpression operand) {
        super(line, "-- (pre)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "Operand to -- must have an LValue.");
            type = Type.ANY;
        } else {
            operand = (JExpression) operand.analyze(context);
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }




    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (operand instanceof JVariable) {
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            output.addIINCInstruction(offset, -1);
            if (!isStatementExpression) {
                operand.codegen(output);
            }
        } else {
            ((JLhs) operand).codegenLoadLhsLvalue(output);
            ((JLhs) operand).codegenLoadLhsRvalue(output);
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(ISUB);
            ((JLhs) operand).codegenDuplicateRvalue(output);
            ((JLhs) operand).codegenStore(output);
        }
    }
}