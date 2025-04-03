import java.util.HashSet;
import java.util.Set;

public class EquationParser {
  public static boolean debug = true; // all debugging prints will be removed when there are no issues anymore
  static String name = "";
  static boolean isFunction = false;
  private static boolean parseBetweenBrackets = false;
  private static byte specialOpMagicNum = 6;
  public static ApplicationController controller;

  // this code could also be in Main.java
  // TODO
  // - Sanitize String
  // - Check if valid
  // - Deal with brackets and sings
  // - figure out what todo with variables
  // - special functions

  private static String transformString(String input) {
    // Sanitize and Transform string
    // remove all spaces
    input = input.replaceAll("\\s", "");
    input = input.toLowerCase();
    if (input.equals("")) {
      return null;
    }

    // replace constants
    String uniConstants = "Φ π";
    String constants = "phi pi e"; // must be divided by spaces for split() in getConstant
                                   // should be sorted by length to avoid replacement with shorter constants
                                   // F.e when there would be a constant like "pie" and "e" is replaced
    Double constValues[] = { 1.6180339887498948, Math.PI, Math.E };
    input = replaceConstants(input, constants, constValues);
    input = replaceConstants(input, uniConstants, constValues);

    // replace )( with )*( so something like (2+1)(x-1) works
    input = input.replaceAll("\\)\\(", ")*(");

    // Transform
    if (!parseBetweenBrackets) {
      name = "";
      if (!input.contains("=") && !input.contains("y")) { // fe. 3x+1
        input += "-y";
        isFunction = true;
        name = "";
        if (debug)
          System.out.println("a function");
      } else if (checkIfFunction(input)) { // fe. f(x)=x or y=x
        input = input.split("=")[1] + "-y";
        if (debug)
          System.out.println("is a function");
      } else if (input.contains("=")) {
        String[] split = input.split("=");
        if (split.length == 2) {
          input = split[0] + "-(" + split[1] + ")";
          isFunction = false;
          if (debug)
            System.out.println("is not a function");
        } else { // invalid input y=
          return null;
        }
      } else { // 3y+1
        isFunction = false;
        if (debug)
          System.out.println("not a function");

      }
    }

    // FIXME workaround when equation starts with brackets (20-2)*2
    char first = input.charAt(0);
    if (first == '(') {
      input = "0+" + input;
    } else if (first == '-') {
      input = "0" + input;
    }

    if (debug)
      System.out.println("input after TRANSFORM: " + input);
    return input;
  }

  private static String replaceConstants(String input, String constants, Double[] constValues) {
    String constArr[] = constants.split(" ");
    for (int i = 0; i < constArr.length; i++) {
      // System.out.println("Test replace: |"+constArr[i]+"|");
      input = input.replaceAll(constArr[i], "(" + constValues[i] + ")");
    }
    return input;
  }

