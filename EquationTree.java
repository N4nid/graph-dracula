import javafx.scene.paint.Color;
import java.util.ArrayList;

public class EquationTree{
  
  public EquationNode root;
  public Color graphColor = Color.BLACK;
  public boolean isFunction = false;
  public String name;

  public EquationTree(byte rootState, String rootValue) {
    this.root = new EquationNode(rootState,rootValue);
  }
  
  public EquationTree(EquationNode root,String name, boolean isFunction) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
  }
  
  public EquationTree(byte rootState, double rootValue) {
    this.root = new EquationNode(rootState,rootValue);
  }
  
  public EquationTree(EquationNode root) {
    this.root = root;
  }
  
  public EquationTree() {}
  
  public double calculate(TwoDVec<Double> realCoord, Variable[] parameters) {
    return root.calculate(realCoord,parameters);
  }
  
  public void renderEquation(ArrayList<TwoDVec<TwoDVec<Double>>> linePoints) {
    
  }
}
