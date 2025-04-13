import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class TopNavBar {
    private Pane background;
    private Button[] navButtons;
    private static final int numberButtons = 2;

    private static final double navBarHeight = 37;
    private static final double buttonWidth = 100;
    private static final String[] navBarOptions = new String[] {"file","help"};

    public TopNavBar(Pane root) {
        background = new Pane();
        background.setPrefSize(root.getWidth(), navBarHeight);
        background.getStyleClass().add("black");
        background.getStyleClass().add("solid-bottom-border");
        background.getStyleClass().add("yellow-border");
        navButtons = new Button[numberButtons];
        for (int i = 0; i < numberButtons; i++) {
            navButtons[i] = new Button();
            navButtons[i].getStyleClass().add("black");
            navButtons[i].getStyleClass().add("no-border");
            navButtons[i].getStyleClass().add("nav-button-text");
            navButtons[i].setPrefSize(buttonWidth,0);
            navButtons[i].setLayoutX(i*buttonWidth+3);
            navButtons[i].setLayoutY(3);
            navButtons[i].setText(navBarOptions[i]);
            Effects.addDefaultHoverEffect(navButtons[i]);
            background.getChildren().add(navButtons[i]);
        }
        root.getChildren().add(background);
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            background.setPrefWidth(root.getWidth());
        });
    }
}
