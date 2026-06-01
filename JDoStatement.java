package jminusminus;

/**
 * The AST node for a do-statement.
 */
class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;


    protected boolean hasBreak;
    

    protected String breakLabel;

    protected boolean hasContinue;

    protected String continueLabel;

    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
        this.hasBreak = false;
        this.breakLabel = null;
        this.hasContinue = false;
        this.continueLabel = null;
    }

    /**
    * Analyzes the do-statement. Checks that the test expression
    * is boolean and analyzes both the test and the body.

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // Mark this loop as the current enclosing statement
        JMember.enclosingStatement.push(this);
    
        // Analyze the loop condition and check that it’s a boolean
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
    
        // Analyze the body of the loop
        body = (JStatement) body.analyze(context);
    
        // Done analyzing this loop
        JMember.enclosingStatement.pop();
    
        return this;
    }
    
    /**
     * Generate bytecode for the do-while loop.
     */
    public void codegen(CLEmitter output) {
        String bodyLabel = output.createLabel();
        String testLabel = output.createLabel();
    
        // Label for continue always goes to the condition check in do-while
        continueLabel = testLabel;
    
        // Create a label for break, even if it's not used
        breakLabel = output.createLabel();
    
        // Start of the loop body
        output.addLabel(bodyLabel);
        body.codegen(output);  // Generate code for the loop body
    
        // Condition check (also where continue jumps to)
        output.addLabel(testLabel);
        condition.codegen(output);  // Evaluate condition
    
        // If condition is true, jump back to the start of the loop
        output.addBranchInstruction(CLConstants.IFNE, bodyLabel);
    
        // Label to jump to when breaking out of the loop
        output.addLabel(breakLabel);
    }
    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}