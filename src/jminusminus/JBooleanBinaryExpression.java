package jminusminus;

import static jminusminus.CLConstants.GOTO;
import static jminusminus.CLConstants.ICONST_0;
import static jminusminus.CLConstants.ICONST_1;
import static jminusminus.CLConstants.IF_ACMPEQ;
import static jminusminus.CLConstants.IF_ACMPNE;
import static jminusminus.CLConstants.IF_ICMPEQ;
import static jminusminus.CLConstants.IF_ICMPNE;
import static jminusminus.CLConstants.LCMP;
import static jminusminus.CLConstants.IFNE;
import static jminusminus.CLConstants.IFEQ;
import static jminusminus.CLConstants.DCMPG;
import static jminusminus.CLConstants.DCMPL;
/**
 * This abstract base class is the AST node for binary expressions that return booleans.
 */
abstract class JBooleanBinaryExpression extends JBinaryExpression {
    /**
     * Constructs an AST node for a boolean binary expression.
     *
     * @param line     line in which the boolean binary expression occurs in the source file.
     * @param operator the boolean binary operator.
     * @param lhs      lhs operand.
     * @param rhs      rhs operand.
     */

    protected JBooleanBinaryExpression(int line, String operator, JExpression lhs, JExpression rhs) {
        super(line, operator, lhs, rhs);
    }

    /**
     * Emits the branch for a numeric comparison whose two operands are already on the stack.
     *
     * int operands use a single IF_ICMPxx instruction. long and double operands are first reduced
     * to an int (-1, 0 or 1) with LCMP, DCMPL or DCMPG, followed by the matching IFxx instruction.
     * For doubles, DCMPG is used for {@code <} and {@code <=} and DCMPL otherwise, so that any
     * comparison involving NaN is false, exactly as javac compiles it.
     *
     * @param output       the code emitter.
     * @param type         the operand type.
     * @param intOp        IF_ICMPxx instruction for int operands.
     * @param zeroOp       IFxx instruction applied to the result of LCMP / DCMPx.
     * @param nanIsGreater true to use DCMPG (NaN compares as greater), false for DCMPL.
     * @param targetLabel  the branch target.
     */
    protected static void compareAndBranch(CLEmitter output, Type type, int intOp, int zeroOp,
                                           boolean nanIsGreater, String targetLabel) {
        if (type == Type.LONG) {
            output.addNoArgInstruction(LCMP);
            output.addBranchInstruction(zeroOp, targetLabel);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(nanIsGreater ? DCMPG : DCMPL);
            output.addBranchInstruction(zeroOp, targetLabel);
        } else {
            output.addBranchInstruction(intOp, targetLabel);
        }
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
}

/**
 * The AST node for an equality (==) expression.
 */
class JEqualOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for an equality expression.
     *
     * @param line line number in which the equality expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public JEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "==", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type().isReference()) {
            output.addBranchInstruction(onTrue ? IF_ACMPEQ : IF_ACMPNE, targetLabel);
        } else {
            compareAndBranch(output, lhs.type(), onTrue ? IF_ICMPEQ : IF_ICMPNE, onTrue ? IFEQ : IFNE,
                    false, targetLabel);
        }
    }
}

/**
 * The AST node for a logical-and (&amp;&amp;) expression.
 */
class JLogicalAndOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for a logical-and expression.
     *
     * @param line line in which the logical-and expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLogicalAndOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "&&", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (onTrue) {
            String falseLabel = output.createLabel();
            lhs.codegen(output, falseLabel, false);
            rhs.codegen(output, targetLabel, true);
            output.addLabel(falseLabel);
        } else {
            lhs.codegen(output, targetLabel, false);
            rhs.codegen(output, targetLabel, false);
        }
    }
}

/**
 * The AST node for a logical-or (||) expression.
 */
class JLogicalOrOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for a logical-or expression.
     *
     * @param line line in which the logical-or expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLogicalOrOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "||", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;

    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (onTrue) {
            // Jump to the target if either side is true (the rhs is skipped when the lhs is true).
            lhs.codegen(output, targetLabel, true);
            rhs.codegen(output, targetLabel, true);
        } else {
            // Jump to the target only if both sides are false: a true lhs skips the rhs entirely.
            String trueLabel = output.createLabel();
            lhs.codegen(output, trueLabel, true);
            rhs.codegen(output, targetLabel, false);
            output.addLabel(trueLabel);
        }
    }
}


/**
 * The AST node for a not-equal-to (!=) expression.
 */
class JNotEqualOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for not-equal-to (!=) expression.
     *
     * @param line line number in which the not-equal-to (!=) expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public JNotEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "!=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type().isReference())
            output.addBranchInstruction(onTrue ? IF_ACMPNE : IF_ACMPEQ, targetLabel);
        else {
            compareAndBranch(output, lhs.type(), onTrue ? IF_ICMPNE : IF_ICMPEQ, onTrue ? IFNE : IFEQ,
                    false, targetLabel);
        }
    }
}
