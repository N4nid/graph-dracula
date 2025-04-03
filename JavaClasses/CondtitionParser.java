import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CondtitionParser {
    private static List<String> comparisonOperators = Arrays.asList("<",">","<=",">=","=","!=");
    public static CondtionNode root = null;
    public static CondtionTree parseConditon(String conditionString, ApplicationController controller) {
        String currentValue = "";
        String currentEquation = "";
        for (int i = 0; i < conditionString.length(); i++) {
            currentValue += conditionString.charAt(i);
            ArrayList<String> potComparisonOperators = getPotentialOperators(comparisonOperators,currentValue);
            if (potComparisonOperators.size() == 1 && potComparisonOperators.get(0).equals(currentValue)) {
                if (currentEquation != "") {
                    EquationNode equation = parseEquation(currentEquation,controller);
                    CondtionNode euqationConditionNode = new CondtionNode(equation);
                    currentEquation = "";
                    if (equation != null) {
                        addNode(euqationConditionNode);
                    }
                }
                CondtionNode comparisonOperator = new CondtionNode(CondtionNode.Type.COMPARE,currentValue);
                addNode(comparisonOperator);
                currentValue = "";
            } else if (potComparisonOperators.size() == 0) {
                currentEquation += currentValue;
                currentValue = "";
            }
        }
        if (currentEquation != "") {
            EquationNode equation = parseEquation(currentEquation,controller);
            CondtionNode euqationConditionNode = new CondtionNode(equation);
            currentEquation = "";
            if (equation != null) {
                addNode(euqationConditionNode);
            }
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
    private static void addNode(CondtionNode node) {
        if (root == null) {
            root = node;
            return;
        }
        if (root.type.equals(CondtionNode.Type.COMPARE)) {
            if (node.type.equals(CondtionNode.Type.EQUATIONNODE)) {
                if (root.left == null) {
                    root.left = node;
                    return;
                }
                if (root.right == null) {
                    root.right = node;
                    return;
                }
                System.out.println("Error: cannot compare 3 values!");
                return;
            }
            if (node.type.equals(CondtionNode.Type.BOOLOPERATION)) {
                node.left = root;
                root = node;
                return;
            }
            System.out.println("Error: Invalid compare!");
        }
        if (root.type.equals(CondtionNode.Type.EQUATIONNODE)) {
            node.left = root;
            root = node;
            return;
        }
        if (root.type.equals(CondtionNode.Type.BOOLOPERATION)) {
            if (node.type.equals(CondtionNode.Type.COMPARE)) {
                if (root.left == null) {
                    root.left = node;
                    return;
                }
                if (root.right == null) {
                    root.right = node;
                    return;
                }
                System.out.println("Error: cannot do a boolean operation with 3 values!");
                return;
            }
            if (node.type.equals(CondtionNode.Type.BOOLOPERATION)) {
                node.left = root;
                root = node;
                return;
            }
            System.out.println("Error: Cannot do boolean operations with values!");
        }
    }

}
