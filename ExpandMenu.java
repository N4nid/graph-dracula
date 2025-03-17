import javafx.scene.layout.Pane;

public class ExpandMenu {
    public Pane background;
    private double xMargin = 65;
    private double height = 200;
    public ExpandMenu(Pane root) {
        background = new Pane();
        background.getStyleClass().add("black");
        background.getStyleClass().add("border");
        background.relocate(xMargin,0);
        background.setPrefHeight(height);
        background.setViewOrder(-1);
        root.getChildren().add(background);

    }
}
