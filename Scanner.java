package jminusminus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;

import static jminusminus.TokenKind.*;

/**
 * A lexical analyzer for j--, that has no backtracking mechanism.
 */
class Scanner {
    /**
     * End of file character.
     */
    public final static char EOFCH = CharReader.EOFCH;

    // Keywords in j--.
    private final Hashtable<String, TokenKind> reserved;

    // Source characters.
    private final CharReader input;

    // Next unscanned character.
    private char ch;

    // Whether a scanner error has been found.
    private boolean isInError;

    // Source file name.
    private final String fileName;

    // Line number of current token.
    private int line;

    /**
     * Constructs a Scanner from a file name.
     *
     * @param fileName name of the source file.
     * @throws FileNotFoundException when the named file cannot be found.
     */
    public Scanner(String fileName) throws FileNotFoundException {
        this.input = new CharReader(fileName);
        this.fileName = fileName;
        isInError = false;

        // Keywords in j--
        reserved = new Hashtable<>();
        reserved.put(ABSTRACT.image(), ABSTRACT);
        reserved.put(DO.image(), DO);  // added DO Statement
        reserved.put(BREAK.image(), BREAK);         // added break 
        reserved.put(CASE.image(), CASE);           // added case 
        reserved.put(CONTINUE.image(), CONTINUE);   // added continue 
        reserved.put(DEFAULT.image(), DEFAULT);     // added default 
        reserved.put(DOUBLE.image(), DOUBLE);       // added double 
        reserved.put(FOR.image(), FOR);             // added for 
        reserved.put(LONG.image(), LONG);           // added long 
        reserved.put(SWITCH.image(), SWITCH);       // added switch 
        reserved.put(BOOLEAN.image(), BOOLEAN);
        reserved.put(CHAR.image(), CHAR);
        reserved.put(CLASS.image(), CLASS);
        reserved.put(ELSE.image(), ELSE);
        reserved.put(EXTENDS.image(), EXTENDS);
        reserved.put(FALSE.image(), FALSE);
        reserved.put(IF.image(), IF);
        reserved.put(IMPORT.image(), IMPORT);
        reserved.put(INSTANCEOF.image(), INSTANCEOF);
        reserved.put(INT.image(), INT);
        reserved.put(NEW.image(), NEW);
        reserved.put(NULL.image(), NULL);
        reserved.put(PACKAGE.image(), PACKAGE);
        reserved.put(PRIVATE.image(), PRIVATE);
        reserved.put(PROTECTED.image(), PROTECTED);
        reserved.put(PUBLIC.image(), PUBLIC);
        reserved.put(RETURN.image(), RETURN);
        reserved.put(STATIC.image(), STATIC);
        reserved.put(SUPER.image(), SUPER);
        reserved.put(THIS.image(), THIS);
        reserved.put(TRUE.image(), TRUE);
        reserved.put(VOID.image(), VOID);
        reserved.put(WHILE.image(), WHILE);

        // Prime the pump.
        nextCh();
    }

