public class ConditionTree {
  public ConditionNode root;

  public boolean checkCondition(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
    if (root != null && root.type.equals(ConditionNode.Type.COMPARE)) {
      return root.calculateInRange(realCoord,customVars,existingFunctions);
    }
    if (root != null && root.type.equals(ConditionNode.Type.BOOLOPERATION)) {
      return root.calculateBoolMaths(realCoord,customVars,existingFunctions);
    }
    doError(realCoord);
    System.out.println("Invalid condition tree!");
    return false;
  }
  
  public ConditionTree(ConditionNode root) {
    this.root = root;
  }
  
  public ConditionTree(double min, double max) {
    this.root = new ConditionNode(ConditionNode.Type.BOOLOPERATION,"&");
    this.root.left = new ConditionNode(ConditionNode.Type.COMPARE,"<");
    this.root.left.left = new ConditionNode(new EquationNode((byte) 0,min));
    this.root.left.right = new ConditionNode(new EquationNode((byte)1,"x"));
    this.root.right = new ConditionNode(ConditionNode.Type.COMPARE,"<");
    this.root.right.left = new ConditionNode(new EquationNode((byte)1,"x"));
    this.root.right.right = new ConditionNode(new EquationNode((byte) 0,max));
  }
  private void doError(TwoDVec<Double> realCord) {
    realCord.setPos(-1.0,-1.0);
  }
}
