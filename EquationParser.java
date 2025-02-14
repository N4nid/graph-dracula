public class EquationParser {
  // this code could also be in Main.java
  // TODO
  // - Sanitize String
  // - Check if valid
  // - Deal with brackets and sings
  // - figure out what todo with variables
  // - special functions
  public EquationTree parseString(String input) {
    // FIXME workaround when equation starts with brackets (20-2)*2
    char first = input.charAt(0);
    if (getState(first) == -1) {
      input = "0+" + input;
    }

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
      // System.out.println(" " + val + " |state: " + state);

      if (state == 0 || state == 1) { // is either a num or variable
        if (lastNode.state >= 2) {
          // System.out.println("right " + val + " to: " + lastNode.value);
          lastNode.right = currentNode;
        }
        lastNode = currentNode;
      } else if (state == 3) {
        System.out.println("current: " + val + " | " + bracketDepth);
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);
        if (stackTop != null) {
          EquationNode lastOp = stackTop.elem;
          lastOp.right = currentNode;
          currentNode.above = lastOp;
          System.out.println("addBelow " + lastOp.value);
          // System.out.println("add beleow yo");
        } else {
          root = currentNode;
          System.out.println("setin rooot");
        }
        lastNode = currentNode;
        operators.add(currentNode, bracketDepth);

      } else if (state == -1) { // its a bracket
        if (val.equals("(")) {
          bracketDepth++;
          // System.out.println("in klammer");
        } else {
          bracketDepth--;
          // System.out.println("klammer ende");
        }
      } else if (state == 2) {
        System.out.println("current: " + val + " | " + bracketDepth);
        OperatorStackElement stackTop = operators.getLast(bracketDepth, opLevel);

        if (stackTop != null) {
          EquationNode lastOp = stackTop.elem;
          int lastDepth = lastOp.bracketDepth;

          if (bracketDepth > lastDepth
              || (lastOp.state == 2 && lastOp.opLevel < opLevel)) {
            // Either in brackets or operator is Higher
            System.out.println("addBelow " + lastOp.value);
            addBelow(lastOp, currentNode);
          } else { // add current above. F.e 2*2+2
            EquationNode above = lastOp.above;
            System.out.println("add above " + lastOp.value);
            currentNode.left = lastOp;
            lastOp.above = currentNode;

            if (lastOp == root) {
              System.out.println("YOOOOOO");
              root = currentNode;
            }

            if (above != null) {
              System.out.println("-#- below: " + above.value);
              // above.right
              above.right = currentNode;
              currentNode.above = above;
            }
          }

        } else {
          root = currentNode;
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
    if (root.left != null) {
      // System.out.println("root.left: " + root.left.value);
      if (root.left.state == 0) {
        if (Double.parseDouble((String) root.left.value) == 0) {
          // System.out.println("fixin ye");
          root = root.right;
        }
      }
    }

    operators.printStack();
    root.recursivePrint("");
    System.out.println(root.calculate(0, 0, new Variable[1]));

    return new EquationTree(root);
  }

  private void addBelow(EquationNode lastNode, EquationNode currentNode) {
    EquationNode lastRight = lastNode.right;
    lastNode.right = currentNode;
    currentNode.left = lastRight;
    currentNode.above = lastNode;
  }

  private EquationNode getNextNode(StringBuffer input) {
    if (input.length() == 0) {
      // System.out.println("autch");
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

    String specials = "sin cos tan ln";
    if (state == 1) {
      if (!specials.contains(value)) {
        return null; // invalid input
      }
      state = 3; // correct state since getState only looks at one char
                 // -> both variables and special func. use letters
      opLevel = 3;
    }

    result = new EquationNode(state, value);
    result.opLevel = opLevel;
    return result;
  }

  private byte getOpLevel(String op) {
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

  private byte getState(char c) {
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
