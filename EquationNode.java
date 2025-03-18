public class EquationNode {
  public byte state; // 0:IsNumber, 1:IsVariable, 2:IsOperator, 3:IsSpecialFunc;
  public Object value;
  public EquationNode left;
  public EquationNode right;
  public int bracketDepth;
  public byte opLevel; // to sort operators easier later. -1 if not a operator
  public EquationNode above;

  public EquationNode(byte state, String value) {
    this.state = state;
    this.value = value;
  }

  public EquationNode(byte state, double value) {
    this.state = state;
    this.value = value;
  }

  public void recursivePrint(String helper) {
    System.out.println(helper + "   " + value);
    if (left != null) {
      this.left.recursivePrint(helper + "l");
    }
    if (right != null) {
      this.right.recursivePrint(helper + "r");
    }

  }

  public double calculate(double x, double y, Variable[] parameters) {
    if (state == 0) {
      return Double.parseDouble((String) value);
      // return (double) value;
    } else if (state == 1) {
      String varName = (String) value;
      if (varName.equals("x")) {
        return x;
      } else if (varName.equals("y")) {
        return y;
      } else {
        return readVar(parameters, varName);
      }
    } else if (state == 2 && left != null && right != null) {
      double part1 = left.calculate(x, y, parameters);
      double part2 = right.calculate(x, y, parameters);
      String op = (String) value;
      if (op.equals("+")) {
        return part1 + part2;
      } else if (op.equals("-")) {
        return part1 - part2;
      } else if (op.equals("*")) {
        return part1 * part2;
      } else if (op.equals("/")) {
        return part1 / part2;
      } else if (op.equals("^")) {
        return Math.pow(part1, part2);
      } else if (op.equals("mod")) {
        return part1%part2;
      } else if (op.equals("root")) {
        return Math.pow(part2, 1.0 / part1);
      } else if (op.equals("log")) {
        return Math.log(part2) / Math.log(part1);
      } else {
        System.out.println("Invalid Operator!");
      }
    } else if (state == 3 && right != null) {
      String op = (String) value;
      double calVal = right.calculate(x, y, parameters);
      if (op.equals("sin")) {
        return Math.sin(calVal);
      } else if (op.equals("cos")) {
        return Math.cos(calVal);
      } else if (op.equals("tan")) {
        return Math.tan(calVal);
      } else if (op.equals("ln")) {
        return Math.log(calVal);
      } else if (op.equals("abs")){
        return Math.abs(calVal);
      } else if (op.equals("sqrt")) {
        return Math.sqrt(calVal);
      } else {
        System.out.println("Invalid special function!");
      }
    } else {
      System.out.println("Invalid Node!");
    }
    return 0.0;
  }

  private static double readVar(Variable[] parameters, String varName) {
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i].name.equals(varName)) {
        return parameters[i].value;
      }
    }
    System.out.println("Error: Invalid Parameter!");
    return 0;
  }

}
