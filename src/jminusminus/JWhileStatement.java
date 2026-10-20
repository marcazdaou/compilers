package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * The AST node for a while-statement.
 */
class JWhileStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // Body.
    private JStatement body;

    protected boolean hasBreak;

    protected String breakLabel;

    protected boolean hasContinue;

    protected String continueLabel;



    /**
     * Constructs an AST node for a while-statement.
     *
     * @param line      line in which the while-statement occurs in the source file.
     * @param condition test expression.
     * @param body      the body.
     */
    public JWhileStatement(int line, JExpression condition, JStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
        this.hasBreak = false;
        this.breakLabel = null;
    }

    /**
     * {@inheritDoc}
     */
    public JWhileStatement analyze(Context context) {
        // Mark this while-loop as the current enclosing statement
        JMember.enclosingStatement.push(this);
    
        // Analyze the condition and make sure it's a boolean
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
    
        // Analyze the body of the loop
        body = (JStatement) body.analyze(context);
    
        // Done analyzing this loop
        JMember.enclosingStatement.pop();
    
        return this;
    }
    
    /**
     * Generate bytecode for the while loop.
     */
    public void codegen(CLEmitter output) {
        String testLabel = output.createLabel();  // Label to check the condition
    
        // Continue jumps to the condition check in a while loop
        continueLabel = testLabel;
    
        // Create a break label, even if it's not used
        breakLabel = output.createLabel();
    
        // Mark where to start checking the condition
        output.addLabel(testLabel);
    
        // Generate code for the condition; jump to break if false
        condition.codegen(output, breakLabel, false);
    
        // If condition is true, execute the body
        body.codegen(output);
    
        // Jump back to the condition to re-check
        output.addBranchInstruction(GOTO, testLabel);
    
        // Place the break label here to exit the loop
        output.addLabel(breakLabel);
    }
    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JWhileStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Body", e2);
        body.toJSON(e2);
    }
}