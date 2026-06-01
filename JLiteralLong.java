package jminusminus;

import static jminusminus.CLConstants.LCONST_0;
import static jminusminus.CLConstants.LCONST_1;

/**
 * The AST node for a long literal.
 */
class JLiteralLong extends JExpression {
    // String representation of the literal.
    private final String text;

    /**
     * Constructs an AST node for a long literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public JLiteralLong(int line, String text) {
        super(line);
        this.text = text;
    }

    /**
     * Returns the literal as a long.
     *
     * @return the literal as a long.
     */
    public long toLong() {
        return Long.parseLong(text.substring(0, text.length() - 1));
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        type = Type.LONG;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String valueStr = text;
    if (text.endsWith("L") || text.endsWith("l")) {
        valueStr = text.substring(0, text.length() - 1);
    }
    
    long l = Long.parseLong(valueStr);
    
    // Emit the most efficient instruction based on the value
    if (l == 0) {
        output.addNoArgInstruction(LCONST_0);
    } else if (l == 1) {
        output.addNoArgInstruction(LCONST_1);
    } else {
        output.addLDCInstruction(l);
    }
}

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JLiteralLong:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}