public class EquationParser {
  public static boolean debug = true; // all debugging prints will be removed when there are no issues anymore
  static String name = "";
  static boolean isFunction = false;

  // this code could also be in Main.java
  // TODO
  // - Sanitize String
  // - Check if valid
  // - Deal with brackets and sings
  // - figure out what todo with variables
  // - special functions


  private static String transformString(String input){
    //Sanitize string
    input = input.replaceAll("\\s","");

    //Transform
    if(!input.contains("=") && !input.contains("y")){
      input += "-y";
      isFunction = false;
    }
    else if(checkIfFunction(input)){
      input = input.split("=")[1]+"-y";
    }else if(input.contains("=")){
      String[] split = input.split("=");
      input = split[0]+"-("+split[1]+")";
    }

    // FIXME workaround when equation starts with brackets (20-2)*2
    char first = input.charAt(0);
    if (first == '(') {
      input = "0+" + input;
    } else if (first == '-') {
      input = "0" + input;
    }
    return input;
  }

  public static EquationTree parseString(String input) {
    input = transformString(input);

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

      if (state == 0 || state == 1) { // is either a num or variable
        if (lastNode.state >= 2) {
          lastNode.right = currentNode;
        }
        lastNode = currentNode;
        if (root == null) {
          if (debug)
            System.out.println("setin rooot");
          root = currentNode;
        }
      } else if (state == 3) {
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
      } else if (state == 4) {
        currentNode.state = 2; // change state because it will be treated like a operator in calculate()
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);
        String[] betweenBrackts = getBetweenBrackets(in);
        if (betweenBrackts != null) {
          if (debug) {
            System.out
                .println("leftStr: " + betweenBrackts[0] + " rightStr: " + betweenBrackts[1]);
            System.out.println("---PARSING LEFT----");
          }
          EquationNode left = parseString(betweenBrackts[0]).root;
          if (debug)
            System.out.println("---PARSING RIGHT---");
          EquationNode right = parseString(betweenBrackts[1]).root;
          if (debug) {
            System.out.println("---PARSING DONE----");
            System.out.println("left: " + left.value + " right: " + right.value);
          }
          currentNode.right = right;
          currentNode.left = left;
        } else if (debug) {
          System.out.println("YOO Somethin wong da bwackts no woking");
        }

        if (stackTop != null) {
          EquationNode lastOp = stackTop.elem;
          addBelow(lastOp, currentNode);

        } else {
          root = currentNode;
          if (debug)
            System.out.println("> root: " + root.value);
        }
        operators.add(currentNode, bracketDepth);
        lastNode = currentNode;

      } else if (state == 2) {
        if (debug)
          System.out.println("current: " + val + " | " + bracketDepth);
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);

        if (stackTop != null) {

          if (lastNode.value.equals("(") && val.equals("-")) { // so that negative numbers work
            if (debug)
              System.out.println("neg. number fix " + lastNode.value);
            lastNode.right = new EquationNode((byte) 0, "0");
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
      if (root != null) {
        root.recursivePrint(""); // For debugging
        double res = root.calculate(new TwoDVec<Double>(0.0,0.0), new Variable[1]);
        System.out.println(res);
      }
    }
    // debugging

    return new EquationTree(root,name,isFunction);
  }

  private static boolean checkIfFunction(String input){
    //only one char functions 
    // f(x) and not wow(x)
    String sub = input.substring(1,5); // should be (x)=
    if(sub.equals("(x)=")){
      name = input.charAt(0)+"(x)";
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
        System.out.println("inp: " + input);
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
    if(value.equals("e")){
      value = Math.E+"";
      state = 0;
    }

    if (input.length() == 1) { // to prevent out of bounds with the following lines
      input = input.delete(0, 1);
      opLevel = getOpLevel(value);
      result = new EquationNode(state, value);
      result.opLevel = opLevel;
      return result;
    }

    counter++;
    char next = input.charAt(counter);
    byte nextState = getState(next);

    if (state == -1 || (state == 2 || state == 1 && nextState != state)) {// is a operator or variable -> no further
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
    }
    // remove used part
    input = input.delete(0, counter);

    String specials = "sin cos tan ln sqrt";
    String constants = "pi phi"; // must be divided by spaces for split() in getConstant
    Double constValues[] = {Math.PI,1.6180339887498948}; // must be divided by spaces for split() in getConstant
    String operators = "root log";
    if (state == 1) {
      if (specials.contains(value)) {
        state = 3;
        opLevel = 3;
      } else if (operators.contains(value)) {
        state = 4; // So that i can treat it differently -> will be changed to 2 later
        opLevel = 3; // XXX CHECK IF WORKS THAT WAY
      } else if(constants.contains(value)){
        state = 0;
        String consts[] = constants.split(" ");
        for (int i = 0; i < consts.length; i++) {
          if(value.equals(consts[i])){
            value = constValues[i]+"";
            break;
          }
        }
      }else {
        return null; // invalid input
      }
    }

    result = new EquationNode(state, value);
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

}
