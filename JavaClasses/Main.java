import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.util.Scanner;

import java.io.IOException;

public class Main extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/GraphDraculaUI.fxml"));
    Parent root = loader.load();
    // Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
    ApplicationController controller = loader.getController();
    Scene scene = new Scene(root);
    String css = this.getClass().getResource("/resources/application.css").toExternalForm();
    scene.getStylesheets().add(css);

    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        controller.hideRedundantElements();
      }
    });

    stage.setTitle("Graph Dracula");
    stage.setScene(scene);
    stage.show();
    controller.setup();
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

  public static void quickDebug() {
    String test = "1+root(2,x)";
    //String test = "sin(ln(root(3,x)))";
    //String test = "root(root(2,4),64)";
    // String test = "1+3*3^2";
    // String test = "cos(sin(1-1)*2.4)";
    // String test = "4*4^5+cos(4^2)-1/2*sin(4)+1/4*(4+1)";
    // String test = "sin(((4^(3))/32)-2*cos(16/31+108/31-4))+42";
    // String test = "(1-1)+2*(3^(2-1))";
    // String test = "1+2*(1+3*3+1)";
    // String test = "cos(sin(1/3)+1)";
    // String test = "3^(cos(sin(1/3)+1))*(1/2)";
    // String test = "3^(3+3)*2";
    // String test = "2+3^(23-1/2)";
    // String test = "x^3+34/6.5-1/(a^(sin(56)))*3";
    EquationParser.parseString(test);
    // EquationParser.getBetweenBrackets(new StringBuffer("(root(2,4),64)+1"));
  }

  public static void interactiveDemo() {
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
          EquationParser.parseString(inp);
        } catch (Exception e) {
          System.out.println("whopsies: " + e);
        }
      } else {
        break;
      }
    }
  }

  public static void testParser() { // todo
    // brackets
    String test[] = { "3*2^2+1", "1+2*3^2", "2*3^sin(0)+1", "1+sin(0)*2",
        "1+1^3*3+1", "1+2*(3-1)", "(2*2+1)^2", "sin(1-1)+2*(3^(2-1))", "1+2*(1+3*3+1)",
        "3^(sin(2*cos(1/3*3-1)-2)+2)*(1/2)", "cos(sin(1-1)*2)", "sin(2*sin(2-2))", "sin(2*sin(22*0))", "root(2,64)-4",
        "root(2,root(2,64)/2)*2^1", "1+root(2,4)", "sin(2pi)","2sin(pi/2)"
    };
    double results[] = { 13, 19, 3, 1, 5, 5, 25, 6, 23, 4.5, 1, 0, 0, 4, 4,3 ,0,2};
    int passed = 0;
    for (int i = 0; i < test.length; i++) {
      System.out.println("-----------------");
      EquationTree root = EquationParser.parseString(test[i]);
      double res = root.calculate(0, 0, new Variable[1]);
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

  public static void debugStrinparser(String[] args) {
    if (args.length >= 1) {
      System.out.println("--- MODE: " + args[0]);
      switch (args[0]) {
        case "test":
          Main.testParser();
          break;

        case "demo":
          Main.interactiveDemo();
          break;
        case "debug":
          Main.quickDebug();
          break;

        default:
          System.out.println("Use one of the following args: test, demo, debug");
          break;
      }
    }
  }

  public static void main(String[] args) {
    // option to not launch gui
    if (args.length >= 1) {
      debugStrinparser(args);
    } else {
      launch();
    }
  }
}
