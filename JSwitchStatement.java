package jminusminus;

import static jminusminus.CLConstants.LOOKUPSWITCH;
import static jminusminus.CLConstants.TABLESWITCH;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * The AST node for a switch-statement.
 */
class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    protected boolean hasBreak;
    
    protected String breakLabel;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> switchStmtGroups;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line             line in which the switch-statement occurs in the source file.
     * @param condition        test expression.
     * @param switchStmtGroups list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition, ArrayList<SwitchStatementGroup> switchStmtGroups) {
        super(line);
        this.condition = condition;
        this.switchStmtGroups = switchStmtGroups;
        this.hasBreak = false;
        this.breakLabel = null;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // Push the current switch statement onto the enclosing statement stack
        JMember.enclosingStatement.push(this);
        
        // Analyze the condition and ensure it's an integer
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.INT);
        
        // Create a new local context for the case groups
        LocalContext localContext = new LocalContext(context);
        
        // Analyze each switch case group
        for (SwitchStatementGroup group : switchStmtGroups) {
            // Analyze the labels of each case group
            for (int i = 0; i < group.getSwitchLabels().size(); i++) {
                JExpression label = group.getSwitchLabels().get(i);
                if (label != null) {
                    // Analyze the label and ensure it's an integer
                    label = label.analyze(context);
                    label.type().mustMatchExpected(line(), Type.INT);
                    group.getSwitchLabels().set(i, label);
                }
            }
            
            // Analyze the statements inside the case group
            for (int i = 0; i < group.block().size(); i++) {
                JStatement statement = group.block().get(i);
                group.block().set(i, (JStatement) statement.analyze(localContext));
            }
        }
        
        // Pop the current switch statement from the enclosing statement stack
        JMember.enclosingStatement.pop();
        
        return this;
    }
    
    public void codegen(CLEmitter output) {
        // Generate code for the condition
        condition.codegen(output);
        
        // Label for the default case
        String defaultLabel = output.createLabel();
        
        // Create a break label for loop control (if needed)
        breakLabel = output.createLabel();
        
        // Create a map of case labels and values
        TreeMap<Integer, String> switchCasePairs = new TreeMap<>();
        ArrayList<String> caseLabels = new ArrayList<>();
        
        // Find the minimum and maximum case values
        int lo = Integer.MAX_VALUE;
        int hi = Integer.MIN_VALUE;
        
        // First pass: create case labels and record the range of case values
        for (SwitchStatementGroup group : switchStmtGroups) {
            for (JExpression label : group.getSwitchLabels()) {
                if (label != null) {
                    // Create a label for the case and update the range
                    String caseLabel = output.createLabel();
                    int value = ((JLiteralInt) label).toInt();
                    
                    caseLabels.add(caseLabel);
                    switchCasePairs.put(value, caseLabel);
                    
                    if (value < lo) lo = value;
                    if (value > hi) hi = value;
                }
            }
        }
        
        // Calculate the number of case labels
        int nLabels = switchCasePairs.size();
        
        // Decide whether to use TABLESWITCH or LOOKUPSWITCH
        long tableSpaceCost = 5 + hi - lo;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels;
        long lookupTimeCost = nLabels;
        int opcode = nLabels > 0 && (tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost) ?
                     TABLESWITCH : LOOKUPSWITCH;
        
        // Emit the appropriate switch instruction based on the decision
        if (opcode == TABLESWITCH) {
            // For TABLESWITCH, create an array of labels ordered by case value
            ArrayList<String> tableLabels = new ArrayList<>();
            for (int i = lo; i <= hi; i++) {
                String label = switchCasePairs.get(i);
                tableLabels.add(label != null ? label : defaultLabel);
            }
            // Emit TABLESWITCH instruction
            output.addTABLESWITCHInstruction(defaultLabel, lo, hi, tableLabels);
        } else {
            // For LOOKUPSWITCH, use the map of case values and labels
            output.addLOOKUPSWITCHInstruction(defaultLabel, nLabels, switchCasePairs);
        }
        
        // Ensure default case is handled correctly
        boolean defaultUsed = false;
        
        // Second pass: Generate code for each case group
        int labelIndex = 0;
        for (SwitchStatementGroup group : switchStmtGroups) {
            boolean groupHasDefault = false;
            
            // Add labels for each case group
            for (JExpression label : group.getSwitchLabels()) {
                if (label == null) {
                    // Default case
                    groupHasDefault = true;
                    if (!defaultUsed) {
                        output.addLabel(defaultLabel);
                        defaultUsed = true;
                    }
                } else if (labelIndex < caseLabels.size()) {
                    output.addLabel(caseLabels.get(labelIndex++));
                }
            }
            
            // Generate code for the case group statements
            for (JStatement stmt : group.block()) {
                stmt.codegen(output);
            }
        }
        
        // Ensure the default case label is added if not yet used
        if (!defaultUsed) {
            output.addLabel(defaultLabel);
        }
        
        // Add the break label
        output.addLabel(breakLabel);
    }
    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : switchStmtGroups) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch-statement group consists of a list of switch labels and a block of statements.
 */
class SwitchStatementGroup {
    // Switch labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels switch labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Returns the switch labels associated with this switch-statement group.
     *
     * @return the switch labels associated with this switch-statement group.
     */
    public ArrayList<JExpression> getSwitchLabels() {
        return switchLabels;
    }

    /**
     * Returns the block of statements associated with this switch-statement group.
     *
     * @return the block of statements associated with this switch-statement group.
     */
    public ArrayList<JStatement> block() {
        return block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}