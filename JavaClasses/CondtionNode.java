public class CondtionNode {
    enum Type {
        COMPARE,
        BOOLOPERATION,
        EQUATIONNODE
    }
    private Type type;
    private String value; //possible values: <;>;<=;>=;=;!=;&;or;!&;!or;!
    private EquationNode equationNode;

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

    public boolean calculateInRange(TwoDVec<Double> realCoord) {
        if (type == Type.BOOLOPERATION) {
            switch (value) {
                case "<":
                    return

            }
        }
        return false;
    }

    public double calculateValue(TwoDVec<Double> realCoord){
        if (type == Type.EQUATIONNODE) {
            return equationNode.calculate()
        }
    }
}