    /**
     * Scans and returns the next token from input.
     *
     * @return the next scanned token.
     */
    public TokenInfo getNextToken() {
        StringBuilder buffer;
        boolean moreWhiteSpace = true;
        while (moreWhiteSpace) {
            while (isWhitespace(ch)) {
                nextCh();
            }
            if (ch == '/') {
                nextCh();
                if (ch == '/') {
                    // CharReader maps all new lines to '\n'.
                    while (ch != '\n' && ch != EOFCH) {
                        nextCh();
                    }
                } else if (ch == '*') { // Multiline comment
                    while (true) { // if there is a * after / keep going.
                        nextCh();  
                        if (ch == EOFCH) {  // Prevent infinite loop if '*/' is missing
                        reportScannerError(" Nope. Comment is wrong");  // error 
                        }
                        if (ch == '*') {   // if there is a a second *
                            nextCh();
                            if (ch == '/') {  // and there is a / after the star
                                nextCh();
                                break;
                            }
                        }
                    }
                } else if (ch == '=') {  // "/="
                nextCh();
                return new TokenInfo(DIV_ASSIGN, line);  // Div assign
            } else {
                    return new TokenInfo(DIV, line); // for the division
                }
            } else {
                moreWhiteSpace = false;
            }
        }
        switch (ch) {
            case EOFCH:
                return new TokenInfo(EOF, line);
            case '%':
                nextCh();   // case remainder
                if (ch == '=') {  
                    nextCh();
                    return new TokenInfo(MOD_ASSIGN, line);  // "%="
                } else {
                    return new TokenInfo(REM, line);  // remainder
                }
            case ',':
                nextCh();
                return new TokenInfo(COMMA, line);
            case '.':
                buffer = new StringBuilder();
                buffer.append(ch);
                nextCh(); // Advance past '.'
            
                // If ch is NOT a digit, return it as a DOT token
                if (!isDigit(ch)) {
                    return new TokenInfo(DOT, ".", line);
                }
            
                // Otherwise, handle numbers starting with '.'
                buffer.append(digits());
            
                // If ch is 'e' or 'E', append exponent
                if (ch == 'e' || ch == 'E') {
                    buffer.append(exponent());
                }
            
                // If ch is 'd' or 'D', append it and advance input
                if (ch == 'd' || ch == 'D') {
                    buffer.append(ch);
                    nextCh();
                }
            
                return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line); // to here
                
            case '[':
                nextCh();
                return new TokenInfo(LBRACK, line);
            case '{':
                nextCh();
                return new TokenInfo(LCURLY, line);
            case '(':
                nextCh();
                return new TokenInfo(LPAREN, line);
            case ']':
                nextCh();
                return new TokenInfo(RBRACK, line);
            case '}':
                nextCh();
                return new TokenInfo(RCURLY, line);
            case ')':
                nextCh();
                return new TokenInfo(RPAREN, line);
            case ';':
                nextCh();
                return new TokenInfo(SEMI, line);
            case '-':
                nextCh();
                if (ch == '-') {
                    nextCh();
                    return new TokenInfo(DEC, line);
                } else if (ch == '=') {
                    nextCh();
                    return new TokenInfo(MINUS_ASSIGN, line);  //  "-="
                } else {
                    return new TokenInfo(MINUS, line);  //  "-"
                }
            case '+':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(PLUS_ASSIGN, line);
                } else if (ch == '+') {
                    nextCh();
                    return new TokenInfo(INC, line);
                } else {
                    return new TokenInfo(PLUS, line);
                }
            case '*':
            nextCh();
            if (ch == '=') {
                nextCh();
                return new TokenInfo(MULT_ASSIGN, line);  //  "*="
            } else {
                return new TokenInfo(STAR, line);  //  "*"
            }
            case '?':
                nextCh();
                return new TokenInfo(QUESTION, line);  // QUESTION
            case ':':
                nextCh();
                return new TokenInfo(COLON, line);  //COLON
            case '=':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(EQUAL, line);
                } else {
                    return new TokenInfo(ASSIGN, line);
                }
                case '>':
                nextCh();
                if (ch == '=') {  
                    nextCh();
                    return new TokenInfo(GREATER_EQUAL, line);  //  ">="
                } else {
                    return new TokenInfo(GT, line);  //  ">"
                }
            case '<':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(LE, line);
                } else {
                    return new TokenInfo(LESS, line); // less
                }
                case '!':
                nextCh();
                if (ch == '=') {  
                    nextCh();
                    return new TokenInfo(NOT_EQUAL, line);  //  "!="
                } else {
                    return new TokenInfo(LNOT, line);  //  "!"
                }
            case '&':
                nextCh();
                if (ch == '&') {
                    nextCh();
                    return new TokenInfo(LAND, line);
                } else {
                    reportScannerError("operator & is not supported in j--");
                    return getNextToken();
                }
                case '|':
                nextCh();
                if (ch == '|') {  
                    nextCh();
                    return new TokenInfo(LOR, line);  // for the  "||" operator
                } else {
                    reportScannerError("operator | is not supported in j--"); // | is not supported
                    return getNextToken();  
                }
            case '\'':
                buffer = new StringBuilder();
                buffer.append('\'');
                nextCh();
                if (ch == '\\') {
                    nextCh();
                    buffer.append(escape());
                } else {
                    buffer.append(ch);
                    nextCh();
                }
                if (ch == '\'') {
                    buffer.append('\'');
                    nextCh();
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                } else {
                    // Expected a '; report error and try to recover.
                    reportScannerError(ch + " found by scanner where closing ' was expected");
                    while (ch != '\'' && ch != ';' && ch != '\n') {
                        nextCh();
                    }
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                }
            case '"':
                buffer = new StringBuilder();
                buffer.append("\"");
                nextCh();
                while (ch != '"' && ch != '\n' && ch != EOFCH) {
                    if (ch == '\\') {
                        nextCh();
                        buffer.append(escape());
                    } else {
                        buffer.append(ch);
                        nextCh();
                    }
                }
                if (ch == '\n') {
                    reportScannerError("unexpected end of line found in string");
                } else if (ch == EOFCH) {
                    reportScannerError("unexpected end of file found in string");
                } else {
                    // Scan the closing ".
                    nextCh();
                    buffer.append("\"");
                }
                return new TokenInfo(STRING_LITERAL, buffer.toString(), line);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            // start 
            buffer = new StringBuilder();

    if (isDigit(ch)) {
        buffer.append(digits());
    }

    if (ch == 'd' || ch == 'D') {
        buffer.append(ch);
        nextCh();
        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
    }

    // If the next character is NOT '.' (decimal point) or 'e/E' (exponent), it's an integer.
    if (ch != '.' && ch != 'e' && ch != 'E' && ch != 'l' && ch != 'L') {
        return new TokenInfo(INT_LITERAL, buffer.toString(), line);
    }

    // Handle long literal (e.g., 123L)
    if (ch == 'l' || ch == 'L') {
        buffer.append(ch);
        nextCh();
        return new TokenInfo(LONG_LITERAL, buffer.toString(), line);
    }

    // If ch is '.', process as a double literal
    if (ch == '.') {
        buffer.append(ch);
        nextCh();  // Advance input

        // If ch is a digit, append digits to buffer
        if (isDigit(ch)) {
            buffer.append(digits());
        }

        // If ch is 'e' or 'E', append exponent
        if (ch == 'e' || ch == 'E') {
            buffer.append(exponent());
        }

        // If ch is 'd' or 'D', append it and advance input
        if (ch == 'd' || ch == 'D') {
            buffer.append(ch);
            nextCh();
        }

        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
    }

    // Otherwise, check for exponent notation
    if (ch == 'e' || ch == 'E') {
        buffer.append(exponent());

        // If ch is 'd' or 'D', append it and advance input
        if (ch == 'd' || ch == 'D') {
            buffer.append(ch);
            nextCh();
        }

        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
    }

    // Otherwise, check for 'd' or 'D' suffix without exponent
    if (ch == 'd' || ch == 'D') {
        buffer.append(ch);
        nextCh();
        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
    }

    // Otherwise, malformed double literal error
    reportScannerError("Malformed double literal: " + buffer.toString());
    return getNextToken();

                default:
                        if (isIdentifierStart(ch)) {
                        buffer = new StringBuilder();
                        while (isIdentifierPart(ch)) {
                            buffer.append(ch);
                            nextCh();
                    }
                    String identifier = buffer.toString();
                    if (reserved.containsKey(identifier)) {
                        return new TokenInfo(reserved.get(identifier), line);
                    } else {
                        return new TokenInfo(IDENTIFIER, identifier, line);
                    }
                } else {
                    reportScannerError("unidentified input token '%c'", ch);
                    nextCh();
                    return getNextToken();
                }
        }
    }

  /** 
 * Scans and returns a string of digits starting at 'ch', which must be a digit.
 */