  public static EquationTree parseString(String input, ApplicationController appController) {
    if (debug && input.equals("debug")) {
      testParser(appController);
    }

    input = transformString(input);
    if (input == null) {
      return null;
    }
    controller = appController;

    if (debug)
      System.out.println(input);
    StringBuffer in = new StringBuffer(input); // The StringBuffer is mutable by refrence
                                               // so that i can manipulate the string in getNextNode
    EquationNode currentNode = getNextNode(in);
    int bracketDepth = 0;
    EquationNode lastNode = currentNode;
    OperatorStack operators = new OperatorStack();
    EquationNode root = null;

    while (currentNode != null) {
      Object val = currentNode.value;
      Byte state = currentNode.state;
      Byte opLevel = currentNode.opLevel;
      currentNode.bracketDepth = bracketDepth;

      if (handleAdvancedInput(currentNode, lastNode, in)) {
        bracketDepth = currentNode.bracketDepth; // since it could have been updated
        currentNode = getNextNode(in);
        // System.out.println("doskip");
        continue;
      }

      if (state == 0 || state == 1) { // is either a num or variable
        System.out.println("NUMORVAR: " + val + "| " + lastNode.value + " " + lastNode.bracketDepth);
        if (lastNode.state >= 2) {
          lastNode.right = currentNode;
        }

        if (state == 1 && controller.functionExists(val.toString())) {
          return null;
        }
        if (state == 1 && !(val.equals("y") || val.equals("x"))) { // handle
                                                                   // variables
          controller.customVarList.addCustomVar(val.toString());
        }

        lastNode = currentNode;
        // lastNode.bracketDepth = bracketDepth;
        // System.out.println("setting lastnode="+val);
        if (root == null) {
          if (debug)
            System.out.println("setin rooot");
          root = currentNode;
        }
      } else if (state == 3 || state == 4) { // is specialFunction or function
        if (debug)
          System.out.println("current: " + val + " | " + bracketDepth);
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);
        if (stackTop != null) {
          EquationNode lastOp = stackTop.elem;
          lastOp.right = currentNode;
          currentNode.above = lastOp;
          if (debug)
            System.out.println("addBelow " + lastOp.value);
        } else {
          root = currentNode;
          if (debug)
            System.out.println("setin rooot");
        }
        lastNode = currentNode;
        operators.add(currentNode, bracketDepth);

      } else if (state == -1) { // its a bracket
        if (val.equals("(")) {
          bracketDepth++;
        } else {
          bracketDepth--;
        }
      } else if (state == specialOpMagicNum) { // is "special" operator (root log)
        currentNode.state = 2; // change state because it will be treated like a operator in calculate()
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);
        String[] betweenBrackts = getBetweenBrackets(in);
        if (betweenBrackts != null) {
          parseBetweenBrackets = true;
          EquationTree parsedTree;
          if (debug) {
            System.out
                .println("leftStr: " + betweenBrackts[0] + " rightStr: " + betweenBrackts[1]);
            System.out.println("---PARSING LEFT----");
          }
          parsedTree = parseString(betweenBrackts[0], controller);
          if (parsedTree == null) {
            parseBetweenBrackets = false;
            return null;
          }
          EquationNode left = parsedTree.root;

          if (debug)
            System.out.println("---PARSING RIGHT---");

          parsedTree = parseString(betweenBrackts[1], controller);
          if (parsedTree == null) {
            parseBetweenBrackets = false;
            return null;
          }
          EquationNode right = parsedTree.root;

          if (debug) {
            System.out.println("---PARSING DONE----");
            System.out.println("left: " + left.value + " right: " + right.value);
          }
          currentNode.left = left;
          currentNode.right = right;
          parseBetweenBrackets = false;
        } else if (debug) {
          System.out.println("YOO Somethin wong da bwackts no woking");
        }

        if (stackTop != null) {
          EquationNode lastOp = stackTop.elem;
          lastOp.right = currentNode; // XXX DANGEROUS
          // addBelow(lastOp, currentNode);

        } else {
          root = currentNode;
          if (debug)
            System.out.println("> root: " + root.value);
        }
        operators.add(currentNode, bracketDepth);
        lastNode = currentNode;

      } else if (state == 2) { // is operator (+-*/)
        if (debug)
          System.out.println("current: " + val + " | " + bracketDepth);
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);

