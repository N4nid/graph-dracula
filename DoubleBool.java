public class DoubleBool {
    private boolean bool1;
    private boolean bool2;
    private static final boolean[][] boolMap = {{false,false,true,true},{false,true,false,true}};

    public DoubleBool(int value) {
        if (value < 4 && value >= 0) {
           bool1 = boolMap[0][value];
           bool2 = boolMap[1][value];
        }
        else {
            System.out.println("Error: Invalid input value to create a new DoubleBool!");
        }
    }

    public boolean isValue(int value) {
        return  (bool1 == boolMap[0][value] && bool2 == boolMap[1][value]);
    }
}
