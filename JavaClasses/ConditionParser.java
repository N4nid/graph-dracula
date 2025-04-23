import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConditionParser {
    private static List<String> comparisonOperators = Arrays.asList("<",">","<=",">=","==","!=");
    private static List<String> booleanOperators = Arrays.asList("&","or","!&","!or");
    private ConditionNode root = null;
    private ConditionNode workOnNode = root;


    public ConditionTree parseCondition(String conditionString, ApplicationController controller) {
        String currentValue = "";
        String currentEquation = "";
        for (int i = 0; i < conditionString.length(); i++) {
            currentValue += conditionString.charAt(i);
            ArrayList<String> potComparisonOperators = getPotentialOperators(comparisonOperators,currentValue);
            ArrayList<String> potBooleanOperators = getPotentialOperators(booleanOperators,currentValue);
            boolean canCheckComparrissonFurther = (i < conditionString.length() - 1 && getPotentialOperators(comparisonOperators,currentValue + conditionString.charAt(i+1)).size() > 0);
            boolean canCheckBooleanOperatorFurther = (i < conditionString.length() - 1 && getPotentialOperators(booleanOperators,currentValue + conditionString.charAt(i+1)).size() > 0);
            if ((potComparisonOperators.size() == 1 || (potComparisonOperators.size() > 1 && !canCheckComparrissonFurther)) && potComparisonOperators.get(0).equals(currentValue)) {
                if (!currentEquation.isEmpty()) {
                    //System.out.println("Adding value node " + currentEquation);
                    addEquation(currentEquation, controller);
                    currentEquation = "";
                }
                ConditionNode comparisonOperator = new ConditionNode(ConditionNode.Type.COMPARE,currentValue);
                addNode(comparisonOperator);
                currentValue = "";
            } else if ((potBooleanOperators.size() == 1 || (potBooleanOperators.size() > 1 && !canCheckBooleanOperatorFurther)) && potBooleanOperators.get(0).equals(currentValue)) {
                if (!currentEquation.isEmpty()) {
                    //System.out.println("Adding value node " + currentEquation);
                    addEquation(currentEquation, controller);
                    currentEquation = "";
                }
                ConditionNode booleanOperator = new ConditionNode(ConditionNode.Type.BOOLOPERATION, currentValue);
                addNode(booleanOperator);
                currentValue = "";

                if (i < conditionString.length() - 2 && conditionString.charAt(i+1) == '(') {
                    String bracketContent = getBracketContent(conditionString,i+1);
                    if (bracketContent.isEmpty()) {
                        System.out.println("Error: Invalid Bracket content!");
                        return null;
                    }
                    ConditionParser bracketParser = new ConditionParser();
                    ConditionNode bracketNode = bracketParser.parseCondition(bracketContent,controller).root;
                    if (bracketNode == null) {
                        System.out.println("Error: Invalid Bracket content!");
                        return null;
                    }
                    //bracketNode.recursivePrint("BracketNode: ");
                    addBelow(bracketNode,workOnNode);
                    i+=bracketContent.length()+2;
                }
            } else if (potComparisonOperators.size() == 0 && potBooleanOperators.size() == 0) {
                currentEquation += currentValue;
                currentValue = "";
            }
        }
        if (!currentEquation.isEmpty()) {
            //System.out.println("Adding value node " + currentEquation);
            addEquation(currentEquation,controller);
        }

        TwoDVec<Double> testCoord = new TwoDVec<>(0.0,0.0);
        root.calculateBoolean(testCoord,controller.customVarList.getAllCustomVars(),controller.getAllFunctions());
        if(testCoord.x == -1.0) {
            return null;
        }

        return new ConditionTree(root);
    }

    private EquationNode parseEquation(String equationString, ApplicationController controller) {
        EquationParser.parametricParsing = true;
        EquationTree currentEquation = EquationParser.parseEquation(equationString,controller);
        EquationParser.parametricParsing= false;
        if (currentEquation != null && currentEquation.root != null) {
            return currentEquation.root;
        }
        System.out.println("Invalid equation for condition!");
        return null;
    }
    private ArrayList<String> getPotentialOperators(List<String> searchList, String searchString) {
        ArrayList<String> retList = new ArrayList<String>();
        for (int i = 0; i < searchList.size(); i++) {
            if (searchList.get(i).length() >= searchString.length() && searchList.get(i).startsWith(searchString)) {
                retList.add(searchList.get(i));
            }
        }
        return retList;
    }

    private void addEquation(String currentEquation, ApplicationController controller) {
        EquationNode equation = parseEquation(currentEquation,controller);
        ConditionNode euqationConditionNode = new ConditionNode(equation);
        if (equation != null) {
            addNode(euqationConditionNode);
        }
    }
    private void addNode(ConditionNode node) {
        /*if (root != null) {
            root.recursivePrint("Conditon Tree: ");
        }*/
        if (root == null) {
            root = node;
            workOnNode = root;
            return;
        }
        if (workOnNode.type.equals(ConditionNode.Type.COMPARE)) {
            if (node.type.equals(ConditionNode.Type.EQUATIONNODE)) {
                if (!addBelow(node, workOnNode)) {
                    System.out.println("Error: cannot compare 3 values!");
                }
                return;
            }
            if (node.type.equals(ConditionNode.Type.BOOLOPERATION)) {
                addAbove(node, root);
                return;
            }
            System.out.println("Error: Invalid compare!");
        }
        if (workOnNode.type.equals(ConditionNode.Type.EQUATIONNODE)) {
            addAbove(node, workOnNode);
            return;
        }
        if (workOnNode.type.equals(ConditionNode.Type.BOOLOPERATION)) {
            if (node.type.equals(ConditionNode.Type.COMPARE)) {
                if (workOnNode.left.type.equals(ConditionNode.Type.EQUATIONNODE)) {
                    node.left = workOnNode.left;
                    workOnNode.left = null;
                }
                if (workOnNode.right.type.equals(ConditionNode.Type.EQUATIONNODE)) {
                    node.left = workOnNode.right;
                    workOnNode.right = null;
                }

                if (!addBelow(node, workOnNode)) {
                    System.out.println("Error: cannot do a boolean operation with 3 values!");
                    return;
                }
                workOnNode = node;
                return;
            }
            if (node.type.equals(ConditionNode.Type.BOOLOPERATION)) {
                addAbove(node, workOnNode);
                return;
            }
            if (node.type.equals(ConditionNode.Type.EQUATIONNODE)) {
                addBelow(node,workOnNode);
            }
        }
    }

    private String getBracketContent(String entireString, int BracketPos) {
        int BracketDepth = 1;
        String BracketString = "";
        BracketPos+=1;
        while (BracketPos < entireString.length() && BracketDepth > 0) {
            if (entireString.charAt(BracketPos) == '(') {
                BracketDepth++;
            }
            if (entireString.charAt(BracketPos) == ')') {
                BracketDepth--;
            }
            if (BracketDepth > 0) {
                BracketString+= entireString.charAt(BracketPos);
            }
            BracketPos++;
        }
        if (BracketDepth > 0) {
            System.out.println("Error: Unclosed Bracket!");
            return "";
        }
        return BracketString;
    }

    private boolean addBelow(ConditionNode node, ConditionNode addToNode) {
        node.aboveElement = addToNode;
        if (addToNode.left == null) {
            addToNode.left = node;
            return true;
        }
        if (addToNode.right == null) {
            addToNode.right = node;
            return true;
        }
        return false;
    }

    private void addAbove(ConditionNode node , ConditionNode addToNode) {
        //System.out.println("Adding above " + addNode.type);
        if (addToNode.aboveElement != null) {
            //System.out.println("Adding on top of " + addToNode.type);
            boolean aboveElementHasChildren = false;
            if (addToNode.aboveElement.left != null && addToNode.aboveElement.left.equals(addToNode)) {
                aboveElementHasChildren = true;
                if (!addBelow(addToNode,node)) {
                    System.out.println("Error: Can't add above this element!");
                    return;
                }
                addToNode.aboveElement.left = node;
            }
            if (addToNode.aboveElement.right != null && addToNode.aboveElement.left.equals(addToNode)) {
                aboveElementHasChildren = true;
                if (!addBelow(addToNode,node)) {
                    System.out.println("Error: Can't add above this element!");
                    return;
                }
                addToNode.aboveElement.right = node;
            }
            if (!aboveElementHasChildren) {
                System.out.println("Error: Can't add above this element!");
                return;
            }
            node.aboveElement = addToNode.aboveElement;
        }
        else {
            //System.out.println("Adding on top of root " + root.type);
            if (!addBelow(root,node)) {
                System.out.println("Error: Can't add above this element!");
                return;
            }
            root = node;
        }
        addToNode.aboveElement = node;
        workOnNode = node;
    }

}
