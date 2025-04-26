import java.util.ArrayList;
import java.util.Set;


public class EquationParser {
  public static boolean debug = true; // all debugging prints will be removed when there are no issues anymore
  static String name = "";
  static boolean isFunction = false;
  static boolean isParametic = false;
  static boolean parametricParsing = false;
  static boolean parametricIntervalParsing= false;
  private static boolean parseBetweenBrackets = false;
  public static ApplicationController controller;
  public static ArrayList<CustomVarUIElement> oldVarCache;
  
  private static Set<String> specialFunctions = Set.of("abs", "sin", "cos", "tan", "ln", "sqrt"); // sets because they dont have to be modified
  private static Set<String> specialOperators = Set.of("root", "log");                            // and the order does not matter; also O(1) access time is nice
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

  private static String transformString(String input) {
    if(input == null){
      return null;
    }
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

    // Transform for different equation types
    if (!parseBetweenBrackets) { // when parsing log(2,x)
                                 // the stuff between brackets "(2,x)"
                                 // cant/shouldnt be a function -> skip this

      String inputToCheck;
      String splitInput[] = null;
      if(input.contains(";")){ // input contains a condition
        splitInput = input.split(";");
        inputToCheck = splitInput[0]; // split input because conditon could contain "y"
                                            // which would mess with the below logic
      }else{
        inputToCheck = input;
      }

      name = "";
      int equalSignPos = inputToCheck.indexOf('=');
      if (equalSignPos == -1 && !inputToCheck.contains("y")) { // is a simple term fe. 3x+1
        isFunction = true;
        name = "";
        if (debug) {
          System.out.println("a function");
        }
      } else if (checkIfFunction(inputToCheck)) { // is a function fe. f(x)=x or y=x
        inputToCheck = inputToCheck.split("=")[1];
        if (debug)
          System.out.println("is a function");
      } else if (equalSignPos != -1) { // is a equation containing y fe. x^2-y^2=9
        String[] split = inputToCheck.split("=");
        if (split.length == 2) {
          inputToCheck = split[1] + "-(" + split[0] + ")"; // bring one side to the other
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

      input = inputToCheck;
      if(splitInput != null && splitInput.length >= 2){
        input += ";"+splitInput[1]; // since only one condition is allowed
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
    if(controller == null){
      return null;
    }

    //transform string to move a condition standing at the beginning to the back
    //so that the string can still be detected as a parametric
    parseBetweenBrackets = true; // to skip some transforming
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


    // check if input is valid
    String parts[] = input.split(";"); // part 0 and 1 are the equations; part 2 the interval
                                       // part 0 -> x defintion and part 1 -> y defintion
    if (!input.contains(";") || parts.length < 3 || parts.length > 4 || input.length() < 26) {
      System.out.println("invalid parametric input");
      return null;
    }


    // remove unecessary parts
    // turn: "f(t->xy):x=(t);y=(2t);for(a<t<b)"
    // into: {"(t)", "(2t)", "(a<t<b)"}
    String[] toRemove = {"(t->xy):x=","y=","for"};
    int[] removeTillIndex = {11,2,3};
    for (int i = 0; i < removeTillIndex.length; i++) {
      if(parts[i].length() < removeTillIndex[i]){
        System.out.println("invalid parametric input!");
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
    EquationTree result = new EquationTree(root, name, false);

    //parse possible condition
    if(parts.length == 4){ // there is a condition at the end (presumably)
      String[] split = parts[3].split("if");
      if(split.length != 2){ // there is no condition
        if(debug)System.out.println("Invalid condition!!");
        return null;
      }
      if(debug)System.out.println("CONDITION: "+split[1]); // split[1] is the part with the condition; (y<2)
      ConditionTree condition = parseCondtionString(split[1]);

      if(condition == null){
        if(debug)System.out.println("Invalid condition!");
        return null;
      }

      result.rangeCondition = condition;
    }

    parametricParsing = true; // so that i can call parseString without problems

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

    //parse interval
    parts[2] = getBetweenBrackets(parts[2]);
    if(parts[2] == null) return null;
    String[] intervalString = parts[2].split("<t<");

    if (intervalString.length == 2) {
      //parse left part of interval
      EquationNode intervalNode = parseParametricInterval(intervalString[0], controller);
      if(intervalNode == null)
        return null;
      result.intervalStart = intervalNode;

      //parse right part of interval
      intervalNode = parseParametricInterval(intervalString[1], controller);
      if(intervalNode == null)
        return null;
      result.intervalEnd = intervalNode;

      result.isFunction = false;
      result.isParametric = true;
      parametricParsing = false; // reset parametric parsing so other input wont be affected by this flag
      result.name = "" + input.charAt(0);
      return result;
    }

    parametricParsing = false;
    return null;
  }

  private static EquationNode parseParametricInterval(String input, ApplicationController controller){
    if(input == null|| controller == null){
      return null;
    }

    parametricIntervalParsing = true; // so the interval cannot be dependend on "t"

    EquationTree intervalTree = parseEquation(input, controller);

    if (intervalTree == null || intervalTree.root == null){
      parametricIntervalParsing = false;
      return null;
    }
    parametricIntervalParsing = false;
    return intervalTree.root;
  }

  private static ConditionTree parseCondtionString(String conditionString){
    String betweenBrackets = getBetweenBrackets(conditionString); // (x<9) -> x<9
    if(betweenBrackets == null || betweenBrackets.isBlank()){
      if(debug) System.out.println("Invalid condition");
      return null;
    }

    if(conditionString.length() > betweenBrackets.length()+2){ 
      // there is something after the condition -> invalid input
      // fe. "x^2;if(y<2)2x" would be one such case
      return null;
    }

    String backupName = name; // would otherwise be set to another value by parsing the condition
    boolean backupIsFunction = isFunction;
    boolean backupIsParametric = isParametic;

    ConditionParser conditionParser = new ConditionParser();
    ConditionTree condition = conditionParser.parseCondition(betweenBrackets, controller);

    // reset values back to their original value
    isFunction = backupIsFunction;
    isParametic = backupIsParametric;
    name = backupName;

    if(condition == null){
      if(debug) System.out.println("invalid condition");
      return null;
    }
    return condition;
  }

  public static EquationTree parseEquation(String input, ApplicationController appController) {
    input = transformString(input);
    if (input == null|| controller == null) {
      return null;
    }else if(input.contains("(t->xy):x")){ // happens when something is infront of the parametric; fe. phi
      if(debug)System.out.println("[!] parametric in equation");
      return null;
    }

    if (debug && input.contains("bug")) {
      // to quickly test a lot of different inputs
      testParser(appController);
    }

    EquationTree result = new EquationTree();

    if(input.contains(";if")){ // input contains a condition -> set result.rangeCondition
      String[] split = input.split(";if");
      if(split.length <= 1) return null; // to catch if there is nothing before or after ";if"

      String conditionString = split[1];
      if(debug)System.out.println("CONDITION: "+conditionString);
      ConditionTree condition = parseCondtionString(conditionString);

      if(condition == null){
        if(debug)System.out.println("invalid condition!");
        return null;
      }

      result.rangeCondition = condition;

      input = split[0];// input without condition
    }

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
        if(state == varID && val.equals("t") && parametricIntervalParsing){ 
          // since the interval of parametrics must not contain "t" 
          // but since other valid strings exists also containig "t" it has to be checked here
          // fe. "root(2,64)" is a valid interval but contains "t"
          return null;
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
          if (!(parametricParsing && val.equals("t"))) { // since t should not be added as a variable when parsing parametics
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

  public static String getBetweenBrackets(String input) {
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


    if (state == varID) {
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
      result.value = value;
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
      return numID;
    } else if (brackets.contains(info)) {
      return bracketID;
    } else if (operators.contains(info)) {
      return operatorID;
    } else {
      return varID; // could be variable or special function
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
