import javafx.scene.paint.Color;

public class EquationTree{

    public EquationNode root;
    public Color graphColor = Color.BLACK;
    public boolean isFunction = false;

    public EquationTree(byte rootState, String rootValue) {
        this.root = new EquationNode(rootState,rootValue);
    }

    public EquationTree(byte rootState, double rootValue) {
        this.root = new EquationNode(rootState,rootValue);
    }

    public EquationTree(EquationNode root) {
        this.root = root;
    }

    public EquationTree() {}

    public double calculate(double x, double y, Variable[] parameters) {
        return root.calculate(x,y,parameters);
    }
}
