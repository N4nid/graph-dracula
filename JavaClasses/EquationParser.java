import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EquationParser {
  public static boolean debug = true; // all debugging prints will be removed when there are no issues anymore
  static String name = "";
  static boolean isFunction = false;
  static boolean isParametic = false;
  static boolean parametricParsing = false;
  private static boolean parseBetweenBrackets = false;
  public static ApplicationController controller;
  public static ArrayList<CustomVarUIElement> oldVarCache;
  
  private static Set<String> specialFunctions = Set.of("abs", "sin", "cos", "tan", "ln", "sqrt"); // sets because they dont have to be modified
  private static Set<String> specialOperators = Set.of("root", "log");                            // and the order does not matter
  private static Set<String> allSpecials = Set.of("root", "log","abs", "sin", "cos", "tan", "ln", "sqrt", "mod");

  //magic nums for the different states
  private static byte bracketID = -1;
  private static byte numID = 0;
  private static byte varID = 1;
  private static byte operatorID = 2;
  private static byte specialFuncID = 3;
  private static byte functionID = 4;
  private static byte parametricID = 5;
  private static byte specialOpID = 6;
  private static byte conditionID = 42;

  private static String transformString(String input) {
    // Sanitize and Transform string
    // remove all spaces
    if (debug) System.out.println(input);
    input = input.replaceAll("\\s", "");
    if (input.equals("")) {
      return null;
    }

    // So that the equalSign of a condition node will not be used
    input = input.replaceAll("==", "≠");
    input = input.replaceAll("<=", "≤");
    input = input.replaceAll(">=", "≥");
    input = input.toLowerCase();

    // replace constants
    String uniConstants = "φ π"; // for some reason "Φ" will be passed as "φ"; maybe utf8 problem idk
    String constants = "phi pi e"; // must be divided by spaces for split() in getConstant
                                   // should be sorted by length to avoid replacement with shorter constants
                                   // F.e when there would be a constant like "pie" and "e" is replaced
    Double constValues[] = { 1.6180339887498948, Math.PI, Math.E };
    input = replaceConstants(input, constants, constValues);
    input = replaceConstants(input, uniConstants, constValues);

    // replace ")(" with ")*(" so something like (2+1)(x-1) works
    input = input.replaceAll("\\)\\(", ")*(");


    // handle case in which the condition is in the beginning -> move it to the back
    // Fe. if(x<1) x^2
    if (input.length() > 4) { // so its not just "if"
      String sub = input.substring(0, 2);
      if (sub.equals("if")) {
        StringBuffer in = new StringBuffer(input);
        in.delete(0,2);
        String betweenBrackets = getBetweenBrackets(in);
        if(betweenBrackets == null){
          if(debug) System.out.println("Invalid condition at start");
          return null;
        }
        input = in.toString();
        input += "if("+betweenBrackets+")";
      }
    }

    // Transform for different equation types
    if (!parseBetweenBrackets) { // when parsing log(2,x)
                                 // the stuff between brackets "(2,x)"
                                 // cant/shouldnt be a function -> skip this
      name = "";
      int equalSignPos = input.indexOf('=');
      if (equalSignPos == -1 && !input.contains("y")) { // is a simple term fe. 3x+1
        isFunction = true;
        name = "";
        if (debug) {
          System.out.println("a function");
        }
      } else if (checkIfFunction(input)) { // is a function fe. f(x)=x or y=x
        input = input.split("=")[1];
        if (debug)
          System.out.println("is a function");
      } else if (equalSignPos != -1) { // is a equation containing y fe. x^2-y^2=9
        String[] split = input.split("=");
        if (split.length == 2) {
          input = split[1] + "-(" + split[0] + ")"; // bring one side to the other
          isFunction = false;
          if (debug) {
            System.out.println("is not a function");
          }
        } else { // invalid input
          return null;
        }
      } else { // is a term containing y fe. 3y+1
        isFunction = false;
        if (debug) {
          System.out.println("not a function");
        }
      }
    }

    // workaround when equation starts with brackets (20-2)*2
    // Have to do this since the bracketdepth for the first operator has to be 0
    char first = input.charAt(0);
    if (first == '(') {
      input = "0+" + input;
    } else if (first == '-') {
      input = "0" + input;
    }

    // "undo" previous replacing so that conditions get parsed correctly
    input = input.replaceAll("≠", "==");
    input = input.replaceAll("≤", "<=");
    input = input.replaceAll("≥", ">=");

    if (debug) System.out.println("input after TRANSFORM: " + input);
    return input;
  }

  private static String replaceConstants(String input, String constants, Double[] constValues) {
    String constArr[] = constants.split(" ");
    for (int i = 0; i < constArr.length; i++) {
      input = input.replaceAll(constArr[i], "(" + constValues[i] + ")");
    }
    return input;
  }

  public static EquationTree parseString(String input, ArrayList<EquationTree> existingFuntions, ArrayList<Variable> customVars) {
    controller = new ApplicationController();
    if (existingFuntions != null) {
      for (int i = 0; i < existingFuntions.size(); i++) {
        controller.listElements.add(new EquationVisElement(existingFuntions.get(i)));
      }
    }
    controller.customVarList = new CustomVarUIList();
    if (customVars != null) {
      for (int i = 0; i < customVars.size(); i++) {
        controller.customVarList.addCustomVar(customVars.get(i).name);
        controller.customVarList.setCustomVar(customVars.get(i).name,customVars.get(i).value);
      }
    }
    return parseString(input,controller);
  }

  public static EquationTree parseString(String input, ApplicationController appController) {
    controller = appController;

    //transform string to move a condition standing at the beginning to the back
    //so that the string can still be detected as a parametric
    parseBetweenBrackets = true; // so it will not add "-y"
    input = transformString(input);
    parseBetweenBrackets = false;

    if(input == null){
      if(debug)System.out.println("invalid inputt");
      return null;
    }

    if (input.length() > 8 && input.substring(1, 8).equals("(t->xy)")) {
      return parseParametics(input);
    } else {
      return parseEquation(input, appController);
    }
  }

  public static EquationTree parseParametics(String input) {
    // Example input:
    // f(t->xy):x=(t);y=(t);for(a<t<b)
    // f(t->xy):x=t;y=t;for(a<t<b)

    if(input == null){
      if(debug)System.out.println("invalid inputt in parseParametics");
      return null;
    }

    parametricParsing = true; // so that i can call parseString without problems

    // check if input is valid
    String parts[] = input.split(";"); // part 0 and 1 are the equations; part 2 the interval
                                       // part 0 -> x defintion and part 1 -> y defintion
    if (!input.contains(";") || parts.length != 3 || input.length() < 26) {
      System.out.println("invalid parametric input");
      return null;
    }

    // remove unecessary parts
    // turn: "f(t->xy):x=(t);y=(2t);for(a<t<b)"
    // into: {"(t)", "(2t)", "(a<t<v)"}
    String[] toRemove = {"(t->xy):x=","y=","for"};
    int[] removeTillIndex = {11,2,3};
    for (int i = 0; i < removeTillIndex.length; i++) {
      if(parts[i].length() < removeTillIndex[i]){
        System.out.println("invalid parametric input");
        return null;
      }

      String check = parts[i].substring(0,removeTillIndex[i]);
      if(check.contains(toRemove[i])){
        // .contains because the name standing before "(t->xy):x="
        // fe. g(t->xy):x=  f(t->xy):x=
        parts[i] = parts[i].substring(removeTillIndex[i]);

        if(parts[i].contains("x") || parts[i].contains("y")){
          if(debug) System.out.println("The defintion must not contain x or y");
          return null;
        }
      }
    }
    if(debug)System.out.println("---------------- "+parts[0]+"  -  "+parts[1]+"  -  "+parts[2]);


    EquationNode root = new EquationNode(parametricID, "");

    //parse x part
    EquationTree left = parseEquation(parts[0], controller);
    if (left == null || left.root == null)
      return null;
    //parse y part
    EquationTree right = parseEquation(parts[1], controller);
    if (right == null || right.root == null)
      return null;

    root.left = left.root;
    root.right = right.root;
    EquationTree result = new EquationTree(root, name, false);

    //parse interval
    parts[2] = getBetweenBrackets(new StringBuffer(parts[2]));
    if(parts[2] == null) return null;
    String[] intervalString = parts[2].split("<t<");

    if (intervalString.length == 2) {
      //parse left part of interval
      EquationNode intervalNode = parseEquation(intervalString[0], controller).root;
      if (intervalNode == null)
        return null;
      result.intervalStart = intervalNode;

      //parse right part of interval
      intervalNode = parseEquation(intervalString[1], controller).root;
      if (intervalNode == null)
        return null;
      result.intervalEnd = intervalNode;

      result.isParametric = true;
      parametricParsing = false;
      result.name = "" + input.charAt(0);
      return result;
    }

    parametricParsing = false; // reset parametric parsing
    return null;
  }

  public static EquationTree parseEquation(String input, ApplicationController appController) {
    input = transformString(input);
    if (input == null) {
      return null;
    }
    if (debug && input.contains("bug")) {
      // to quickly test a lot of different inputs
      testParser(appController);
    }

    EquationTree result = new EquationTree();
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
    EquationNode root = null;

    while (currentNode != null) {
      Object val = currentNode.value;
      Byte state = currentNode.state;
      Byte opLevel = currentNode.opLevel;
      currentNode.bracketDepth = bracketDepth;

      if (state == conditionID) {
        if(val instanceof ConditionTree){
          result.rangeCondition = (ConditionTree) val;
        }else{
          if(debug) System.out.println("Invalid condition! not instanceof ConditionTree");
        }
        currentNode = getNextNode(in);
        continue;
      }

      // handle invisible multiplikation fe. 2x -> 2*x
      if (handleAdvancedInput(currentNode, lastNode, in)) { // the StringBuffer has been modified -> update values
        bracketDepth = currentNode.bracketDepth; // since it could have been updated
        currentNode = getNextNode(in);
        continue;
      }

      if (state == numID || state == varID) { // is either a num or variable
        if (debug) {
          System.out.println("NUMORVAR: " + val + "| " + lastNode.value + " " + lastNode.bracketDepth);
        }

        // check if lastnode is some kind of operator and adds itself below
        if (lastNode.state >= operatorID) {
          lastNode.right = currentNode;
        }

        // case where there already exists a function with that name
        // but it is used like a variable
        // fe. f(x)=2 | g(x)=f*2 -> this case
        if (state == varID && controller.equationNameExists(val.toString())) {
          discardVars(addedVars); //remove all vars added by this invalid input
          return null;
        }

        if (state == varID && !(val.equals("y") || val.equals("x"))) { // handle variables
          if (!(parametricParsing && val.equals("t"))) { // since t should not be added when parsing parametics
            boolean didntExistBefore = controller.customVarList.addCustomVar(val.toString());
            if (didntExistBefore) {
              addedVars.add(val.toString());
            }
          }
        }

        lastNode = currentNode;
        if (root == null) {
          if (debug) {
            System.out.println("setin rooot");
          }
          root = currentNode;
        }
      } else if (state == specialFuncID || state == functionID) { // is specialFunction or function
        if (debug) {
          System.out.println("current: " + val + " | " + state);
        }
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);
        if (stackTop != null) {
          //add below lastOp
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

      } else if (state == bracketID) { // its a bracket
        if (val.equals("(")) {
          bracketDepth++;
        } else {
          bracketDepth--;
        }
      } else if (state == specialOpID) { // is "special" operator (root log)
        currentNode.state = operatorID; // change state because it will be treated like a operator in calculate()

        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);
        String[] betweenBrackts = getValuesInBrackets(in); // since specialOPs have 2 values in brackets; root(2,x)

        if (betweenBrackts != null) {
          parseBetweenBrackets = true;
          EquationTree parsedTree;
          if (debug) {
            System.out
                .println("leftStr: " + betweenBrackts[0] + " rightStr: " + betweenBrackts[1]);
            System.out.println("---PARSING LEFT----");
          }

          //parse left part
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

          //parse right part
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
          lastOp.right = currentNode; // add below last operator
        } else {
          root = currentNode;
          if (debug) {
            System.out.println("> root: " + root.value);
          }
        }
        operators.add(currentNode, bracketDepth);
        lastNode = currentNode;

      } else if (state == operatorID) { // is operator (+-*/^)
        if (debug) {
          System.out.println("current: " + val + " | " + bracketDepth);
        }
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);

        if (stackTop != null) {

          if ((lastNode.bracketDepth < bracketDepth) && val.equals("-")) { // so that negative numbers work
            // checks that neg. num is in brackets; (-1)
            if (debug) {
              System.out.println("neg. number fix " + lastNode.value);
            }
            lastNode.right = new EquationNode(numID, 0); // turn (-1) into (0-1)
          }

          EquationNode lastOp = stackTop.elem;
          int lastDepth = lastOp.bracketDepth;

          if (bracketDepth > lastDepth
              || (lastOp.state == operatorID && lastOp.opLevel < opLevel)) {
            // Either in brackets or operator is Higher
            // higher as in "punkt vor strich" (explained more in documentation)

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

            if (lastOp == root) { // since we add above root, this is now the new root
              if (debug) {
                System.out.println("setting root.");
              }
              root = currentNode;
            }

            if (above != null) { // so that we dont delete nodes that are already above lastOp
              if (debug) {
                System.out.println("-#- below: " + above.value);
              }
              above.right = currentNode;
              currentNode.above = above;
            }
          }

        } else { // this is the first operator -> set as root
          root = currentNode;
          if (debug) {
            System.out.println("> root: " + root.value);
          }
          if(! lastNode.equals(currentNode)){ // for the edgecase in wich there is no last node; fe. "+3"
            currentNode.left = lastNode; // add value (var, num...) below itself; fe. "2+"
          }
        }
        operators.add(currentNode, bracketDepth);
        lastNode = currentNode;
      }

      currentNode = getNextNode(in);// might be null now, dont use after this line
    }


    if (debug) {
      operators.printStack();
    }
    if (root != null) {
      if (debug) {
        root.recursivePrint(""); // to print the tree
      }
      // testing if the tree is valid through a test calculation
      TwoDVec coords = new TwoDVec<Double>(0.0, 0.0);
      EquationTree[] existingFunctions = controller.getAllFunctions();
      double res = root.calculate(coords, null, existingFunctions);

      if (debug) {
        System.out.println(res + " name: " + name + " isFunction: " + isFunction);
      }

      // since technically everything could be a result
      // the TwoDVec coords, which is passed by refrence
      // will be changed in calculate() if the tree is invalid.
      if ((double) coords.x == -1.0) {
        if (debug) {
          System.out.println("Return null");
        }
        discardVars(addedVars);
        return null;
      }
    }

    result.root = root;
    result.name = name;
    result.isFunction = isFunction;
    return result;
  }

  private static void discardVars(ArrayList<String> varList) {
    // to have a method of removing all the vars added by invalid input
    for (String var : varList) {
      if (debug) System.out.println("Removed: " + var);
      controller.customVarList.removeCustomVar(var);
    }
  }

  private static boolean handleAdvancedInput(EquationNode currentNode, EquationNode lastNode,
      StringBuffer input) {
    // should recognize the following and add a multiplikation inbetween:
    // 2(, )2, 2x, x2, 2sin, xsin, )sin,
    // should also fix x^-1

    if (currentNode == null || lastNode == null || input == null) {
      System.out.println("NULL passed to handleAdvancedInput");
      return false;
    }

    byte state = currentNode.state;

    if (currentNode.value.equals("-") && lastNode.value.equals("^")) { // checks for this case: "^-"
      StringBuffer testBuffer = new StringBuffer(input); // so the actual buffer does not get modified
      EquationNode nextNode = getNextNode(testBuffer);

      if (nextNode.state == numID) { // only works for numbers
        input.setLength(0);
        input.append("(0-" + nextNode.value + ")" + testBuffer); // fix negative num
        return true;
      }
    }

    // cant do add the multiplikation -> return
    if (lastNode.equals(currentNode) || state == operatorID || state == bracketID || lastNode.state >= operatorID || lastNode.state == bracketID) {
      return false;
    }

    int bracketDepth = currentNode.bracketDepth;
    Object val = currentNode.value;

    // handeling brackets
    if (lastNode.bracketDepth != bracketDepth) {
      if (lastNode.bracketDepth < bracketDepth) { // before bracket 2(
        input.insert(0, "*(" + val);
        currentNode.bracketDepth--;
      } else {
        input.insert(0, "*" + val); // after bracket )2
      }
    } else if (lastNode.state != state || val.equals("x") || lastNode.value.equals("x")) { // handle 2x x2 ax xa
      input.insert(0, "*" + val);
    }

    // return true if the StringBuffer has been modified and it should be "reparsed" in parseEquation
    return true;
  }

  private static boolean checkIfFunction(String input) {
    // only one char functions and also y=x
    // f(x) and not wow(x)

    //check for y=
    if (input.length() < 3) { // so its not just y=
      return false;
    }
    String sub = input.substring(0, 2); // should be y=
    if (sub.equals("y=") && !input.split("=")[1].contains("y")) { // cant also have y on the other side; y=2y
      name = "";
      isFunction = true;
      return true;
    }

    //check for *(x)=
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
  public static String getBetweenBrackets(StringBuffer input) {
    // To get the string between brackets 
    // fe. if(1<x<pi) -> "1<x<pi"
    if (debug) {
      System.out.println("input: " + input);
    }
    int depth = 1;
    char current;
    String between = "";

    for (int i = 1; i < input.length(); i++) { // start at 1 as to skip the first bracket
      current = input.charAt(i);
      if (current == '(') {
        depth++;
      } else if (current == ')') {
        depth--;
      }

      if (depth == 0) { // the matching bracket has been found
        input = input.delete(0, i + 1);
        if (debug) {
          System.out.println("inp without brackets: " + input);
        }
        return between;
      }
      between += Character.toString(current);
    }
    return null;
  }

  public static String[] getValuesInBrackets(StringBuffer input) {
    // To get the different values seperated by "," in specialFunctions
    // fe. log(2,x) -> {"2","x"}
    // Necessary since getBetweenBrackets() would not work with specialFunctions in specialFunctions
    // fe. log(log(2,5),x)
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

      if (depth == 0) { // matching bracket has been found
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
    int index = 0;
    char first = input.charAt(0);
    String value = Character.toString(first);
    byte state = getState(first);
    byte opLevel = -1;
    EquationNode result = new EquationNode(state, "");

    if (input.length() == 1) { // to prevent out of bounds with the following lines
      input = input.delete(0, 1);
      opLevel = getOpLevel(value);
      if (state == numID) {
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

    index++;
    char next = input.charAt(index);
    byte nextState = getState(next);

    //state = varID means its just letters and not numbers
    if (state == varID && next == '(') { // edgecase for parsing functions f(
      if (controller.equationNameExists(value) && !name.equals(value)) {
        // check if it is a variables or function; a(x) could also mean a*(x)
        input = input.delete(0, 1);
        result = new EquationNode(functionID, value);
        result.opLevel = 3;
        return result;
      }
    }

    if (state == bracketID ||
        (state == operatorID || state == varID &&
            (nextState != state || first == 'x' || first == 'y' || next == 'x' || next == 'y'))) {
      // getting other one character long values
      // should cover the following cases:
      // (,),ax,ay,x,y,+,-,*,/

      input = input.delete(0, 1);
      opLevel = getOpLevel(value);
      result = new EquationNode(state, value);
      result.opLevel = opLevel;
      return result;
    }

    // can now only be numbers an special functions
    while (nextState == state && index < input.length()) {
      value += Character.toString(next);
      index++;
      if (index < input.length()) {
        next = input.charAt(index);
        nextState = getState(next);
      }
      if (value.contains("mod")) {// edge case when 2modsin()
        break;
      }
    }
    // remove parsed part as to be able to parse the rest of the input
    // fe. "sin(x)+2" -> "(x)+2"
    input = input.delete(0, index);

    if (value.equals("if")) { // edgecase for conditionNodes; if(y<9)
      String betweenBrackets = getBetweenBrackets(input);
      if(betweenBrackets == null || betweenBrackets.isBlank()){
        if(debug) System.out.println("Invalid condition");
        return null;
      }
      String backupName = name; // would otherwise be reset by parsing the condition
      boolean backupIsFunction = isFunction;

      ConditionParser conditionParser = new ConditionParser();
      ConditionTree condition = conditionParser.parseCondition(betweenBrackets, controller);

      // reset values back to their original value
      isFunction = backupIsFunction; 
      name = backupName;

      state = conditionID;
      result.value = condition;
      result.state = conditionID;
      return result;
    }

    if (state == varID) {
      // TODO Handle mod: amod
      if (debug) {
        System.out.println("is: " + value);
      }

      for (String element : allSpecials) { // get variables before specialFunctions
        if (value.contains(element)) {
          if (debug) {
            System.out.println(":-- " + element);
          }
          if (value.length() > element.length()) { // to prevent error when value is only element
                                                   // fe. value = sin and not asin

            input.insert(0, element); // add the special back to input buffer 
                                      // fe. asin(x) -> (x) -> sin(x)

            value = value.split(element)[0]; // get part before special; in the above example that would be "a"
            if (debug) {
              System.out.println("new val: " + value);
            }
          }
        }
      }

      if (specialFunctions.contains(value)) {
        state = specialFuncID;
        opLevel = 3;
      } else if (specialOperators.contains(value)) {
        state = specialOpID; // So that i can treat it differently -> will be changed to operatorID later
        opLevel = 3;
      } else if (value.equals("mod")) { // special case for mod
        state = operatorID;
        opLevel = 1;
      } else { // only customVars -> add multiplikation between
        String otherVars = "";
        for (int i = 1; i < value.length(); i++) { // start at 1 as to skip the first var
          otherVars += "*" + value.charAt(i);
        }
        state = varID;
        opLevel = -1;
        value = value.charAt(0) + "";
        input.insert(0, otherVars); // add variables with multiplikation in between back
        if (debug) {
          System.out.println("input: " + input + " this: " + value + "|" + state + "|" + opLevel);
        }
      }

    }

    if (state == numID) {
      try {
        result.value = Double.parseDouble(value);
      } catch (Exception e) {
        return null;
      }
    } else {
      if (state != conditionID) { // is not a conditionNode since they have the condition as value
        result.value = value;
      }
    }
    result.opLevel = opLevel;
    result.state = state;
    return result;
  }


  private static byte getOpLevel(String op) {
    // the level determines the order of operators.
    // if the operator will get added above or below another one
    // -> see parseEquation for detail of the tree building

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
    //the state represents of what type it is
    //fe. is it a number or operator

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

  public static void testParser(ApplicationController controller) { // For debug purposes
    //!!! results of this test are not 100% representative for correct parsing.
    //This rather serves as a quick way of testing if things work at least at a fundamental level

    String test[] = { "3*2^2+1", "1+2*3^2", "2*3^sin(0)+1", "1+sin(0)*2",
        "1+1^3*3+1", "1+2*(3-1)", "(2*2+1)^2", "sin(1-1)+2*(3^(2-1))", "1+2*(1+3*3+1)",
        "3^(sin(2*cos(1/3*3-1)-2)+2)*(1/2)", "cos(sin(1-1)*2)", "sin(2*sin(2-2))", "sin(2*sin(22*0))", "root(2,64)-4",
        "root(2,root(2,64)/2)*2^1", "(x/3)^4-2(x/3)^2 -5", "x^2-2x-1" ,"2^2-x^2","cos(1/3*3-1)","x-sin(x)^sin(x)^2*4"};

    //results for x = 0
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
