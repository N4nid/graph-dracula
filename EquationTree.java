import javafx.application.Application;

import javafx.scene.paint.Color;

import java.io.IOException;

public class EquationTree {

  public EquationNode root;
  public Color GraphColor = Color.BLACK;
  public boolean isFunction = false;

  public EquationTree(byte rootState, String rootValue) {
    this.root = new EquationNode(rootState, rootValue);
  }

  public EquationTree(byte rootState, double rootValue) {
    this.root = new EquationNode(rootState, rootValue);
  }

  public EquationTree(EquationNode root) {
    this.root = root;
  }

  public EquationTree() {
  }

  public double calculate(double x, double y, Variable[] parameters) {
    return root.calculate(x, y, parameters);
  }
}
