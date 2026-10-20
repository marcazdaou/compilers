"""Test suite for the j-- compiler.

Four kinds of checks, all run from the repository root after `ant`:

1. Golden-file tests: the scanner (-t) and parser (-p) output of each program must
   match the expected .tokens / .ast file, for both the hand-written front end and
   the JavaCC front end.
2. Differential tests: each program is compiled by j-- and by the reference javac,
   both class files are run on the JVM with the same arguments, and the outputs
   must be identical.
3. Rejection tests: programs with type errors must produce a compile error, not a
   crash, and no class file.
4. Bytecode checks: specific JVM instructions must appear in the generated code
   (for example TABLESWITCH for a dense switch).

Usage:
    ant
    python tests/run_tests.py
"""
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JMM_JAR = ROOT / "lib" / "j--.jar"
HANDWRITTEN, JAVACC = "jminusminus.Main", "jminusminus.JavaCCMain"

# Language features that j-- in this repository does not implement; their golden files are skipped.
NOT_SUPPORTED = {"parsing/ExceptionHandlers.java", "parsing/Interface.java"}

# Differential tests: (source file, command-line arguments)
CASES = [
    # Feature tests written for this project (no input needed)
    ("tests/features/OperatorsTest.java", []),
    ("tests/features/LongDoubleTest.java", []),
    ("tests/features/LoopsTest.java", []),
    ("tests/features/SwitchTest.java", []),
    # Course test programs for the same features
    ("codegen/Operators.java", ["17", "5"]),
    ("codegen/Factorial.java", ["10"]),
    ("codegen/ForStatement.java", ["100"]),
    ("codegen/BreakStatement.java", ["50"]),
    ("codegen/ContinueStatement.java", ["1000"]),
    ("codegen/Quadratic.java", ["1", "-3", "2"]),
    ("codegen/Stats.java", []),
    ("simpleops/Division.java", ["42", "5"]),
    ("simpleops/Remainder.java", ["42", "5"]),
    ("simpleops/UnaryPlus.java", ["-7"]),
    ("simpleops/DoStatement.java", ["10"]),
    ("simpleops/DayOfWeek.java", ["7", "4", "1976"]),
    # Programs from the original j-- distribution: the base language still works
    ("tests/HelloWorld.java", []),
    ("tests/Factorial.java", ["7"]),
    ("tests/Fibonacci.java", ["15"]),
    ("tests/Euclid.java", ["1071", "462"]),
    ("tests/Pascal.java", ["6"]),
    ("tests/PrimeCounter.java", ["100"]),
    ("tests/DayOfWeek.java", ["2", "29", "2024"]),
    ("tests/DivisorPattern.java", ["6"]),
    ("tests/LeapYear.java", ["2000"]),
    ("tests/MatrixSquared.java", ["1", "2", "3", "4"]),
    ("tests/PowersOfTwo.java", ["10"]),
    ("tests/SumOfPowers.java", ["4", "3"]),
    ("tests/Casts.java", []),
    ("tests/Inheritance.java", []),
    ("tests/InstanceOf.java", []),
]

# Programs with type errors: j-- must report an error, without crashing and without writing a class file
MUST_REJECT = [
    "simpleops/ConditionalExpressionFail.java",
    "simpleops/DivisionFail.java",
    "simpleops/RemainderFail.java",
    "simpleops/UnaryPlusFail.java",
    "simpleops/DoStatementFail.java",
]

# (source file, instruction that must appear in `javap -c` of the class j-- generates)
BYTECODE_CHECKS = [
    ("tests/features/SwitchTest.java", "tableswitch"),
    ("tests/features/SwitchTest.java", "lookupswitch"),
    ("tests/features/LongDoubleTest.java", "lmul"),
    ("tests/features/LongDoubleTest.java", "ddiv"),
    ("tests/features/LongDoubleTest.java", "lcmp"),
    ("tests/features/LongDoubleTest.java", "dcmpg"),
]

passed, failed = 0, 0


def run(cmd):
    return subprocess.run(cmd, cwd=ROOT, capture_output=True, text=True, timeout=60)


