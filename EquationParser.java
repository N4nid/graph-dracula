
public class EquationParser {
  // this code could also be in Main.java
  // TODO
  // - Sanitize String
  // - Check if valid
  // - Deal with brackets and sings
  // - figure out what todo with variables

  public EquationTree parseString(String input) {
    input = "0+" + input; // FIXME workaround when equation starts with brackets (20-2)*2
    System.out.println(input);
    StringBuffer in = new StringBuffer(input); // The StringBuffer is mutable by refrence
                                               // so that i can manipulate the string in getNextNode
    EquationNode currentNode = getNextNode(in);
    boolean inBracket = false;
    EquationNode lastNode = currentNode;
    EquationNode lastOp = null;
    EquationNode root = null;

    // System.out.println(opIsHigher("-", "+"));

    while (currentNode != null) {
      Object val = currentNode.value;
      Byte state = currentNode.state;
      System.out.println(" " + val + " |state: " + state);

      if (state == 0 || state == 1) { // is either a num or variable
        if (lastNode.state == 2) {
          System.out.println("right " + val + " to: " + lastNode.value);
          lastNode.right = currentNode;
        }
        lastNode = currentNode;
      } else if (state == -1) { // its a bracket
        if (val.equals("(")) {
          inBracket = true;
          System.out.println("in klammer");
        } else {
          inBracket = false;
          System.out.println("klammer ende");
        }
      } else if (state == 2) {

        if (lastOp != null) {
          if (inBracket) {
            System.out.println("addBelow cuz klammer");
            addBelow(lastOp, currentNode);
          } else if (opIsHigher((String) lastOp.value, (String) val)) { // F.e 2+2*2
            // add below
            System.out.println("addBelow opIsHigher");
            addBelow(lastOp, currentNode);

          } else { // add current above. F.e 2*2+2
            System.out.println("add above");
            currentNode.left = lastOp; // could blow up maybe?

            lastOp = currentNode; // only set lastOp if it is bigger (excluding brackets)
                                  // since you always add above/below the "biggest" operator
          }

        } else {
          lastOp = currentNode;
          root = currentNode;
          currentNode.left = lastNode;
        }
        lastNode = currentNode;

      }

      currentNode = getNextNode(in);// might be null now, dont use after this line
    }
    root = lastOp;
    root.recursivePrint("");
    System.out.println(root.calculate(0, 0, new Variable[1]));

    return new EquationTree();
  }

  private void addBelow(EquationNode lastNode, EquationNode currentNode) {
    EquationNode lastRight = lastNode.right;
    lastNode.right = currentNode;
    currentNode.left = lastRight; // could blow up maybe?
  }

  private boolean opIsHigher(String op1, String op2) {
    String ops1 = "+*^";
    String ops2 = "-/^";
    int ind1;
    int ind2;

    if (ops1.contains(op1)) {
      ind1 = ops1.indexOf(op1);
    } else {
      ind1 = ops2.indexOf(op1);
    }

    if (ops1.contains(op2)) {
      ind2 = ops1.indexOf(op2);
    } else {
      ind2 = ops2.indexOf(op2);
    }

    return ind1 < ind2;

  }

  // private EquationNode recursiveAdd(EquationNode prevNode, StringBuffer input)
  // {
  // EquationNode left = getNextNode(input);
  // EquationNode op = getNextNode(input);
  // EquationNode right = getNextNode(input);

  //// return EquationNode();
  // }

  private EquationNode getNextNode(StringBuffer input) {
    // System.out.println("in: " + input);
    if (input.length() == 0) {
      // System.out.println("autch");
      return null;
    }
    int counter = 0;
    char first = input.charAt(counter);
    byte state = getState(first);

    if (input.length() == 1) { // to prevent out of bounds with the following lines
      // System.out.println("wowi");
      input = input.delete(0, 1);
      return new EquationNode(state, Character.toString(first));
    }

    counter++;
    char next = input.charAt(counter);
    byte nextState = getState(next);

    if (state == 2 || state == 1 && nextState != state) {// is a operator or variable -> no further iteration necessary
      // System.out.println(input);
      input = input.delete(0, 1);
      // System.out.println("asd");
      // System.out.println(input);

      return new EquationNode(state, Character.toString(first));
    }

    // can now only be numbers an special functions
    String value = Character.toString(first);
    while (nextState == state && counter < input.length()) {
      // System.out.println(value);
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
      state = 3; // correct state since getState only looks at one char
                 // -> both variables and special func. use letters
      if (!specials.contains(value)) {
        return null; // invalid input
      }
    }

    return new EquationNode(state, value);
  }

  private byte getState(char c) {
    String info = Character.toString(c);
    String nums = "0123456789";
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
