# j-- Compiler Extensions

My extensions to **j--**, a compiler for a subset of Java that targets **JVM bytecode** (the teaching compiler
from *Introduction to Compiler Construction in a Java World*). Across the scanner, the parser, the JavaCC
grammar, semantic analysis and code generation, I added new types, operators and control-flow statements.

This repository contains the files I added or modified; they plug into the full j-- code base from the course.

## Pipeline

```
j-- source → Scanner (tokens) → Parser (AST) → analyze() (types, scopes) → codegen() → JVM .class file
```

Each feature below needed changes at every stage: new tokens, grammar rules, AST node classes with
`analyze()` type checking, and `codegen()` bytecode emission.

## What I added

**Types and literals**
- `double` and `long` primitive types with literals (`3.14`, `42L`): `JLiteralDouble`, `JLiteralLong`, scanner and grammar rules

**Operators** (`JBinaryExpression`, `JUnaryExpression`, `JComparisonExpression`, `JBooleanBinaryExpression`, `JAssignment`)
- Arithmetic `-`, `/`, `%` and unary `+`
- Comparisons `>=`, `<`, `!=` (alongside the existing ones)
- Logical `||`
- Pre/post increment and decrement (`++x`, `x++`, `--x`, `x--`)
- Compound assignment `-=`, `*=`, `/=`, `%=`
- Ternary conditional `a ? b : c` (`JConditionalExpression`)

**Statements**
- `do-while` (`JDoStatement`) and `for` (`JForStatement`) loops
- `break` and `continue` that target the innermost enclosing loop or switch (`JBreakStatement`, `JContinueStatement`)
- `switch` (`JSwitchStatement`): emits **TABLESWITCH** or **LOOKUPSWITCH** by comparing their estimated
  space and time cost, the same heuristic `javac` uses

**Grammar**
- `j-- (2).jj`: the JavaCC version of the grammar, updated with the new tokens and rules

## Files

| Files | Stage |
|---|---|
| `Scanner.java`, `TokenInfo.java` | Lexical analysis: new keywords, literals and operators |
| `Parser.java`, `j-- (2).jj` | Recursive-descent parser and JavaCC grammar |
| `J*.java` | AST nodes: parsing, semantic analysis and bytecode generation for each construct |

## Build

Copy these files into the j-- source tree from the course (`jminusminus` package), replacing the originals,
then build and run j-- as described in that distribution.

## Concepts

Lexical analysis · recursive-descent parsing · abstract syntax trees · type checking · JVM bytecode generation
