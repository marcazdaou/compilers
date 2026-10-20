package jminusminus;
import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a break-statement.
 */
class JBreakStatement extends JStatement {

    private JStatement enclosingStatement;
    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */

     public JStatement analyze(Context context) {
        // Get the closest enclosing loop or switch statement
        enclosingStatement = JMember.enclosingStatement.peek();
    
        // If there's no loop or switch, it's an error
        if (enclosingStatement == null) {
            JAST.compilationUnit.reportSemanticError(line(), "break should be inside a loop ");
        } 
        // Otherwise, mark the enclosing statement as having a break
        else if (enclosingStatement instanceof JDoStatement) {
            ((JDoStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JWhileStatement) {
            ((JWhileStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JForStatement) {
            ((JForStatement) enclosingStatement).hasBreak = true;
        } else if (enclosingStatement instanceof JSwitchStatement) {
            ((JSwitchStatement) enclosingStatement).hasBreak = true;
        } 
        // If the statement isn't a valid type, it's also an error
        else {
            JAST.compilationUnit.reportSemanticError(line(), "break should be in a loop ");
        }
    
        return this;
    }
    
    public void codegen(CLEmitter output) {
        // Only generate code if we have a valid enclosing statement
        if (enclosingStatement != null) {
            String breakLabel = null;
    
            // Get the break label from the specific type of statement
            if (enclosingStatement instanceof JDoStatement) {
                breakLabel = ((JDoStatement) enclosingStatement).breakLabel;
            } else if (enclosingStatement instanceof JWhileStatement) {
                breakLabel = ((JWhileStatement) enclosingStatement).breakLabel;
            } else if (enclosingStatement instanceof JForStatement) {
                breakLabel = ((JForStatement) enclosingStatement).breakLabel;
            } else if (enclosingStatement instanceof JSwitchStatement) {
                breakLabel = ((JSwitchStatement) enclosingStatement).breakLabel;
            }
    
            // If a valid break label was found, jump to it
            if (breakLabel != null) {
                output.addBranchInstruction(GOTO, breakLabel);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}