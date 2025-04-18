import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class TopNavBar implements MenuHaver{
    private Pane background;
    private Button[] navButtons;
    private ApplicationController controller;
    private UserManual currentManual;

    private static final double navBarHeight = 37;
    private static final double buttonWidth = 100;
    private static final String[] navBarOptions = new String[] {"file","help"};

    public TopNavBar(Pane root, ApplicationController controller) {
        this.controller = controller;

        background = new Pane();
        background.setPrefSize(root.getWidth(), navBarHeight);
        background.getStyleClass().add("black");
        background.getStyleClass().add("no-border");
        navButtons = new Button[navBarOptions.length];
        for (int i = 0; i < navBarOptions.length; i++) {
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
        for (int i = 0; i < navButtons.length; i++) {
            setupInteractivity(navButtons[i],navBarOptions[i]);
        }
    }

    private void setupInteractivity(Button button, String option) {
        button.setOnMouseEntered(e -> {
            setupOverlayMenu(button,option);
        });
        button.setOnMouseClicked(e -> {
            setupOverlayMenu(button,option);
        });
    }

    private void setupOverlayMenu(Button button, String option) {
        controller.hideRedundantElements();
        OverlayMenu menu = new OverlayMenu(this,option,new TwoDVec<>(button.getLayoutX(),button.getLayoutY() + navBarHeight),controller.root);
        controller.hideOnClick.add(menu);
    }

    @Override
    public void executeMenuOption(String option) {
        if (option.equals("quit")) {
            controller.quit();
        }
        if (option.equals("about")) {
            System.out.println("about stuff");
        }
        if (option.equals("user manual")) {
            if (currentManual == null || currentManual.isClosed) {
                currentManual = new UserManual(controller.getWindow());
            }
        }
    }
}
