public class ConditionNode {
    enum Type {
        COMPARE,
        BOOLOPERATION,
        EQUATIONNODE
    }
    public Type type;
    private String value; //possible values: <;>;<=;>=;==;!=;&;or;!&;!or;!
    public EquationNode equationNode;
    public ConditionNode left;
    public ConditionNode right;
    public ConditionNode aboveElement;

    public ConditionNode(Type type, String value) {
        if (type != Type.EQUATIONNODE) {
            this.type = type;
            this.value = value;
            return;
        }
        System.out.println("Invalid type!");
    }
    public ConditionNode(EquationNode value) {
        this.type = Type.EQUATIONNODE;
        this.equationNode = value;
    }

    public boolean calculateBoolean(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
        if (type.equals(Type.BOOLOPERATION)) {
            return calculateBoolMaths(realCoord,customVars,existingFunctions);
        }
        if (type.equals(Type.COMPARE)) {
            return calculateInRange(realCoord,customVars,existingFunctions);
        }
        System.out.println("Error: Can't calculate boolean of Equationode!");
        doError(realCoord);
        return false;
    }

    public boolean calculateInRange(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
        if (type.equals(Type.COMPARE) && left != null && right != null && left.type.equals(Type.EQUATIONNODE) && right.type.equals(Type.EQUATIONNODE)) {
            double leftValue = left.calculateValue(realCoord,customVars,existingFunctions);
            double rightValue = right.calculateValue(realCoord,customVars,existingFunctions);
            //System.out.println("Left: " + leftValue);
            //System.out.println("Right: " + rightValue);
            switch (value) {
                case "<":
                    return leftValue < rightValue;
                case ">":
                    return leftValue > rightValue;
                case "<=":
                    return leftValue <= rightValue;
                case ">=":
                    return leftValue >= rightValue;
                case "==":
                    return leftValue == rightValue;
                case "!=":
                    return leftValue != rightValue;
            }
        }
        doError(realCoord);
        System.out.println("Error: Invalid compare ConditionNode!");
        return false;
    }

    public boolean calculateBoolMaths(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions) {
        if (type.equals(Type.BOOLOPERATION) && left != null && right != null && !left.type.equals(Type.EQUATIONNODE) && !right.type.equals(Type.EQUATIONNODE)) {
            boolean leftValue = left.calculateBoolean(realCoord,customVars,existingFunctions);
            boolean rightValue = right.calculateBoolean(realCoord,customVars,existingFunctions);
            switch (value) {
                case "&":
                    return leftValue && rightValue;
                case "or":
                    return leftValue || rightValue;
                case "!&":
                    return !(leftValue && rightValue);
                case "!or":
                    return !(leftValue || rightValue);
            }
        }
        doError(realCoord);
        System.out.println("Error: Invalid boolean ConditionNode!");
        return false;
    }

    public double calculateValue(TwoDVec<Double> realCoord, Variable[] customVars, EquationTree[] existingFunctions){
        if (type == Type.EQUATIONNODE) {
            return equationNode.calculate(realCoord,customVars,existingFunctions);
        }
        doError(realCoord);
        System.out.println("Error: Invalid EquationNode ConditionNode!");
        return 0.0;
    }

    public void recursivePrint(String helper) {
        if (type.equals(Type.EQUATIONNODE)) {
            System.out.println(helper + " " + type);
        }
        else {
            System.out.println(helper + " " + value);
        }
        if (left != null) {
            this.left.recursivePrint(helper + "l");
        }
        if (right != null) {
            this.right.recursivePrint(helper + "r");
        }
    }

    private void doError(TwoDVec<Double> realCord) {
        realCord.setPos(-1.0,-1.0);
    }
}

