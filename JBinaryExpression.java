package jminusminus;


import static jminusminus.CLConstants.LADD; // add import
import static jminusminus.CLConstants.DADD;  // add import
import static jminusminus.CLConstants.IADD;
import static jminusminus.CLConstants.IREM;
import static jminusminus.CLConstants.DREM;  // add import
import static jminusminus.CLConstants.LREM;  // add import
import static jminusminus.CLConstants.IDIV;  // add import
import static jminusminus.CLConstants.LDIV;  // add import
import static jminusminus.CLConstants.DDIV;  // add import
import static jminusminus.CLConstants.IMUL;
import static jminusminus.CLConstants.DMUL;  // add import
import static jminusminus.CLConstants.LMUL;  // add import
import static jminusminus.CLConstants.ISUB;
import static jminusminus.CLConstants.DSUB;  // add import
import static jminusminus.CLConstants.LSUB;  // add import

/**
 * This abstract base class is the AST node for a binary expression --- an expression with a binary operator and two
 * operands: lhs and rhs.
 */
abstract class JBinaryExpression extends JExpression {
    /**
     * The binary operator.
     */
    protected String operator;

    /**
     * The lhs operand.
     */
    protected JExpression lhs;

    /**
     * The rhs operand.
     */
    protected JExpression rhs;

    /**
     * Constructs an AST node for a binary expression.
     *
     * @param line     line in which the binary expression occurs in the source file.
     * @param operator the binary operator.
     * @param lhs      the lhs operand.
     * @param rhs      the rhs operand.
     */
    protected JBinaryExpression(int line, String operator, JExpression lhs, JExpression rhs) {
        super(line);
        this.operator = operator;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBinaryExpression:" + line, e);
        e.addAttribute("operator", operator);
        e.addAttribute("type", type == null ? "" : type.toString());
        JSONElement e1 = new JSONElement();
        e.addChild("Operand1", e1);
        lhs.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Operand2", e2);
        rhs.toJSON(e2);
    }
}

/**
 * The AST node for a multiplication (*) expression.
 */
class JMultiplyOp extends JBinaryExpression {
    /**
     * Constructs an AST for a multiplication expression.
     *
     * @param line line in which the multiplication expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JMultiplyOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "*", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    
     public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        if (lhs.type() == Type.INT) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type() == Type.LONG) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type() == Type.DOUBLE) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        }

        return this;
    }

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);

        if (type == Type.INT) {
            output.addNoArgInstruction(IMUL);
        } else if (type == Type.LONG) {
            output.addNoArgInstruction(LMUL);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(DMUL);
        }
    }
}


/**
 * The AST node for a plus (+) expression. In j--, as in Java, + is overloaded to denote addition
 * for numbers and concatenation for Strings.
 */
class JPlusOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an addition expression.
     *
     * @param line line in which the addition expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JPlusOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "+", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        if (lhs.type() == Type.STRING || rhs.type() == Type.STRING) {
            return new JStringConcatenationOp(line, lhs, rhs).analyze(context);
        }

        if (lhs.type() == Type.INT) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type() == Type.LONG) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type() == Type.DOUBLE) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        }

        return this;
    }

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);

        if (type == Type.INT) {
            output.addNoArgInstruction(IADD);
        } else if (type == Type.LONG) {
            output.addNoArgInstruction(LADD);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(DADD);
        }
    }
}
/**
 * The AST node for a subtraction (-) expression.
 */
class JSubtractOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a subtraction expression.
     *
     * @param line line in which the subtraction expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JSubtractOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "-", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        if (lhs.type() == Type.INT) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type() == Type.LONG) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type() == Type.DOUBLE) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        }

        return this;
    }

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);

        if (type == Type.INT) {
            output.addNoArgInstruction(ISUB);
        } else if (type == Type.LONG) {
            output.addNoArgInstruction(LSUB);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(DSUB);
        }
    }
}
/**
 * The AST node for a division (/) expression.
 */
class JDivideOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a division expression.
     *
     * @param line line in which the division expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JDivideOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "/", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        if (lhs.type() == Type.INT) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type() == Type.LONG) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type() == Type.DOUBLE) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        }

        return this;
    }

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);

        if (type == Type.INT) {
            output.addNoArgInstruction(IDIV);
        } else if (type == Type.LONG) {
            output.addNoArgInstruction(LDIV);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(DDIV);
        }
    }
}

/**
 * The AST node for a remainder (%) expression.
 */
class JRemainderOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a remainder expression.
     *
     * @param line line in which the division expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JRemainderOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "%", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        if (lhs.type() == Type.INT) {
            rhs.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        } else if (lhs.type() == Type.LONG) {
            rhs.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        } else if (lhs.type() == Type.DOUBLE) {
            rhs.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        }

        return this;
    }

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);

        if (type == Type.INT) {
            output.addNoArgInstruction(IREM);
        } else if (type == Type.LONG) {
            output.addNoArgInstruction(LREM);
        } else if (type == Type.DOUBLE) {
            output.addNoArgInstruction(DREM);
        }
    }
}