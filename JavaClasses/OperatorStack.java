public class OperatorStack {
  public OperatorStackElement top = null;
  public boolean debug = false;

  public OperatorStackElement getLast(int depth, int lvl) {
    cleanUp(depth);
    if (top == null) {
      return null;
    }

    // if it isnt a lvl 0 or 1 operator -> no search necessary
    if (lvl > 1) {
      if (debug) {
        System.out.println("last: " + top.elem.value);
      }
      return top;
    }

    // first op with max 1 level diff and also on the same depth
    // Turns out its better to keep searching for the perfect match (lvl diff = 0)
    // and only take the other option if there is no better match

    OperatorStackElement looker = top;
    OperatorStackElement potentialOption = top;
    boolean hasFoundPotentialOption = false;
    while (looker != null) { // only lvl 0 and 1 get here
      // the or is for in brackets
      //System.out.println(" --- " + looker.elem.value + " | " +looker.elem.opLevel);
      if (looker.elem.bracketDepth == depth) {
        int lookerLvl = looker.elem.opLevel;
        //System.out.println(" --- right depth");
        if (lookerLvl == 0) {
          if (debug) {
            System.out.println("FOUND last: " + looker.elem.value);
          }
          return looker;
        } else if (lookerLvl == 1 && !hasFoundPotentialOption) { 
          if (debug) {
            System.out.println("found potentialOption: " + looker.elem.value);
          }
          hasFoundPotentialOption = true;
          potentialOption = looker;
        }
      } else if (potentialOption.equals(top)) { // this is also for in brackets, might change later FIXME maybe ?
        if (debug) {
          System.out.println("Not in bracketDepth");
        }
        return top;
      }
      if (looker.next != null) {
        looker = looker.next;
      } else {
        break;
      }
    }

    if (debug) {
      System.out.println("notfound last: " + potentialOption.elem.value);
    }
    return potentialOption; // XXX MIGHT BLOW UP !?
  }

  public void printStack() {
    if (top != null && debug) {
      System.out.println("------ top");
      top.printStack();
      System.out.println("--- bottom");
    }

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