private String digits() {
    StringBuilder buffer = new StringBuilder();
    while (isDigit(ch)) {
        buffer.append(ch);
        nextCh();
    }
    return buffer.toString();
}

/** 
 * Scans and returns an exponent, starting at 'ch', which must be 'e' or 'E'.
 */
private String exponent() {
    StringBuilder buffer = new StringBuilder();

    if (ch == 'e' || ch == 'E') {
        buffer.append(ch);
        nextCh();

        // Optional sign (+ or -)
        if (ch == '+' || ch == '-') {
            buffer.append(ch);
            nextCh();
        }

        // Must have at least one digit after exponent
        if (!isDigit(ch)) {
            reportScannerError("Malformed exponent in double literal: " + buffer.toString());
            return "";
        }

        buffer.append(digits()); // Collect digits after exponent
    }

    return buffer.toString();
}
    /**
     * Returns true if an error has occurred, and false otherwise.
     *
     * @return true if an error has occurred, and false otherwise.
     */
    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Returns the name of the source file.
     *
     * @return the name of the source file.
     */
    public String fileName() {
        return fileName;
    }

    // Scans and returns an escaped character.
    private String escape() {
        switch (ch) {
            case 'b':
                nextCh();
                return "\\b";
            case 't':
                nextCh();
                return "\\t";
            case 'n':
                nextCh();
                return "\\n";
            case 'f':
                nextCh();
                return "\\f";
            case 'r':
                nextCh();
                return "\\r";
            case '"':
                nextCh();
                return "\\\"";
            case '\'':
                nextCh();
                return "\\'";
            case '\\':
                nextCh();
                return "\\\\";
            default:
                reportScannerError("Badly formed escape: \\%c", ch);
                nextCh();
                return "";
        }
    }

    // Advances ch to the next character from input, and updates the line number.
    private void nextCh() {
        line = input.line();
        try {
            ch = input.nextChar();
        } catch (Exception e) {
            reportScannerError("unable to read characters from input");
        }
    }

    // Reports a lexical error and records the fact that an error has occurred. This fact can be ascertained from the
    // Scanner by sending it an errorHasOccurred message.
    private void reportScannerError(String message, Object... args) {
        isInError = true;
        System.err.printf("%s:%d: error: ", fileName, line);
        System.err.printf(message, args);
        System.err.println();
    }

    // Returns true if the specified character is a digit (0-9), and false otherwise.
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    // Returns true if the specified character is a whitespace, and false otherwise.
    private boolean isWhitespace(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\f');
    }

    // Returns true if the specified character can start an identifier name, and false otherwise.
    private boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$');
    }

    // Returns true if the specified character can be part of an identifier name, and false otherwise.
    private boolean isIdentifierPart(char c) {
        return (isIdentifierStart(c) || isDigit(c));
    }
}

