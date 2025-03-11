import javafx.scene.layout.Pane;

public class ExpandMenu {
    public Pane background;
    private double xMargin = 50;
    private double height = 200;
    public ExpandMenu(Pane root) {
        background = new Pane();
        background.getStyleClass().add("black");
        background.getStyleClass().add("border");
        background.relocate(xMargin,0);
        background.resize(0,height);
        root.getChildren().add(background);

    }
}
