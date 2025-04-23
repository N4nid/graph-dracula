public class OperatorStack { // see documetation for details
  public OperatorStackElement top = null;
  public boolean debug = true;

  public OperatorStackElement getLast(int depth, int lvl) {
    cleanUp(depth); // to remove the operators that are in a deeper bracket depth
                    // fe. 2+(2*x^2)-1 the "-" should only "search" for operators on his bracket depth

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

    // find first operator with lvl 0 or first operator with lvl 1 if no operator with lvl 0 is found
    // and only on the same bracketDepth
    // (lvl 0: +- lvl 1: */)

    OperatorStackElement looker = top;
    OperatorStackElement potentialOption = top;
    boolean hasFoundPotentialOption = false;
    while (looker != null) { // only lvl 0 and 1 get here
      if (looker.elem.bracketDepth == depth) {
        int lookerLvl = looker.elem.opLevel;
        if (lookerLvl == 0) {
          if (debug) {
            System.out.println("FOUND last: " + looker.elem.value);
          }
          return looker;
        } else if (lookerLvl == 1 && !hasFoundPotentialOption) { // only set potentialOption once
          if (debug) {
            System.out.println("found potentialOption: " + looker.elem.value);
          }
          hasFoundPotentialOption = true;
          potentialOption = looker;
        }
      }else{ // not in same bracketDepth anymore
        break;
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
    return potentialOption;
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
      newTop.next = top;
      top = newTop;
    } else {
      top = newTop;
    }
  }

  private void cleanUp(int depth) {
    // to remove the operators that are in a deeper bracket depth
    // fe. 2+(2*x^2)-1 the "-" should only "search" for operators on his bracket depth

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
      top = top.next;
    }
  }

}
