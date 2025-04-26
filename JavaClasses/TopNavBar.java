import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Pane;

public class TopNavBar implements MenuHaver{
    private Pane background;
    private Button[] navButtons;
    private ApplicationController controller;
    private HTMLWindow currentManual;
    private HTMLWindow currentAboutWindow;

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
        ColorAdjust brighten = new ColorAdjust();
        brighten.setBrightness(0.15);
        button.setOnMouseEntered(e -> {
            button.setEffect(brighten);
            setupOverlayMenu(button,option);
        });
        button.setOnMouseClicked(e -> {
            setupOverlayMenu(button,option);
        });
        button.setOnMouseExited(e -> {
            button.setEffect(null);
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
            if (currentAboutWindow == null || currentAboutWindow.isClosed) {
                currentAboutWindow = new HTMLWindow(controller.getWindow(),"/resources/ExtraHTML/about.html");
            }
        }
        if (option.equals("user manual")) {
            if (currentManual == null || currentManual.isClosed) {
                currentManual = new HTMLWindow(controller.getWindow(),"/resources/ExtraHTML/index.html");
            }
        }
    }
}
