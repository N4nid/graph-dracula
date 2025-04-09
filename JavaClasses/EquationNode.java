public class EquationNode {
  public byte state; // 0:IsNumber, 1:IsVariable, 2:IsOperator, 3:IsSpecialFunc, 4:IsEquation,
                     // 5:RootOfParametric
  public Object value;
  public EquationNode left;
  public EquationNode right;
  public int bracketDepth;
  public byte opLevel; // to sort operators easier later. -1 if not a operator
  public EquationNode above;
  boolean invalid = false;

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

  public double calculate(TwoDVec<Double> realCoord, Variable[] vars, EquationTree[] existingFunctions) {
    if (state == 0) {
      return (double) value;
      // return (double) value;
    } else if (state == 1) {
      String varName = (String) value;
      if (varName.equals("x")) {
        return realCoord.x;
      } else if (varName.equals("y")) {
        return realCoord.y;
      } else {
        return readVar(vars, varName);
      }
    }
    return handleHigherStates(realCoord,vars,existingFunctions,false,0);
  }

  public double calculateParametric(double t, Variable[] vars, EquationTree[] existingFunctions) {
    if (state == 0) {
      return (Double) value;
      // return (double) value;
    } else if (state == 1) {
      String varName = (String) value;
      if (varName.equals("t")) {
        return t;
      } else {
        return readVar(vars, varName);
      }
    }
    return handleHigherStates(null,vars,existingFunctions,true,t);
  }


  private static double readVar(Variable[] vars, String varName) {
    if (vars == null) {
      return 0;
    }
    for (int i = 0; i < vars.length; i++) {
      if (vars[i].name.equals(varName)) {
        return vars[i].value;
      }
    }
    return 0;
  }

  private double handleHigherStates(TwoDVec<Double> realCoord, Variable[] vars, EquationTree[] existingFunctions, boolean isParametric, double t) {
    if (state == 2 && left != null && right != null) {
      double part1;
      double part2;
      if (isParametric) {
        part1 = left.calculateParametric(t, vars, existingFunctions);
        part2 = right.calculateParametric(t, vars, existingFunctions);
      }
      else {
        part1 = left.calculate(realCoord, vars, existingFunctions);
        part2 = right.calculate(realCoord, vars, existingFunctions);
      }
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
        return part1 % part2;
      } else if (op.equals("root")) {
        return Math.pow(part2, 1.0 / part1);
      } else if (op.equals("log")) {
        return Math.log(part2) / Math.log(part1);
      } else {
        System.out.println("Invalid Operator!");
        invalid = true;
      }
    } else if (state == 3 && right != null) {
      String op = (String) value;
      double calVal;
      if (isParametric) {
        calVal = right.calculateParametric(t,vars,existingFunctions);
      }
      else {
        calVal = right.calculate(realCoord, vars, existingFunctions);
      }
      if (op.equals("sin")) {
        return Math.sin(calVal);
      } else if (op.equals("cos")) {
        return Math.cos(calVal);
      } else if (op.equals("tan")) {
        return Math.tan(calVal);
      } else if (op.equals("ln")) {
        return Math.log(calVal);
      } else if (op.equals("abs")) {
        return Math.abs(calVal);
      } else if (op.equals("sqrt")) {
        return Math.sqrt(calVal);
      } else {
        System.out.println("Invalid special function!");
        invalid = true;
      }
    } else if (state == 4 && right != null) { // is a function
      EquationTree function = null;
      if (existingFunctions != null) {
        for (int i = 0; i < existingFunctions.length; i++) {
          if (existingFunctions[i].name.equals(value.toString())) {
            function = existingFunctions[i];
          }
        }
      }

      if (function != null) {
        TwoDVec<Double> newCoords = new TwoDVec(right.calculate(realCoord, vars, existingFunctions), realCoord.y);
        double result = 0;
        try {
          if (!invalid) {
            result = function.calculate(newCoords, vars, existingFunctions);
          }
        } catch (StackOverflowError e) {
          System.out.println("StackOverflowError DONT do recursion");
          System.out.println("bad function: " + value.toString() + "(x)");
          invalid = true;
        }
        if (!invalid)
          return result;
      } else {
        // System.out.println("WHY NOT WORKING -- Function calculate :c");
        invalid = true;
      }
    }
    else {
      System.out.println("Invalid Node! " + value + " state:" + state);
      invalid = true;
    }

    if (invalid) {
      realCoord.setUniform(-1.0); // so that i can catch an invalid EquationTree
    }

    return 0.0;
  }

}
