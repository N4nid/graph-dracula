import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class OverlayMenu implements Hideble, MenuHaver{
    private MenuOption[] options;
    public Pane window;
    private MenuHaver menuHaver;
    private TwoDVec<Double> optionDimentions;

    @Override
    public boolean hide() {
        if (!window.hoverProperty().getValue()) {
            window.setVisible(false);
            return true;
        }
        return false;
    }

    public OverlayMenu(MenuHaver menuHaver, String menuPreset, TwoDVec<Double> position, Pane root) {
        this.menuHaver = menuHaver;
        window = new Pane();
        window.setLayoutX(position.x);
        window.setLayoutY(position.y);
        setMenuPreset(menuPreset);
        if (window.getLayoutX() + window.getPrefWidth() > root.getPrefWidth()) {
            window.setLayoutX(window.getLayoutX() - window.getPrefWidth());
        }
        window.getStyleClass().add("black");
        root.getChildren().add(window);
    }

    private void setMenuPreset(String menuPreset) {
        if (menuPreset.equals("equationElement")) {
            this.optionDimentions = new TwoDVec<Double>(150.0,40.0);
            options =  new MenuOption[2];
            Image editIcon = new Image("/resources/editButton.png");
            Image deleteIcon = new Image("/resources/trashBin.png");
            options[0] = new MenuOption("edit",editIcon,this,optionDimentions,new TwoDVec<Double>(0.0,0.0),window);
            options[1] = new MenuOption("delete",deleteIcon,this,optionDimentions,new TwoDVec<Double>(0.0,optionDimentions.y - 3),window);
        }
        if (menuPreset.equals("graphView")) {
            this.optionDimentions = new TwoDVec<Double>(180.0,35.0);
            options =  new MenuOption[2];
            Image recenterIcon = new Image("/resources/recenter.png");
            Image resetZoomIcon = new Image("/resources/resetZoomIcon.png");
            options[0] = new MenuOption("recenter",recenterIcon,18,22,this,optionDimentions,new TwoDVec<Double>(0.0,0.0),window);
            options[1] = new MenuOption("reset zoom",resetZoomIcon,18,22,this,optionDimentions,new TwoDVec<Double>(0.0,optionDimentions.y -3),window);
        }
        window.setPrefWidth(optionDimentions.x);
        window.setPrefHeight(optionDimentions.y * options.length);
    }

    public void executeMenuOption(String option) {
        menuHaver.executeMenuOption(option);
        this.window.setVisible(false);
    }

}
