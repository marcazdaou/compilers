# j-- Compiler

A full compiler for **j--**, a Java subset language, targeting **JVM bytecode**. Implemented a complete compilation pipeline from raw source code to executable `.class` files, extending a base compiler with 6 new types and 15+ language features.

## Compilation Pipeline

```
Source (.j--) → Lexer/Scanner → Parser → AST → Semantic Analysis → JVM Bytecode (.class)
```

## Features Implemented

### New Types & Literals
| Feature | File |
|---|---|
| `double` literals & arithmetic | `JLiteralDouble.java` |
| `long` literals & arithmetic | `JLiteralLong.java` |
| Array types & initializers | `JArrayExpression.java`, `JArrayInitializer.java` |
| Ternary expression (`a ? b : c`) | `JConditionalExpression.java` |

### Control Flow
| Feature | File |
|---|---|
| `do-while` loop | `JDoStatement.java` |
| `for` loop (init / condition / update) | `JForStatement.java` |
| `switch` with `TABLESWITCH` / `LOOKUPSWITCH` optimization | `JSwitchStatement.java` |
| `break` with correct nested-loop semantics | `JBreakStatement.java` |
| `continue` with correct nested-loop semantics | `JContinueStatement.java` |
| `return` in typed and void methods | `JReturnStatement.java` |

### Operators & Expressions
| Feature | File |
|---|---|
| Compound assignment (`+=`, `-=`, `*=`, `/=`) | `JAssignment.java` |
| Full comparison set (`<=`, `>`, `>=`, `!=`) | `JComparisonExpression.java` |
| Boolean binary (`&&`, `\|\|`) | `JBooleanBinaryExpression.java` |
| Arithmetic binary (`+`, `-`, `*`, `/`, `%`) | `JBinaryExpression.java` |
| Unary operators (pre/post `++`, `--`, `!`) | `JUnaryExpression.java` |

### Declarations
| Feature | File |
|---|---|
| Variable declarations with initializers | `JVariableDeclaration.java`, `JVariable.java` |
| Constructor declarations | `JConstructorDeclaration.java` |
| Method declarations with return types | `JMethodDeclaration.java` |

## Architecture

| File | Role |
|---|---|
| `Scanner.java` | Tokenizer — converts source characters into a token stream |
| `Parser.java` | Recursive-descent parser — builds the AST from tokens |
| `J*.java` | AST node classes — one per language construct |
| `j-- (2).jj` | JavaCC grammar specification |

## Technologies

- **Java** — compiler implementation
- **JavaCC** — grammar & parser generation
- **JVM Bytecode** — code generation target (`.class` files)

## Build & Run

```bash
# Compile the compiler
javac *.java

# Compile a j-- source file
java Main MyProgram.j--

# Run the generated JVM bytecode
java MyProgram
```

## Example

```java
// test.j-- — features exercised:  for loop, ternary, long literal, switch
public class Test {
    public static void main(String[] args) {
        long n = 100L;
        for (int i = 0; i < n; i++) {
            String label = (i % 2 == 0) ? "even" : "odd";
            switch (i % 3) {
                case 0: System.out.println(label + "-fizz"); break;
                default: System.out.println(label);
            }
        }
    }
}
```