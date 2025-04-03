public class CondtionTree {
    public CondtionNode root;

    public boolean checkCondition(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
        if (root != null && root.type.equals(CondtionNode.Type.COMPARE)) {
            return root.calculateInRange(realCoord,customVars,existingFunctions);
        }
        if (root != null && root.type.equals(CondtionNode.Type.BOOLOPERATION)) {
            return root.calculateBoolMaths(realCoord,customVars,existingFunctions);
        }
        System.out.println();
        return false;
    }
}
