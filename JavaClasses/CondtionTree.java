public class CondtionTree {
    public CondtionNode root;

    public boolean checkCondition(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
        if (root != null && root.type.equals(CondtionNode.Type.COMPARE)) {
            return root.calculateInRange(realCoord,customVars,existingFunctions);
        }
        if (root != null && root.type.equals(CondtionNode.Type.BOOLOPERATION)) {
            return root.calculateBoolMaths(realCoord,customVars,existingFunctions);
        }
        System.out.println("Invalid condition tree!");
        return false;
    }

    public CondtionTree(CondtionNode root) {
        this.root = root;
    }

    public CondtionTree(double min, double max) {
        this.root = new CondtionNode(CondtionNode.Type.BOOLOPERATION,"&");
        this.root.left = new CondtionNode(CondtionNode.Type.COMPARE,"<");
        this.root.left.left = new CondtionNode(new EquationNode((byte) 0,min));
        this.root.left.right = new CondtionNode(new EquationNode((byte)1,"x"));
        this.root.right = new CondtionNode(CondtionNode.Type.COMPARE,"<");
        this.root.right.left = new CondtionNode(new EquationNode((byte)1,"x"));
        this.root.right.right = new CondtionNode(new EquationNode((byte) 0,max));
    }
}
