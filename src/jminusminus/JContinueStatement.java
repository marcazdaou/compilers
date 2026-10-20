package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a continue-statement.
 */
class JContinueStatement extends JStatement {

    private JStatement enclosingStatement;
    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */
    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // Get the nearest loop this continue is inside of
        enclosingStatement = JMember.enclosingStatement.peek();
    
        // If there's no loop, report an error
        if (enclosingStatement == null) {
            JAST.compilationUnit.reportSemanticError(line(), "continue must be inside a loop");
        } else {
            // Mark the loop as having a continue statement
            if (enclosingStatement instanceof JDoStatement) {
                ((JDoStatement) enclosingStatement).hasContinue = true;
            } else if (enclosingStatement instanceof JWhileStatement) {
                ((JWhileStatement) enclosingStatement).hasContinue = true;
            } else if (enclosingStatement instanceof JForStatement) {
                ((JForStatement) enclosingStatement).hasContinue = true;
            } else {
                JAST.compilationUnit.reportSemanticError(line(), "continue must be inside a loop");
            }
        }
    
        return this;
    }
    
    /**
     * Generate bytecode for continue
     */
    public void codegen(CLEmitter output) {
        if (enclosingStatement != null) {
            String continueLabel = null;
    
            // Get the label to jump to for continue
            if (enclosingStatement instanceof JDoStatement) {
                continueLabel = ((JDoStatement) enclosingStatement).continueLabel;
            } else if (enclosingStatement instanceof JWhileStatement) {
                continueLabel = ((JWhileStatement) enclosingStatement).continueLabel;
            } else if (enclosingStatement instanceof JForStatement) {
                continueLabel = ((JForStatement) enclosingStatement).continueLabel;
            }
    
            // Jump to the continue label
            if (continueLabel != null) {
                output.addBranchInstruction(GOTO, continueLabel);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}