/**
 * A buffered character reader, which abstracts out differences between platforms, mapping all new lines to '\n', and
 * also keeps track of line numbers.
 */
class CharReader {
    /**
     * Representation of the end of file as a character.
     */
    public final static char EOFCH = (char) -1;

    // The underlying reader records line numbers.
    private final LineNumberReader lineNumberReader;

    // Name of the file that is being read.
    private final String fileName;

    /**
     * Constructs a CharReader from a file name.
     *
     * @param fileName the name of the input file.
     * @throws FileNotFoundException if the file is not found.
     */
    public CharReader(String fileName) throws FileNotFoundException {
        lineNumberReader = new LineNumberReader(new FileReader(fileName));
        this.fileName = fileName;
    }

    /**
     * Scans and returns the next character.
     *
     * @return the character scanned.
     * @throws IOException if an I/O error occurs.
     */
    public char nextChar() throws IOException {
        return (char) lineNumberReader.read();
    }

    /**
     * Returns the current line number in the source file.
     *
     * @return the current line number in the source file.
     */
    public int line() {
        return lineNumberReader.getLineNumber() + 1; // LineNumberReader counts lines from 0
    }

    /**
     * Returns the file name.
     *
     * @return the file name.
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Closes the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        lineNumberReader.close();
    }
}
