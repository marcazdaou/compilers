package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.AASTORE;
import static jminusminus.CLConstants.ANEWARRAY;
import static jminusminus.CLConstants.BASTORE;
import static jminusminus.CLConstants.CASTORE;
import static jminusminus.CLConstants.DASTORE;
import static jminusminus.CLConstants.DUP;
import static jminusminus.CLConstants.IASTORE;
import static jminusminus.CLConstants.LASTORE;
import static jminusminus.CLConstants.NEWARRAY;

/**
 * The AST node for an array initializer.
 */
class JArrayInitializer extends JExpression {
    // The initializations.
    private final ArrayList<JExpression> initials;

    /**
     * Constructs an AST node for an array initializer.
     *
     * @param line     line in which this array initializer occurs in the source file.
     * @param type     the type of the array we're initializing.
     * @param initials initializations.
     */
    public JArrayInitializer(int line, Type type, ArrayList<JExpression> initials) {
        super(line);
        this.type = type;
        this.initials = initials;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        type = type.resolve(context);
        if (!type.isArray()) {
            JAST.compilationUnit.reportSemanticError(line, "cannot initialize a " + type.toString()
                    + " with an array sequence {...}");
            return this;
        }
        Type componentType = type.componentType();
        for (int i = 0; i < initials.size(); i++) {
            JExpression initial = initials.get(i);
            initials.set(i, initial = initial.analyze(context));
            if (!(initial instanceof JArrayInitializer)) {
                initial.type().mustMatchExpected(line, componentType);
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        Type componentType = type.componentType();

    // Push the size of the array (number of initial values).
    new JLiteralInt(line, String.valueOf(initials.size())).codegen(output);

    // Create an empty array of the right type.
    output.addArrayInstruction(
        componentType.isReference() ? ANEWARRAY : NEWARRAY,
        componentType.jvmName()
    );

    // Fill the array with initial values.
    for (int i = 0; i < initials.size(); i++) {
        JExpression initial = initials.get(i);

        output.addNoArgInstruction(DUP); // Duplicate the array reference.
        new JLiteralInt(line, String.valueOf(i)).codegen(output); // Push the index.
        initial.codegen(output); // Push the value to store.

        // Store the value in the array based on type.
        if (componentType == Type.INT) {
            output.addNoArgInstruction(IASTORE);
        } else if (componentType == Type.LONG) {
            output.addNoArgInstruction(LASTORE);
        } else if (componentType == Type.DOUBLE) {
            output.addNoArgInstruction(DASTORE);
        } else if (componentType == Type.BOOLEAN) {
            output.addNoArgInstruction(BASTORE);
        } else if (componentType == Type.CHAR) {
            output.addNoArgInstruction(CASTORE);
        } else if (!componentType.isPrimitive()) {
            output.addNoArgInstruction(AASTORE); // For objects
        }
    }
}
    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JArrayInitializer:" + line, e);
        if (initials != null) {
            for (JExpression initial : initials) {
                initial.toJSON(e);
            }
        }
    }
}