def report(ok, label, detail=""):
    global passed, failed
    if ok:
        passed += 1
        print(f"PASS  {label}")
    else:
        failed += 1
        print(f"FAIL  {label}" + (f"\n      {detail.strip()[:600]}" if detail else ""))


def normalize(text):
    return text.replace("\r\n", "\n").strip()


def golden_tests():
    """Scanner and parser output vs the expected .tokens / .ast files."""
    suites = [
        ("scanning", HANDWRITTEN, "-t", ".tokens"),
        ("javacc_frontend", JAVACC, "-t", ".tokens"),
        ("parsing", HANDWRITTEN, "-p", ".ast"),
        ("simpleops", HANDWRITTEN, "-p", ".ast"),
        ("javacc_frontend", JAVACC, "-p", ".ast"),
    ]
    for folder, main_class, flag, suffix in suites:
        for source in sorted((ROOT / folder).glob("*.java")):
            expected = source.with_suffix(suffix)
            relative = source.relative_to(ROOT).as_posix()
            if not expected.exists() or relative in NOT_SUPPORTED:
                continue
            result = run(["java", "-cp", str(JMM_JAR), main_class, flag, relative])
            front_end = "JavaCC" if main_class == JAVACC else "hand-written"
            report(normalize(result.stdout) == normalize(expected.read_text()),
                   f"{relative} ({front_end} {'scanner' if flag == '-t' else 'parser'})",
                   result.stderr)


def differential_tests(work):
    """Compile with j-- and javac, run both, compare outputs. Returns {source: j-- class dir}."""
    compiled = {}
    for case_no, (source, args) in enumerate(CASES):
        name = Path(source).stem
        jmm_dir, javac_dir = work / f"jmm{case_no}", work / f"javac{case_no}"
        jmm_dir.mkdir(), javac_dir.mkdir()
        ours = run(["java", "-cp", str(JMM_JAR), HANDWRITTEN, "-d", str(jmm_dir), source])
        reference = run(["javac", "-nowarn", "-d", str(javac_dir), source])
        label = f"{source} {' '.join(args)}".strip()
        if not list(jmm_dir.glob("*.class")):
            report(False, f"{label}: j-- could not compile it", ours.stdout + ours.stderr)
            continue
        if reference.returncode != 0:
            report(False, f"{label}: javac could not compile it", reference.stderr)
            continue
        got = run(["java", "-cp", str(jmm_dir), name] + args)
        want = run(["java", "-cp", str(javac_dir), name] + args)
        same = (got.stdout, got.returncode) == (want.stdout, want.returncode)
        report(same, f"{label}: same output as javac",
               f"j--: {got.stdout + got.stderr}\n      javac: {want.stdout}")
        if same:
            compiled[source] = jmm_dir
    return compiled


def rejection_tests(work):
    for source in MUST_REJECT:
        out_dir = work / ("rejected_" + Path(source).stem)
        out_dir.mkdir()
        result = run(["java", "-cp", str(JMM_JAR), HANDWRITTEN, "-d", str(out_dir), source])
        output = result.stdout + result.stderr
        clean = "error:" in output and "Exception" not in output and not list(out_dir.glob("*.class"))
        report(clean, f"{source}: rejected with a compile error", output)


def bytecode_checks(compiled):
    for source, instruction in BYTECODE_CHECKS:
        out_dir = compiled.get(source)
        listing = run(["javap", "-c", "-cp", str(out_dir), Path(source).stem]).stdout if out_dir else ""
        report(instruction in listing, f"{source}: j-- emits {instruction}")


def main():
    if not JMM_JAR.exists():
        sys.exit("lib/j--.jar not found: build the compiler first with `ant`.")
    work = Path(tempfile.mkdtemp(prefix="jmm-tests-"))
    try:
        print("== Scanner and parser (golden files)")
        golden_tests()
        print("\n== Code generation (j-- vs javac)")
        compiled = differential_tests(work)
        print("\n== Type errors are rejected cleanly")
        rejection_tests(work)
        print("\n== Generated bytecode")
        bytecode_checks(compiled)
    finally:
        shutil.rmtree(work, ignore_errors=True)
    print(f"\n{passed}/{passed + failed} checks passed")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