        if (stackTop != null) {

          if ((lastNode.bracketDepth < bracketDepth) && val.equals("-")) { // so that negative numbers work
            if (debug)
              System.out.println("neg. number fix " + lastNode.value);
            lastNode.right = new EquationNode((byte) 0, 0);
          }

          EquationNode lastOp = stackTop.elem;
          int lastDepth = lastOp.bracketDepth;

          if (bracketDepth > lastDepth
              || (lastOp.state == 2 && lastOp.opLevel < opLevel)) {
            // Either in brackets or operator is Higher
            if (debug)
              System.out.println("addBelow " + lastOp.value);
            addBelow(lastOp, currentNode);
          } else { // add current above. F.e 2*2+2
            EquationNode above = lastOp.above;
            if (debug)
              System.out.println("add above " + lastOp.value);
            currentNode.left = lastOp;
            lastOp.above = currentNode;

            if (lastOp == root) {
              if (debug)
                System.out.println("YOOOOOO");
              root = currentNode;
            }

            if (above != null) {
              if (debug)
                System.out.println("-#- below: " + above.value);
              // above.right
              above.right = currentNode;
              currentNode.above = above;
            }
          }

        } else {
          root = currentNode;
          if (debug)
            System.out.println("> root: " + root.value);
          currentNode.left = lastNode;
        }
        operators.add(currentNode, bracketDepth);
        lastNode = currentNode;
      }

      currentNode = getNextNode(in);// might be null now, dont use after this line
    }
    // System.out.println("done");
    // root = operators.getRoot();

    // fix workaround
    if (root != null && !root.value.equals("-") && root.left != null) {
      if (root.left.state == 0) {
        if (root.left.value.equals("0")) {
          root = root.right;
        }
      }
    }

    if (debug)
      operators.printStack();
    if (root != null) {
      root.recursivePrint(""); // For debugging
      TwoDVec coords = new TwoDVec<Double>(0.0, 0.0);
      EquationTree[] existingFunctions = controller.getAllFunctions();
      double res = root.calculate(coords, null, existingFunctions);
      if (debug)
        System.out.println(res + " name: " + name + " isFunction: " + isFunction);
      if ((double) coords.x == -1.0) {
        if (debug)
          System.out.println("Return null");
        return null;
      }
    }
    // debugging

    return new EquationTree(root, name, isFunction);
  }

  private static boolean handleAdvancedInput(EquationNode currentNode, EquationNode lastNode,
      StringBuffer input) {
    // should recognize the following and add a multiplikation inbetween:
    // 2(, )2, 2x, x2, 2sin, xsin, )sin,
    // should fix x^-1

    byte state = currentNode.state;

    if (currentNode.value.equals("-") && lastNode.value.equals("^")) { // ^-
      StringBuffer testBuffer = new StringBuffer(input);
      EquationNode nextNode = getNextNode(testBuffer);
      if (nextNode.state == 0) { // is a num
        // System.out.println("-- BUFFERS: ");
        // System.out.println(testBuffer.toString());
        // System.out.println(input.toString());

        input.setLength(0);
        input.append("(0-" + nextNode.value + ")" + testBuffer);
        // System.out.println(input.toString());
        return true;
      }
    }

    // cant do add the multiplikation -> return
    if (lastNode.equals(currentNode) || state == 2 || state == -1 || lastNode.state >= 2 || lastNode.state == -1) {
      return false;
    }
    int bracketDepth = currentNode.bracketDepth;
    boolean doContinue = false;
    Object val = currentNode.value;

    // handeling brackets
    if (lastNode.bracketDepth != bracketDepth) {
      // System.out.println("bracketParse");
      if (lastNode.bracketDepth < bracketDepth) { // before bracket 2(
        // System.out.println("before brack");
        input.insert(0, "*(" + val);
        currentNode.bracketDepth--;
      } else {
        input.insert(0, "*" + val); // after bracket )2
        // System.out.println("after brack");
      }
      doContinue = true;
    } else if (lastNode.state != state || val.equals("x") || lastNode.value.equals("x")) { // handle 2x x2 ax xa
      // System.out.println("handle other");
      input.insert(0, "*" + val);
      doContinue = true;
    }

    return doContinue;
  }

  private static boolean checkIfFunction(String input) {
    // only one char functions and also y=x
    // f(x) and not wow(x)
    if (input.length() < 3) { // so its not just y=
      return false;
    }
    String sub = input.substring(0, 2); // should be y=
    if (sub.equals("y=") && !input.split("=")[1].contains("y")) {
      name = "";
      isFunction = true;
      return true;
    }

    if (input.length() < 6) { // so its not just f(x)=
      return false;
    }
    sub = input.substring(1, 5); // should be (x)=
    if (sub.equals("(x)=")) {
      name = input.charAt(0) + "";
      isFunction = true;
      return true;
    }

    return false;
  }

  public static String[] getBetweenBrackets(StringBuffer input) {
    System.out.println("input: " + input);
    int depth = 1;
    char current;
    String value[] = { "", "" };
    byte index = 0;

    for (int i = 1; i < input.length(); i++) { // start at 1 as to skip the first bracket
      current = input.charAt(i);
      if (current == '(') {
        depth++;
      } else if (current == ')') {
        depth--;
      }

      if (depth == 0) {
        input = input.delete(0, i + 1);
        System.out.println("inp without brackets: " + input);
        System.out.println("vals: " + value[0] + " | " + value[1]);
        return value;
      }
      if (current == ',' && depth == 1) {
        index++; // to split between ,
      } else {
        value[index] += Character.toString(current);
      }
    }
    return null;
  }

  private static void addBelow(EquationNode lastNode, EquationNode currentNode) {
    EquationNode lastRight = lastNode.right;
    lastNode.right = currentNode;
    currentNode.left = lastRight;
    currentNode.above = lastNode;
  }

  private static EquationNode getNextNode(StringBuffer input) {
    if (input.length() == 0) {
      return null;
    }
    int counter = 0;
    char first = input.charAt(0);
    String value = Character.toString(first);
    byte state = getState(first);
    byte opLevel = -1;
    EquationNode result;

    if (input.length() == 1) { // to prevent out of bounds with the following lines
      input = input.delete(0, 1);
      opLevel = getOpLevel(value);
      if (state == 0) {
        try {
          result = new EquationNode(state, Double.parseDouble(value));
        } catch (Exception e) {
          return null;
        }
      }
      else {
        result = new EquationNode(state, value);
      }
      result.opLevel = opLevel;
      return result;
    }

    counter++;
    char next = input.charAt(counter);
    byte nextState = getState(next);

    if (state == 1 && next == '(') { // edgecase for parsing functions f(
      if (controller.functionExists(value) && !name.equals(value)) {
        // check if it is a variables or function; a(x) could also mean a*(x)
        input = input.delete(0, 1);
        result = new EquationNode((byte) 4, value);
        result.opLevel = 3;
        return result;
      }
    }

    if (state == -1 ||
        (state == 2 || state == 1 &&
            (nextState != state || first == 'x' || first == 'y' || next == 'x' || next == 'y'))) {
      // should cover the following cases:
      // (,),ax,ay,x,y,+,-,*,/

      input = input.delete(0, 1);
      opLevel = getOpLevel(value);
      result = new EquationNode(state, value);
      result.opLevel = opLevel;
      return result;
    }

    // can now only be numbers an special functions
    while (nextState == state && counter < input.length()) {
      value += Character.toString(next);
      counter++;
      if (counter < input.length()) {
        next = input.charAt(counter);
        nextState = getState(next);
      }
      if (value.contains("mod")) {// edge case when 2modsin()
        break;
      }
    }
    // remove used part
    input = input.delete(0, counter);

    Set<String> specials = Set.of("abs", "sin", "cos", "tan", "ln", "sqrt");
    Set<String> operators = Set.of("root", "log");
    Set<String> all = new HashSet<String>();
    all.addAll(specials);
    all.addAll(operators);
    all.add("mod"); // since mod is handled differently

    if (state == 1) {
      // TODO Handle mod: amod
      System.out.println("is: " + value);

      for (String element : all) {
        if (value.contains(element)) {
          System.out.println(":-- " + element);
          if (value.length() > element.length()) { // to prevent error when value is only element
                                                   // fe. value = sin and not asin
            input.insert(0, element);
            value = value.split(element)[0]; // get part before special
            System.out.println("new val: " + value);
          }
        }
      }

      if (specials.contains(value)) {
        state = 3;
        opLevel = 3;
      } else if (operators.contains(value)) {
        state = specialOpMagicNum; // So that i can treat it differently -> will be changed to 2 later
        opLevel = 3; // XXX CHECK IF WORKS THAT WAY
      } else if (value.equals("mod")) { // special case for mod
        state = 2;
        opLevel = 1;
      } else { // only customVars -> add multiplikation between
        String otherVars = "";
        for (int i = 1; i < value.length(); i++) {
          otherVars += "*" + value.charAt(i);
        }
        state = 1;
        opLevel = -1;
        value = value.charAt(0) + "";
        input.insert(0, otherVars);
        System.out.println("input: " + input + " this: " + value + "|" + state + "|"
            + opLevel);
      }

      // else {
      // if (debug)
      // System.out.println("invalid input getNode(): " + value);
      // return null; // invalid input
      // }

    }

    if (state == 0) {
      try {
        result = new EquationNode(state, Double.parseDouble(value));
      } catch (Exception e) {
        return null;
      }
    }
    else {
      result = new EquationNode(state, value);
    }
    result.opLevel = opLevel;
    return result;
  }

  private static byte getOpLevel(String op) {
    String ops1 = "+*^";
    String ops2 = "-/^";
    byte ind = -1; // the index represents the level

    if (ops1.contains(op)) {
      ind = (byte) ops1.indexOf(op);
    } else {
      ind = (byte) ops2.indexOf(op);
    }
    return ind;
  }

  private static byte getState(char c) {
    String info = Character.toString(c);
    String nums = ".0123456789";
    String operators = "+-*/^";
    String brackets = "()";

    if (nums.contains(info)) {
      return 0;
    } else if (brackets.contains(info)) {
      return -1;
    } else if (operators.contains(info)) {
      return 2;
    } else {
      return 1; // could be variable or special function
    }
  }

  public static void testParser(ApplicationController controller) { // todo
    // brackets
    String test[] = { "3*2^2+1", "1+2*3^2", "2*3^sin(0)+1", "1+sin(0)*2",
        "1+1^3*3+1", "1+2*(3-1)", "(2*2+1)^2", "sin(1-1)+2*(3^(2-1))", "1+2*(1+3*3+1)",
        "3^(sin(2*cos(1/3*3-1)-2)+2)*(1/2)", "cos(sin(1-1)*2)", "sin(2*sin(2-2))", "sin(2*sin(22*0))", "root(2,64)-4",
        "root(2,root(2,64)/2)*2^1", "(x/3)^4-2(x/3)^2 -5" };

    double results[] = { 13, 19, 3, 1, 5, 5, 25, 6, 23, 4.5, 1, 0, 0, 4, 4, -5 };
    int passed = 0;
    for (int i = 0; i < test.length; i++) {
      System.out.println("-----------------");
      EquationTree root = parseString(test[i], controller);
      TwoDVec coords = new TwoDVec<Double>(0.0, 0.0);
      EquationTree[] existingFunctions = controller.getAllFunctions();
      double res = root.calculate(coords, null, existingFunctions);
      if (debug)
        System.out.println(res + " name: " + name + " isFunction: " + isFunction);
      if ((double) coords.x == -1.0) {
        if (debug)
          System.out.println("Return null");
      }
      System.out.println("calc:" + res + "  - should: " + results[i]);
      if (res == results[i]) {
        System.out.println("### TEST PASSED ###");
        passed++;
      } else {
        System.out.println("--- TEST FAILED ---");
      }
    }
    System.out.println("---- RESULTS ----");
    System.out.println("PASSED: " + test.length + "/" + passed);
    System.out.println("-----------------");
  }

}
