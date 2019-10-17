package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {
    public static String delims = " \t*+-/()[]";

    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is
     * created and stored, even if it appears more than once in the expression. At
     * this time, values for all variables and all array items are set to zero -
     * they will be loaded from a file in the loadVariableValues method.
     *
     * @param expr   The expression
     * @param vars   The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        String build = "";
        String character = "";
        for (int i = 0; i < expr.length(); i++) {
            character = expr.substring(i, i + 1);
            if (!delims.contains(character))
                build += character;
            else if (character.equals("[")) {
                boolean isRepeat = false;
                for (Array a : arrays) {
                    if (a.name.equals(build)) {
                        isRepeat = true;
                        break;
                    }
                }
                if (!isRepeat)
                    arrays.add(new Array(build));
                build = "";
            } else {
                if (!build.equals("")) {
                    boolean isRepeat = false;
                    for (Variable v : vars) {
                        if (v.name.equals(build)) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat)
                        vars.add(new Variable(build));
                    build = "";
                }
            }
        }
        boolean isRepeat = false;
        for (Variable v : vars) {
            if (v.name.equals(build)) {
                isRepeat = true;
                break;
            }
        }
        if (!isRepeat)
            vars.add(new Variable(build));
        for (int i = vars.size() - 1; i >= 0; i--) {
            try {
                if (vars.get(i).name.length() == 0)
                    vars.remove(i);
                else if (Integer.parseInt(vars.get(i).name) > -999999)
                    vars.remove(i);
            } catch (NumberFormatException e) {
                continue;
            }
        }
        System.out.println("Variables: ");
        for (Variable v : vars) {
            System.out.println(v.name);
        }
        System.out.println("Arrays: ");
        for (Array a : arrays)
            System.out.println(a.name);
        boolean sorted = false;
        Variable temp;
       /* while (!sorted) {
            sorted = true;
            for (int i = 0; i < vars.size() - 1; i++) {
                if (vars.get(i).name.length() < vars.get(i + 1).name.length()) {
                    temp = vars.get(i);
                    vars.set(i, vars.get(i + 1));
                    vars.set(i + 1, temp);
                    sorted = false;
                }
            }
        } */
    }

    /**
     * Loads values for variables and arrays in the expression
     *
     * @param sc     Scanner for values input
     * @param vars   The variables array list, previously populated by
     *               makeVariableLists
     * @param arrays The arrays array list - previously populated by
     *               makeVariableLists
     * @throws IOException If there is a problem with the input
     */
    public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
            throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
                continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
                arr = arrays.get(arri);
                arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok, " (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;
                }
            }
        }
    }

    /**
     * Evaluates the expression.
     *
     * @param vars   The variables array list, with values for all variables in the
     *               expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        float result = 0;
        expr = replaceVariables(expr, vars, arrays);
        expr = removeSpaces(expr);
        String number1 = "";
        String number2 = "";
        while (expr.contains("(")) {
            String inParenth = expr.substring(expr.indexOf('(') + 1, expr.lastIndexOf(')'));
            float parenth = evaluate(inParenth, vars, arrays);
            expr = expr.substring(0, expr.indexOf('(')) + parenth + expr.substring(expr.lastIndexOf(')') + 1);
            System.out.println(expr);
        }
        while (expr.contains("[")) {
            String inArray = expr.substring(expr.indexOf('[') + 1, expr.lastIndexOf(']'));
            float arr = evaluate(inArray, vars, arrays);
            System.out.println(arr);
            expr = expr.substring(0, expr.indexOf('[') + 1) + arr + expr.substring(expr.lastIndexOf(']'));
            String build = "";
            for (int i = expr.indexOf('[') - 1; i >= 0; i--) {
                if (delims.contains(expr.charAt(i) + ""))
                    break;
                else
                    build = expr.charAt(i) + build;
            }
            String[] removeArr = expr.split(build + "[" + arr + "]", 2);
            float val = 0;
            for (Array a : arrays) {
                if (a.name.equals(build))
                    val = a.values[(int) (arr)];
            }
            expr = expr.replace(build + "[" + arr + "]", val + "");

            System.out.println(expr);


        }
        while (expr.contains("*")) {
            int index = expr.indexOf("*");
            for (int i = index - 1; i >= 0; i--) {
                if (delims.contains(expr.charAt(i) + ""))
                    break;
                else {
                    number1 = expr.charAt(i) + number1;
                }
            }
            for (int i = index + 1; i < expr.length(); i++) {
                if (delims.contains(expr.charAt(i) + "")) {
                    break;
                } else {
                    number2 = number2 + expr.charAt(i);
                }
            }
            String eq = number1 + "\\*" + number2;
            String[] remove = expr.split(eq, 2);
            if (remove.length == 2)
                expr = remove[0] + (Float.parseFloat(number1) * Float.parseFloat(number2)) + remove[1];
            else
                expr = remove[0];
            number1 = number2 = "";
        }
        while (expr.contains("/")) {
            int index = expr.indexOf("/");
            for (int i = index - 1; i >= 0; i--) {
                if (delims.contains(expr.charAt(i) + ""))
                    break;
                else {
                    number1 = expr.charAt(i) + number1;
                }
            }
            for (int i = index + 1; i < expr.length(); i++) {
                if (delims.contains(expr.charAt(i) + "")) {
                    break;
                } else {
                    number2 = number2 + expr.charAt(i);
                }
            }
            String eq = number1 + "\\/" + number2;
            String[] remove = expr.split(eq, 2);
            if (remove.length == 2)
                expr = remove[0] + (Float.parseFloat(number1) / Float.parseFloat(number2)) + remove[1];
            else
                expr = remove[0];
            number1 = number2 = "";
        }
        while (expr.contains("+")) {
            int index = expr.indexOf("+");
            for (int i = index - 1; i >= 0; i--) {
                if (delims.contains(expr.charAt(i) + "")) {
                    break;
                } else {
                    number1 = expr.charAt(i) + number1;
                }
            }
            for (int i = index + 1; i < expr.length(); i++) {
                if (delims.contains(expr.charAt(i) + "")) {
                    break;
                } else {
                    number2 = number2 + expr.charAt(i);
                }
            }
            String eq = number1 + "\\+" + number2;
            String[] remove = expr.split(eq, 2);
            if (remove.length == 2)
                expr = remove[0] + (Float.parseFloat(number1) + Float.parseFloat(number2)) + remove[1];
            else
                expr = remove[0];
            number1 = number2 = "";
        }
        while (expr.contains("-")) {
            int index = expr.indexOf("-");
            boolean shouldBreak = true;
            boolean startNeg = false;
            if (index == 0) {
                startNeg = true;
                for (int i = index + 1; i < expr.length(); i++) {
                    if (delims.contains(expr.charAt(i) + "")) {
                        shouldBreak = false;
                        break;
                    }
                }
                if (shouldBreak)
                    break;
            }
            if (index > 0) {
                if (!Character.isDigit(expr.charAt(index - 1)))
                    break;
            }
            if (index == 0)
                index = 1+ expr.substring(1).indexOf('-');
            for (int i = index - 1; i >= 0; i--) {
                if (delims.contains(expr.charAt(i) + "")) {
                    break;
                } else {
                    number1 = expr.charAt(i) + number1;
                }
            }
            for (int i = index + 1; i < expr.length(); i++) {
                if (delims.contains(expr.charAt(i) + "")) {
                    break;
                } else {
                    number2 = number2 + expr.charAt(i);
                }
            }
            String eq = number1 + "-" + number2;
            if (startNeg)
                eq = "-" + eq;
            expr = expr.replace(eq, (Float.parseFloat(number1) - Float.parseFloat(number2)) + "");
            number1 = number2 = "";
        }
        System.out.println(expr);
        result = Float.parseFloat(expr);
        return result;
    }

    /**
     * Turns variables to numbers
     *
     * @param expr
     * @param vars
     * @param arrays
     * @return expression with variables replaced with numbers
     */
    private static String replaceVariables(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        int i = 0;
        ArrayList<String> strings = new ArrayList<>();
        while (i < vars.size()) {
            String var = vars.get(i).name;
            if (expr.contains(var)) {
                int varIndex = expr.indexOf(var);
                String nextChar = "";
                String prevChar = "";
                if (expr.length() == 1) {
                    expr = "" + vars.get(i).value;
                    continue;
                }
                if (varIndex + var.length() != expr.length()) {
                    nextChar = expr.substring(varIndex + var.length(), varIndex + var.length() + 1);
                    if (delims.contains(nextChar) && !nextChar.equals("[")) {
                        String[] spl = expr.split(var, 2);
                        expr = spl[0] + vars.get(i).value + spl[1];
                    } else {
                        strings.add(expr.substring(0, varIndex + 1));
                        expr = expr.substring(varIndex + 1);
                    }
                } else if (varIndex + var.length() == expr.length()) {
                    prevChar = expr.substring(varIndex - 1, varIndex);
                    if (delims.contains(prevChar)) {
                        String[] spl = expr.split(var, 2);
                        expr = spl[0] + vars.get(i).value + spl[1];
                    } else {
                        i += 1;
                        String fix = "";
                        for (String s : strings)
                            fix += s;
                        expr = fix + expr;
                    }
                } else {
                    i += 1;
                    String fix = "";
                    for (String s : strings)
                        fix += s;
                    expr = fix + expr;
                }
            } else {
                i += 1;
                String fix = "";
                for (String s : strings)
                    fix += s;
                expr = fix + expr;
                strings = new ArrayList<>();
            }
        }
        return expr;
    }

    private static String removeSpaces(String expr) {
        char character = 0;
        for (int i = expr.length() - 1; i >= 0; i--) {
            character = expr.charAt(i);
            if (character == ' ') {
                expr = expr.substring(0, i) + expr.substring(i + 1);
            }
        }
        return expr;
    }
}
