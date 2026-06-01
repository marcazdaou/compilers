package jminusminus;
import static jminusminus.CLConstants.GOTO;  // import GOTO

/**
 * The AST node for a conditional expression.
 */
class JConditionalExpression extends JExpression {
    // Test expression.
    private JExpression condition;

    // Then part.
    private JExpression thenPart;

    // Else part.
    private JExpression elsePart;

    /**
     * Constructs an AST node for a conditional expression.
     *
     * @param line      line in which the conditional expression occurs in the source file.
     * @param condition test expression.
     * @param thenPart  then part.
     * @param elsePart  else part.
     */
    public JConditionalExpression(int line, JExpression condition, JExpression thenPart, JExpression elsePart) {
        super(line);
        this.condition = condition;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // Analyze the condition expression and ensure it's a boolean
    condition = condition.analyze(context);
    condition.type().mustMatchExpected(line(), Type.BOOLEAN);  
    // Analyze the thenPart and elsePart, they should have the same type
    thenPart = thenPart.analyze(context);
    elsePart = elsePart.analyze(context);

    // Check that both parts have the same type
    thenPart.type().mustMatchExpected(line(), elsePart.type());

    
    type = thenPart.type();  // have the same type

    return this;
}
        

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Create labels else and end
    String elseLabel = output.createLabel();
    String endLabel = output.createLabel();

    // Generate code for the condition (if false, jump to elseLabel)
    condition.codegen(output);
    condition.codegen(output, elseLabel, false);

    // Generate code for the thenPart 
    thenPart.codegen(output);
    output.addBranchInstruction(GOTO, endLabel);  // Jump to endLabel after executing

    // Generate elseLabel and code for elsePart 
    output.addLabel(elseLabel);
    elsePart.codegen(output);

    // Add endLabel 
    output.addLabel(endLabel);
}
    

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JConditionalExpression:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("ThenPart", e2);
        thenPart.toJSON(e2);
        JSONElement e3 = new JSONElement();
        e.addChild("ElsePart", e3);
        elsePart.toJSON(e3);
    }
}
