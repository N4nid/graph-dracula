import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Main programm = new Main();
    if (args.length >= 1) {
      System.out.println("--- MODE: " + args[0]);
      switch (args[0]) {
        case "test":
          programm.testParser();
          break;

        case "demo":
          programm.interactiveDemo();
          break;
        case "debug":
          programm.quickDebug();
          break;

        default:
          System.out.println("Use one of the following args: test, demo, debug");
          break;
      }
    }
  }

  public void quickDebug() {
    EquationParser parser = new EquationParser();
    // String test = "1+3*3-1";
    // String test = "1+3*3^2";
    // String test = "cos(sin(1-1)*2.4)";
    String test = "4*4^5+cos(4^2)-1/2*sin(4)+1/4*(4+1)";
    // String test = "sin(((4^(3))/32)-2*cos(16/31+108/31-4))+42";
    // String test = "(1-1)+2*(3^(2-1))";
    // String test = "1+2*(1+3*3+1)";
    // String test = "cos(sin(1/3)+1)";
    // String test = "3^(cos(sin(1/3)+1))*(1/2)";
    // String test = "3^(3+3)*2";
    // String test = "2+3^(23-1/2)";
    // String test = "x^3+34/6.5-1/(a^(sin(56)))*3";
    parser.parseString(test);
  }

  public void interactiveDemo() {
    EquationParser parser = new EquationParser();
    String inp = "";
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to the StringParser. Available commads:");
    System.out.println("exit, clear");

    while (!inp.contains("exit")) {
      System.out.println("Enter equation: ");
      inp = scanner.nextLine();
      if (inp.contains("clear")) {
        System.out.print("\033\143");
      } else if (!inp.contains("exit")) {
        try {
          parser.parseString(inp);
        } catch (Exception e) {
          System.out.println("whopsies");
        }
      } else {
        break;
      }

    }
    scanner.close();
  }

  public void testParser() { // todo
    EquationParser parser = new EquationParser();
    // brackets
    String test[] = { "3*2^2+1", "1+2*3^2", "2*3^sin(0)+1", "1+sin(0)*2",
        "1+1^3*3+1", "1+2*(3-1)", "(2*2+1)^2", "sin(1-1)+2*(3^(2-1))", "1+2*(1+3*3+1)",
        "3^(sin(2*cos(1/3*3-1)-2)+2)*(1/2)", "cos(sin(1-1)*2)", "sin(2*sin(2-2))", "sin(2*sin(22*0))" };
    double results[] = { 13, 19, 3, 1, 5, 5, 25, 6, 23, 4.5, 1, 0, 0 };
    for (int i = 0; i < test.length; i++) {
      System.out.println("-----------------");
      EquationTree root = parser.parseString(test[i]);
      double res = root.calculate(0, 0, new Variable[1]);
      System.out.println("calc:" + res + "  - should: " + results[i]);
      if (res == results[i]) {
        System.out.println("### TEST PASSED ###");
      } else {
        System.out.println("--- TEST FAILED ---");
      }
    }
  }

  public static EquationTree buildTestEquation() { // 2+4*4
    EquationNode root = new EquationNode((byte) 2, "+");
    root.left = new EquationNode((byte) 0, 3);
    root.right = new EquationNode((byte) 2, "*");
    root.right.left = new EquationNode((byte) 0, 4);
    root.right.right = new EquationNode((byte) 0, 4);
    return new EquationTree(root);
  }

  public static EquationTree buildTestFunction() { // 0 = x^2 -y
    EquationNode root = new EquationNode((byte) 2, "-");
    root.left = new EquationNode((byte) 2, "^");
    root.right = new EquationNode((byte) 1, "y");
    root.left.left = new EquationNode((byte) 1, "x");
    root.left.right = new EquationNode((byte) 0, 2);
    return new EquationTree(root);
  }

  public static EquationTree buildComplicatedTestFunction() { // 0 = ln(sin(sqrt(2x))-y
    EquationNode root = new EquationNode((byte) 2, "-");
    root.left = new EquationNode((byte) 3, "ln");
    root.right = new EquationNode((byte) 1, "y");
    root.left.left = new EquationNode((byte) 3, "sin");
    root.left.left.left = new EquationNode((byte) 2, "root");
    root.left.left.left.left = new EquationNode((byte) 0, 2);
    root.left.left.left.right = new EquationNode((byte) 2, "*");
    root.left.left.left.right.left = new EquationNode((byte) 1, "x");
    root.left.left.left.right.right = new EquationNode((byte) 0, 2);
    return new EquationTree(root);
  }

  public static EquationTree buildTestKreis() { // 0 = y^2 + x^2 - 9
    EquationNode root = new EquationNode((byte) 2, "-");
    root.left = new EquationNode((byte) 2, "+");
    root.right = new EquationNode((byte) 0, 9);
    root.left.left = new EquationNode((byte) 2, "^");
    root.left.right = new EquationNode((byte) 2, "^");
    root.left.left.left = new EquationNode((byte) 1, "y");
    root.left.left.right = new EquationNode((byte) 0, 2);
    root.left.right.left = new EquationNode((byte) 1, "x");
    root.left.right.right = new EquationNode((byte) 0, 2);
    return new EquationTree(root);
  }

  public static EquationTree buildTestParameterFunction() { // 0 = ax^2 - y
    EquationNode root = new EquationNode((byte) 2, "-");
    root.left = new EquationNode((byte) 2, "*");
    root.right = new EquationNode((byte) 1, "y");
    root.left.left = new EquationNode((byte) 1, "a");
    root.left.right = new EquationNode((byte) 2, "^");
    root.left.right.left = new EquationNode((byte) 1, "x");
    root.left.right.right = new EquationNode((byte) 0, 2);
    return new EquationTree(root);
  }

}
