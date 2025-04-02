public class OperatorStack {
  public OperatorStackElement top = null;

  public OperatorStackElement getLast(int depth, int lvl) {
    cleanUp(depth);
    if (top == null) {
      return null;
    }
    // first op with max 1 level diff and also on the same depth
    // Turns out its better to keep searching for the perfect match (lvl diff = 0)
    // and only take the other option if there is no better match

    OperatorStackElement looker = top;
    int levelDiff = looker.elem.opLevel - lvl;
    if (levelDiff <= 1) {
      System.out.println("last: " + looker.elem.value);
      return looker;
    }
    OperatorStackElement potentialOption = top;
    while (looker != null) {
      // the or is for in brackets
      // System.out.println(" --- " + looker.elem.value + " | " +
      // looker.elem.opLevel);
      if (looker.elem.bracketDepth == depth) {
        // System.out.println(" --- right depth");
        levelDiff = looker.elem.opLevel - lvl;
        if (levelDiff == 0) {
          System.out.println("FOUND last: " + looker.elem.value);
          return looker;
        } else if (levelDiff == 1) {
          System.out.println("found potentialOption: " + looker.elem.value);
          potentialOption = looker;
        }
      } else if (potentialOption.equals(top)) { // this is also for in brackets, might change later FIXME maybe ?
        System.out.println("Not in bracketDepth");
        return top;
      }
      if (looker.next != null) {
        looker = looker.next;
      } else {
        break;
      }
    }

    System.out.println("notfound last: " + potentialOption.elem.value);
    return potentialOption; // XXX MIGHT BLOW UP !?
  }

  public void printStack() {
    if (top != null) {
      System.out.println("------ top");
      top.printStack();
      System.out.println("--- bottom");
    }

  }

  public EquationNode getAbove(int depth) { // above in the tree, below in the stack
    // important for cases like 5*(2^3+8)
    // the + in brackets needs to add above the ^
    // But also "link" back to the *
    OperatorStackElement looker = top;
    while (looker != null) {
      if (looker.elem.bracketDepth < depth) {
        return looker.elem;
      }
      looker = looker.next;
    }

    return null;
  }

  public void add(EquationNode elem, int depth) {
    elem.bracketDepth = depth;
    OperatorStackElement newTop = new OperatorStackElement(elem);
    if (top != null) {
      cleanUp(depth);
      // unsorted variant
      newTop.next = top;
      top = newTop;

    } else {
      top = newTop;
    }
  }

  private void cleanUp(int depth) {
    while (top != null) {
      if (top.elem.bracketDepth > depth) {
        pop();
      } else {
        break;
      }
    }
  }

  public void pop() {
    if (top != null) {
      // System.out.println("-- rm: " + top.elem.value);
      top = top.next;
    }
  }

}
