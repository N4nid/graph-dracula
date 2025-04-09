import javafx.scene.paint.Color;

public class EquationTree {

  public boolean isVisible = true;

  public EquationNode root;
  public Color graphColor = Color.BLACK;
  public boolean isFunction = false;
  public boolean isParametric = false;
  public String name;
  public EquationNode intervalStart;
  public EquationNode intervalEnd;
  public ConditionTree rangeCondition;
  
  public EquationTree(EquationNode root,String name, boolean isFunction) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
  }

  public EquationTree(EquationNode root, String name, boolean isFunction, TwoDVec<Double> xRange) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
    this.rangeCondition = new ConditionTree(xRange.x, xRange.y);
  }

  public EquationTree(EquationNode root) {
    this.root = root;
  }

  public EquationTree() {
  }

  public double calculate(TwoDVec<Double> coordinates, Variable[] customVariables, EquationTree[] existingFunctions) {
    return root.calculate(coordinates,customVariables,existingFunctions);
  }
  
  public TwoDVec<Double> calculateParametrics(double t, Variable[] customVariables, EquationTree[] existingFunctions) {
    return new TwoDVec<Double>(root.left.calculateParametric(t,customVariables,existingFunctions),root.right.calculateParametric(t,customVariables,existingFunctions));
  }

}
