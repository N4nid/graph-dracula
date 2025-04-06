import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CondtitionParser {
    private static List<String> comparisonOperators = Arrays.asList("<",">","<=",">=","=","!=");
    private static List<String> booleanOperators = Arrays.asList("&","or","!&","!or");
    private static CondtionNode root = null;
    private static CondtionNode workOnNode = root;
    public static CondtionTree parseConditon(String conditionString, ApplicationController controller) {
        if (conditionString.contains("y")) {
            System.out.println("Error: Condition can't deppend on y-Value!");
            return null;
        }
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
                CondtionNode comparisonOperator = new CondtionNode(CondtionNode.Type.COMPARE,currentValue);
                addNode(comparisonOperator);
                currentValue = "";
            } else if ((potBooleanOperators.size() == 1 || (potBooleanOperators.size() > 1 && !canCheckBooleanOperatorFurther)) && potBooleanOperators.get(0).equals(currentValue)) {
                if (!currentEquation.isEmpty()) {
                    //System.out.println("Adding value node " + currentEquation);
                    addEquation(currentEquation, controller);
                    currentEquation = "";
                }
                CondtionNode booleanOperator = new CondtionNode(CondtionNode.Type.BOOLOPERATION, currentValue);
                addNode(booleanOperator);
                currentValue = "";
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

        return new CondtionTree(root);
    }

    private static EquationNode parseEquation(String equationString, ApplicationController controller) {
        EquationTree currentEquation = EquationParser.parseString(equationString,controller);
        if (currentEquation != null && currentEquation.root != null) {
            if (currentEquation.root.state == 2 && ((String)currentEquation.root.value).equals("-") && currentEquation.root.right != null && ((String)currentEquation.root.right.value).equals("y")) {
                currentEquation.root.right = new EquationNode((byte)0,0);
            }
            return currentEquation.root;
        }
        System.out.println("Invalid equation for condition!");
        return null;
    }
    private static ArrayList<String> getPotentialOperators(List<String> searchList, String searchString) {
        ArrayList<String> retList = new ArrayList<String>();
        for (int i = 0; i < searchList.size(); i++) {
            if (searchList.get(i).length() >= searchString.length() && searchList.get(i).startsWith(searchString)) {
                retList.add(searchList.get(i));
            }
        }
        return retList;
    }

    private static void addEquation(String currentEquation, ApplicationController controller) {
        EquationNode equation = parseEquation(currentEquation,controller);
        CondtionNode euqationConditionNode = new CondtionNode(equation);
        if (equation != null) {
            addNode(euqationConditionNode);
        }
    }
    private static void addNode(CondtionNode node) {
        /*if (root != null) {
            root.recursivePrint("Conditon Tree: ");
        }*/
        if (root == null) {
            root = node;
            workOnNode = root;
            return;
        }
        if (workOnNode.type.equals(CondtionNode.Type.COMPARE)) {
            if (node.type.equals(CondtionNode.Type.EQUATIONNODE)) {
                if (!addBelow(node, workOnNode)) {
                    System.out.println("Error: cannot compare 3 values!");
                }
                return;
            }
            if (node.type.equals(CondtionNode.Type.BOOLOPERATION)) {
                addAbove(node, root);
                return;
            }
            System.out.println("Error: Invalid compare!");
        }
        if (workOnNode.type.equals(CondtionNode.Type.EQUATIONNODE)) {
            addAbove(node, workOnNode);
            return;
        }
        if (workOnNode.type.equals(CondtionNode.Type.BOOLOPERATION)) {
            if (node.type.equals(CondtionNode.Type.COMPARE)) {
                if (workOnNode.left.type.equals(CondtionNode.Type.EQUATIONNODE)) {
                    node.left = workOnNode.left;
                    workOnNode.left = null;
                }
                if (workOnNode.right.type.equals(CondtionNode.Type.EQUATIONNODE)) {
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
            if (node.type.equals(CondtionNode.Type.BOOLOPERATION)) {
                addAbove(node, workOnNode);
                return;
            }
            if (node.type.equals(CondtionNode.Type.EQUATIONNODE)) {
                addBelow(node,workOnNode);
            }
        }
    }

    private static boolean addBelow(CondtionNode node, CondtionNode addNode) {
        node.aboveElement = addNode;
        if (addNode.left == null) {
            addNode.left = node;
            return true;
        }
        if (addNode.right == null) {
            addNode.right = node;
            return true;
        }
        return false;
    }

    private static void addAbove(CondtionNode node , CondtionNode addNode) {
        //System.out.println("Adding above " + addNode.type);
        if (addNode.aboveElement != null) {
            if (!addBelow(node,addNode.aboveElement)) {
                System.out.println("Error: Can't add above this element!");
                return;
            }
            node.aboveElement = addNode.aboveElement;
        }
        else {
            root.aboveElement = node;
            root = node;
        }
        addNode.aboveElement = node;
        node.left = addNode;
        workOnNode = node;
    }

}
