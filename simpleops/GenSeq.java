import java.util.ArrayList;

import jminusminus.CLEmitter;

import static jminusminus.CLConstants.*;

    /** import java.lang.Integer;
import java.lang.System;

public class Seq {
    public static void main(String[] args) {
    public static void main(String[] args) {
        int start = Integer.parseInt(args[0]);
        int step = Integer.parseInt(args[1]);
        int stop = Integer.parseInt(args[2]);
        
       while (start <= stop){
        System.out.println(start);
        start += step;
       }
    }
} **/

public class GenSeq {
    public static void main(String[] args) {
        // Create a CLEmitter instance
        CLEmitter e = new CLEmitter ( true );

        // Create an ArrayList instance to store modifiers
        ArrayList < String > modifiers = new ArrayList < String >();

        // public class Seq {
        modifiers.add ("public");
        e.addClass ( modifiers , "Seq", "java/lang/Object", null , true );

        // public static void main ( String [] args ) {
        modifiers.clear ();
        modifiers.add ("public");
        modifiers.add ("static");
        e.addMethod ( modifiers , "main", "([Ljava/lang/String;)V", null , true );

        // int start = Integer.parseInt(args[0]);
        e.addNoArgInstruction ( ALOAD_0 );
        e.addNoArgInstruction ( ICONST_0 );
        e.addNoArgInstruction ( AALOAD );
        e.addMemberAccessInstruction ( INVOKESTATIC , "java/lang/Integer",
         "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction ( ISTORE_1 );

        //int step = Integer.parseInt(args[1]);
        e.addNoArgInstruction ( ALOAD_0 );
        e.addNoArgInstruction ( ICONST_1 );
        e.addNoArgInstruction ( AALOAD );
        e.addMemberAccessInstruction ( INVOKESTATIC , "java/lang/Integer",
         "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction ( ISTORE_2 );

        // int stop = Integer.parseInt(args[2]);
        e.addNoArgInstruction ( ALOAD_0 );
        e.addNoArgInstruction ( ICONST_2 );
        e.addNoArgInstruction ( AALOAD );
        e.addMemberAccessInstruction ( INVOKESTATIC , "java/lang/Integer",
         "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction ( ISTORE_3 );

        // topLabel:
        e.addLabel("topLabel");

        // if (start > stop) branch to endLabel;
        e.addNoArgInstruction(ILOAD_1); // Load start
        e.addNoArgInstruction(ILOAD_3); // Load stop
        e.addBranchInstruction(IF_ICMPGT, "endLabel");

        // System.out.println(start);
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        e.addNoArgInstruction(ILOAD_1); // Load start
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V");

        // start += step;
        e.addNoArgInstruction(ILOAD_1); // Load start
        e.addNoArgInstruction(ILOAD_2); // Load step
        e.addNoArgInstruction(IADD); // Add start + step
        e.addNoArgInstruction(ISTORE_1); // Store back in start

        // go to topLabel;
        e.addBranchInstruction(GOTO, "topLabel");

        // endLabel:
        e.addLabel("endLabel");

        // return;
        e.addNoArgInstruction(RETURN);

        // Write Seq.class to file system
        e.write();
    }
}
    