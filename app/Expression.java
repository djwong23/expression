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
		vars.add(new Variable(build));
		for (int i = vars.size() - 1; i >= 0; i--) {
			try {
				if (Integer.parseInt(vars.get(i).name) > -999999)
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
	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input
	 * @param vars   The variables array list, previously populated by
	 *               makeVariableLists
	 * @param arrays The arrays array list - previously populated by
	 *               makeVariableLists
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
		/** COMPLETE THIS METHOD **/
		// following line just a placeholder for compilation
		return 0;
	}
}
