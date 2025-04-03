public class CondtionNode {
    enum Type {
        COMPARE,
        BOOLOPERATION,
        EQUATIONNODE
    }
    public Type type;
    private String value; //possible values: <;>;<=;>=;=;!=;&;or;!&;!or;!
    private EquationNode equationNode;
    public CondtionNode left;
    public CondtionNode right;

    public CondtionNode(Type type, String value) {
        if (type != Type.EQUATIONNODE) {
            this.type = type;
            this.value = value;
            return;
        }
        System.out.println("Invalid type!");
    }
    public CondtionNode(EquationNode value) {
        this.type = Type.EQUATIONNODE;
        this.equationNode = value;
    }

    public boolean calculateInRange(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
        if (type.equals(Type.COMPARE) && left != null && right != null && left.type.equals(Type.EQUATIONNODE) && right.type.equals(Type.EQUATIONNODE)) {
            double leftValue = left.calculateValue(realCoord,customVars,existingFunctions);
            double rightValue = right.calculateValue(realCoord,customVars,existingFunctions);
            switch (value) {
                case "<":
                    return leftValue < rightValue;
                case ">":
                    return leftValue > rightValue;
                case "<=":
                    return leftValue <= rightValue;
                case ">=":
                    return leftValue >= rightValue;
                case "=":
                    return leftValue == rightValue;
                case "!=":
                    return leftValue != rightValue;
            }
        }
        System.out.println("Error: Invalid compare ConditionNode!");
        return false;
    }

    public boolean calculateBoolMaths(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
        if (type.equals(Type.BOOLOPERATION) && left != null && right != null && left.type.equals(Type.COMPARE) && right.type.equals(Type.COMPARE)) {
            boolean leftValue = left.calculateInRange(realCoord,customVars,existingFunctions);
            boolean rightValue = right.calculateInRange(realCoord,customVars,existingFunctions);
            switch (value) {
                case "&":
                    return leftValue && rightValue;
                case "or":
                    return leftValue || rightValue;
                case "!&":
                    return !(leftValue && rightValue);
                case "!or":
                    return !(leftValue || rightValue);
                case "=":
                    return leftValue == rightValue;
                case "!=":
                    return leftValue != rightValue;
            }
        }
        if (type.equals(Type.BOOLOPERATION) && left != null) {
            if (left.type.equals(Type.COMPARE)) {
                return !left.calculateInRange(realCoord,customVars,existingFunctions);
            }
            if (left.type.equals(Type.BOOLOPERATION)) {
                return !left.calculateBoolMaths(realCoord,customVars,existingFunctions);
            }
        }
        System.out.println("Error: Invalid compare ConditionNode!");
        return false;
    }

    public double calculateValue(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions){
        if (type == Type.EQUATIONNODE) {
            return equationNode.calculate(realCoord,customVars,existingFunctions);
        }
        System.out.println("Error: Invalid EquationNode ConditionNode!");
        return 0.0;
    }

}

