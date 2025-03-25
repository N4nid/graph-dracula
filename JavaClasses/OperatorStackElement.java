public class OperatorStackElement {
  public EquationNode elem = null;
  public OperatorStackElement next = null;

  public OperatorStackElement(EquationNode elem) {
    this.elem = elem;
  }

  public OperatorStackElement() {
  }

  public void printStack() {
    System.out.println(elem.value + " | " + elem.bracketDepth + " | " + elem.opLevel);
    if (next != null) {
      next.printStack();
    }
  }
}
