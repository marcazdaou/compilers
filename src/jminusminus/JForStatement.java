package jminusminus;

import static jminusminus.CLConstants.GOTO;

import java.util.ArrayList;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
    private JStatement body;

    private Context LocalContext;

    protected boolean hasBreak;
  
    protected String breakLabel;

    protected boolean hasContinue;

    protected String continueLabel;
    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition, ArrayList<JStatement> update,
                         JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
        this.hasBreak = false;
        this.breakLabel = null;
        this.hasContinue = false;
        this.continueLabel = null;
        
    }

    /**
     * {@inheritDoc}
     */
    public JForStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
    
        // Create a local context for the loop
        this.LocalContext = new LocalContext(context);
    
        // Analyze initialization statements if present
        if (init != null) {
            ArrayList<JStatement> initStatements = new ArrayList<>();
            for (JStatement initStmt : init) {
                initStmt = (JStatement) initStmt.analyze(LocalContext);
                initStatements.add(initStmt);
            }
            this.init = initStatements;
        }
    
        // Analyze the condition if present
        if (condition != null) {
            condition = condition.analyze(LocalContext);
        }
    
        // Analyze update statements if present
        if (update != null) {
            ArrayList<JStatement> updateStatements = new ArrayList<>();
            for (JStatement updateStmt : update) {
                updateStmt = (JStatement) updateStmt.analyze(LocalContext);
                updateStatements.add(updateStmt);
            }
            this.update = updateStatements;
        }
    
        // Analyze the loop body
        body = (JStatement) body.analyze(LocalContext);
    
        // Remove this statement from enclosingStatement after processing
        JMember.enclosingStatement.pop();
    
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String conditionLabel = output.createLabel();
    
        // Create labels for continue and break statements
        continueLabel = output.createLabel();
        breakLabel = output.createLabel();
    
        // Generate code for initialization if present
        if (init != null) {
            for (JStatement initStmt : init) {
                initStmt.codegen(output);
            }
        }
    
        // Add label for condition check
        output.addLabel(conditionLabel);
    
        // Generate code for condition if present, or set it to an infinite loop
        if (condition != null) {
            condition.codegen(output, breakLabel, false);
        }
    
        // Generate code for the loop body
        body.codegen(output);
    
        // Add continue label after body code
        output.addLabel(continueLabel);
    
        // Generate code for update statements if present
        if (update != null) {
            for (JStatement updateStmt : update) {
                updateStmt.codegen(output);
            }
        }
    
        // Jump back to condition check
        output.addBranchInstruction(GOTO, conditionLabel);
    
        // Add break label to handle loop exit
        output.addLabel(breakLabel);
    }
    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}