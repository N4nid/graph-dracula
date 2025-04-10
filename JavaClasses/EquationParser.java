import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EquationParser {
  public static boolean debug = false; // all debugging prints will be removed when there are no issues anymore
  static String name = "";
  static boolean isFunction = false;
  static boolean isParametic = false;
  static boolean simpleParsing = false;
  private static boolean parseBetweenBrackets = false;
  private static byte specialOpMagicNum = 6;
  public static ApplicationController controller;
  public static ArrayList<CustomVarUIElement> oldVarCache;

  private static String transformString(String input) {
    // Sanitize and Transform string
    // remove all spaces
    if (debug) System.out.println(input);
    input = input.replaceAll("\\s", "");
    // So that the equalSign of a condition node will not be used
    input = input.replaceAll("==", "≠");
    input = input.replaceAll("<=", "≤");
    input = input.replaceAll(">=", "≥");
    input = input.toLowerCase();
    if (input.equals("")) {
      return null;
    }

    // replace constants
    String uniConstants = "φ π"; // for some reason "Φ" will be passed as "φ"; maybe utf8 problem idk
    String constants = "phi pi e"; // must be divided by spaces for split() in getConstant
                                   // should be sorted by length to avoid replacement with shorter constants
                                   // F.e when there would be a constant like "pie" and "e" is replaced
    Double constValues[] = { 1.6180339887498948, Math.PI, Math.E };
    input = replaceConstants(input, constants, constValues);
    input = replaceConstants(input, uniConstants, constValues);

    // replace )( with )*( so something like (2+1)(x-1) works
    input = input.replaceAll("\\)\\(", ")*(");


    // handle case in which the condition is in the beginning
    // Fe. if(x<1) x^2
    if (input.length() > 4) { // so its not just if
      String sub = input.substring(0, 2);
      if (sub.equals("if")) {
        StringBuffer in = new StringBuffer(input);
        in.delete(0, 2);
        String betweenBrackets = getValuesInBrackets(in)[0];
        if(betweenBrackets == null){
          if(debug) System.out.println("Invalid condition at start");
          return null;
        }
        input = in.toString();
        input += "if("+betweenBrackets+")";
      }
    }

    // Transform
    if (!parseBetweenBrackets) {
      name = "";
      int equalSignPos = input.indexOf('=');
      if (equalSignPos == -1 && !input.contains("y")) { // fe. 3x+1
        if (!simpleParsing)
          input += "-y";
        isFunction = true;
        name = "";
        if (debug) {
          System.out.println("a function");
        }
      } else if (checkIfFunction(input)) { // fe. f(x)=x or y=x
        input = input.split("=")[1] + "-y";
        if (debug)
          System.out.println("is a function");
      } else if (equalSignPos != -1) {
        String[] split = input.split("=");
        if (split.length == 2) {
          input = split[1] + "-(" + split[0] + ")";
          isFunction = false;
          if (debug) {
            System.out.println("is not a function");
          }
        } else { // invalid input y=
          return null;
        }
      } else { // 3y+1
        isFunction = false;
        if (debug) {
          System.out.println("not a function");
        }
      }
    }

    // FIXME workaround when equation starts with brackets (20-2)*2
    char first = input.charAt(0);
    if (first == '(') {
      input = "0+" + input;
    } else if (first == '-') {
      input = "0" + input;
    }

    input = input.replaceAll("≠", "==");
    input = input.replaceAll("≤", "<=");
    input = input.replaceAll("≥", ">=");

    if (debug) System.out.println("input after TRANSFORM: " + input);
    return input;
  }

  private static String replaceConstants(String input, String constants, Double[] constValues) {
    String constArr[] = constants.split(" ");
    for (int i = 0; i < constArr.length; i++) {
      //System.out.println("Test replace: |"+constArr[i]+"|");
      input = input.replaceAll(constArr[i], "(" + constValues[i] + ")");
    }
    return input;
  }

  public static EquationTree parseString(String input, ApplicationController appController) {
    oldVarCache = new ArrayList<>();
    oldVarCache.addAll(appController.customVarList.customVars);
    if (debug) {
      for (CustomVarUIElement customVarUIElement : oldVarCache) {
        System.out.println("old: "+customVarUIElement.value);
      }
    }
    controller = appController;
    input = input.replaceAll("\\s", ""); //remove white spaces
    input = input.toLowerCase();
    if (input.length() > 8 && input.substring(1, 8).equals("(t->xy)")) {
      return parseParametics(input);
    } else {
      return parseEquation(input, appController);
    }
  }

  public static EquationTree parseParametics(String input) {
    // f(t->xy):x=(t);y=(t);for(a<t<b)
    simpleParsing = true; // so that i can call parseString without problems

    // check if input is valid
    String parts[] = input.split(";"); // part 0 and 1 are the equations; part 2 the interval
    if (!input.contains(";") || parts.length != 3) {
      System.out.println("invalid parametric input");
      return null;
    }

    EquationNode root = new EquationNode((byte) 5, "");

    // x and y parts
    if (parts[0].length() < 12 || parts[1].length() < 3) {
      System.out.println("Error: Invalid parametric input!");
      return null;
    }
    parts[0] = parts[0].substring(9); // remove the f(t->xy):
    parts[0] = parts[0].substring(3, parts[0].length() - 1); // remove = and brackets
    parts[1] = parts[1].substring(3, parts[1].length() - 1); // remove = and brackets
    EquationTree left = parseEquation(parts[0], controller);
    if (left == null || left.root == null)
      return null;
    EquationTree right = parseEquation(parts[1], controller);
    if (right == null || right.root == null)
      return null;
    root.left = left.root;
    root.right = right.root;
    EquationTree result = new EquationTree(root, name, false);

    parts[2] = parts[2].substring(4); // remove "for("
    parts[2] = parts[2].substring(0, parts[2].length() - 1); // remove ")" from for(*)
    String[] intervalString = parts[2].split("<t<");

    if (intervalString.length == 2) {
      EquationNode intervalNode = parseEquation(intervalString[0], controller).root;
      if (intervalNode == null)
        return null;
      result.intervalStart = intervalNode;

      intervalNode = parseEquation(intervalString[1], controller).root;
      if (intervalNode == null)
        return null;
      result.intervalEnd = intervalNode;
      // System.out.println(result.calculateParametrics(2, null).x);

      result.isParametric = true;
      if (debug) System.out.println(input);
      simpleParsing = false;
      result.name = "" + input.charAt(0);
      return result;
    }

    simpleParsing = false;
    return null;
  }

  public static EquationTree parseEquation(String input, ApplicationController appController) {
    if (debug && input.equals("debug")) {
      testParser(appController);
    }

    input = transformString(input);
    controller = appController;
    if (input == null) {
      return null;
    }
    if (isParametic) {
      return parseParametics(input);
    }

    EquationTree parsedEquation = new EquationTree();
    ArrayList<String> addedVars = new ArrayList<String>();

    if (debug) {
      System.out.println(input);
    }
    StringBuffer in = new StringBuffer(input); // The StringBuffer is mutable by refrence
                                               // so that i can manipulate the string in getNextNode
    EquationNode currentNode = getNextNode(in);
    int bracketDepth = 0;
    EquationNode lastNode = currentNode;
    OperatorStack operators = new OperatorStack();
    operators.debug = false;
    EquationNode root = null;

    while (currentNode != null) {
      Object val = currentNode.value;
      Byte state = currentNode.state;
      Byte opLevel = currentNode.opLevel;
      currentNode.bracketDepth = bracketDepth;

      if (state == 42) {
        parsedEquation.rangeCondition = (ConditionTree) val;
        currentNode = getNextNode(in);
        continue;
      }

      if (handleAdvancedInput(currentNode, lastNode, in)) {
        bracketDepth = currentNode.bracketDepth; // since it could have been updated
        currentNode = getNextNode(in);
        // System.out.println("doskip");
        continue;
      }

      if (state == 0 || state == 1) { // is either a num or variable
        if (debug) {
          System.out.println("NUMORVAR: " + val + "| " + lastNode.value + " " + lastNode.bracketDepth);
        }
        if (lastNode.state >= 2) {
          lastNode.right = currentNode;
        }

        if (state == 1 && controller.equationNameExists(val.toString())) {
          discardVars(addedVars);
          return null;
        }
        if (state == 1 && !(val.equals("y") || val.equals("x"))) { // handle variables
          if (!(simpleParsing && val.equals("t"))) { // since t should not be added when parsing parametics
            boolean didntExistBefore = controller.customVarList.addCustomVar(val.toString());
            if (didntExistBefore) {
              addedVars.add(val.toString());
            }
          }
        }

        lastNode = currentNode;
        // lastNode.bracketDepth = bracketDepth;
        // System.out.println("setting lastnode="+val);
        if (root == null) {
          if (debug) {
            System.out.println("setin rooot");
          }
          root = currentNode;
        }
      } else if (state == 3 || state == 4) { // is specialFunction or function
        if (debug) {
          System.out.println("current: " + val + " | " + bracketDepth);
        }
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);
        if (stackTop != null) {
          EquationNode lastOp = stackTop.elem;
          lastOp.right = currentNode;
          currentNode.above = lastOp;
          if (debug) {
            System.out.println("addBelow " + lastOp.value);
          }
        } else {
          root = currentNode;
          if (debug) {
            System.out.println("setin rooot");
          }
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
        String[] betweenBrackts = getValuesInBrackets(in);
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
            discardVars(addedVars);
            return null;
          }
          EquationNode left = parsedTree.root;

          if (debug) {
            System.out.println("---PARSING RIGHT---");
          }

          parsedTree = parseString(betweenBrackts[1], controller);
          if (parsedTree == null) {
            parseBetweenBrackets = false;
            discardVars(addedVars);
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
          if (debug) {
            System.out.println("> root: " + root.value);
          }
        }
        operators.add(currentNode, bracketDepth);
        lastNode = currentNode;

      } else if (state == 2) { // is operator (+-*/^)
        if (debug) {
          System.out.println("current: " + val + " | " + bracketDepth);
        }
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);

        if (stackTop != null) {

          if ((lastNode.bracketDepth < bracketDepth) && val.equals("-")) { // so that negative numbers work
            if (debug) {
              System.out.println("neg. number fix " + lastNode.value);
            }
            lastNode.right = new EquationNode((byte) 0, 0);
          }

          EquationNode lastOp = stackTop.elem;
          int lastDepth = lastOp.bracketDepth;

          if (bracketDepth > lastDepth
              || (lastOp.state == 2 && lastOp.opLevel < opLevel)) {
            // Either in brackets or operator is Higher
            if (debug) {
              System.out.println("addBelow " + lastOp.value);
            }
            addBelow(lastOp, currentNode);
          } else { // add current above. F.e 2*2+2
            EquationNode above = lastOp.above;
            if (debug) {
              System.out.println("add above " + lastOp.value);
            }
            currentNode.left = lastOp;
            lastOp.above = currentNode;

            if (lastOp == root) {
              if (debug) {
                System.out.println("YOOOOOO");
              }
              root = currentNode;
            }

            if (above != null) {
              if (debug) {
                System.out.println("-#- below: " + above.value);
              }
              // above.right
              above.right = currentNode;
              currentNode.above = above;
            }
          }

        } else {
          root = currentNode;
          if (debug) {
            System.out.println("> root: " + root.value);
          }
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

    if (debug) {
      operators.printStack();
    }
    if (root != null) {
      if (debug) {
        root.recursivePrint(""); // For debugging
      }
      TwoDVec coords = new TwoDVec<Double>(0.0, 0.0);
      EquationTree[] existingFunctions = controller.getAllFunctions();
      double res = root.calculate(coords, null, existingFunctions);
      if (debug) {
        System.out.println(res + " name: " + name + " isFunction: " + isFunction);
      }
      if ((double) coords.x == -1.0) {
        if (debug) {
          System.out.println("Return null");
        }
        discardVars(addedVars);
        return null;
      }
    }
    // debugging

    parsedEquation.root = root;
    parsedEquation.name = name;
    parsedEquation.isFunction = isFunction;
    return parsedEquation;
  }

  private static void discardVars(ArrayList<String> varList) {
    for (String var : varList) {
      if (debug) System.out.println("Removed: " + var);
      controller.customVarList.removeCustomVar(var);
    }
  }

  private static boolean handleAdvancedInput(EquationNode currentNode, EquationNode lastNode,
      StringBuffer input) {
    // should recognize the following and add a multiplikation inbetween:
    // 2(, )2, 2x, x2, 2sin, xsin, )sin,
    // should fix x^-1
    if (currentNode == null || lastNode == null || input == null) {
      System.out.println("NULL passed to handleAdvancedInput");
      return false;
    }

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

  public static String[] getValuesInBrackets(StringBuffer input) {
    // To get the different values seperated by "," in specialFunctions
    // fe. log(2,x) -> {"2","x"}
    if (debug) {
      System.out.println("input: " + input);
    }
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
        if (debug) {
          System.out.println("inp without brackets: " + input);
          System.out.println("vals: " + value[0] + " | " + value[1]);
        }
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
    EquationNode result = new EquationNode(state, "");

    if (input.length() == 1) { // to prevent out of bounds with the following lines
      input = input.delete(0, 1);
      opLevel = getOpLevel(value);
      if (state == 0) {
        try {
          result = new EquationNode(state, Double.parseDouble(value));
        } catch (Exception e) {
          return null;
        }
      } else {
        result = new EquationNode(state, value);
      }
      result.opLevel = opLevel;
      return result;
    }

    counter++;
    char next = input.charAt(counter);
    byte nextState = getState(next);

    if (state == 1 && next == '(') { // edgecase for parsing functions f(
      if (controller.equationNameExists(value) && !name.equals(value)) {
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
    if (value.equals("if")) { // edgecase for conditionNodes
      String[] betweenBrackets = getValuesInBrackets(input);
      if(betweenBrackets == null || betweenBrackets[0].isBlank()){
        if(debug) System.out.println("Invalid condition");
        return null;
      }
      ConditionParser ConditionParser = new ConditionParser();
      ConditionTree condition = ConditionParser.parseCondition(betweenBrackets[0], controller);      
      state = 42;
      result.value = condition;
      result.state = 42;
      return result;
    }

    Set<String> specials = Set.of("abs", "sin", "cos", "tan", "ln", "sqrt");
    Set<String> operators = Set.of("root", "log");
    Set<String> all = new HashSet<String>();
    all.addAll(specials);
    all.addAll(operators);
    all.add("mod"); // since mod is handled differently

    if (state == 1) {
      // TODO Handle mod: amod
      if (debug) {
        System.out.println("is: " + value);
      }

      for (String element : all) {
        if (value.contains(element)) {
          if (debug) {
            System.out.println(":-- " + element);
          }
          if (value.length() > element.length()) { // to prevent error when value is only element
                                                   // fe. value = sin and not asin
            input.insert(0, element);
            value = value.split(element)[0]; // get part before special
            if (debug) {
              System.out.println("new val: " + value);
            }
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
        if (debug) {
          System.out.println("input: " + input + " this: " + value + "|" + state + "|" + opLevel);
        }
      }

    }

    if (state == 0) {
      try {
        result.value = Double.parseDouble(value);
      } catch (Exception e) {
        return null;
      }
    } else {
      if (state != 42) { // is not a conditionNode
        result.value = value;
      }
    }
    result.opLevel = opLevel;
    result.state = state;
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

  public static void testParser(ApplicationController controller) {
    String test[] = { "3*2^2+1", "1+2*3^2", "2*3^sin(0)+1", "1+sin(0)*2",
        "1+1^3*3+1", "1+2*(3-1)", "(2*2+1)^2", "sin(1-1)+2*(3^(2-1))", "1+2*(1+3*3+1)",
        "3^(sin(2*cos(1/3*3-1)-2)+2)*(1/2)", "cos(sin(1-1)*2)", "sin(2*sin(2-2))", "sin(2*sin(22*0))", "root(2,64)-4",
        "root(2,root(2,64)/2)*2^1", "(x/3)^4-2(x/3)^2 -5", "x^2-2x-1" ,"2^2-x^2","cos(1/3*3-1)","x-sin(x)^sin(x)^2*4"};

    //Ergebnisse für x = 0
    //Sind nicht 100% representativ für das richtige parsen !
    double results[] = { 13, 19, 3, 1, 5, 5, 25, 6, 23, 4.5, 1, 0, 0, 4, 4, -5, -1 ,4,1,-4};
    int passed = 0;
    for (int i = 0; i < test.length; i++) {
      System.out.println("-----------------");
      EquationTree root = parseString(test[i], controller);
      TwoDVec coords = new TwoDVec<Double>(0.0, 0.0);
      EquationTree[] existingFunctions = controller.getAllFunctions();
      double res = root.calculate(coords, null, existingFunctions);
      System.out.println(res + " name: " + name + " isFunction: " + isFunction);
      if ((double) coords.x == -1.0) {
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
