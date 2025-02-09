import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class OverlayMenu implements Hideble{
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

    public void show() {
        window.setVisible(true);
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
            window.setPrefWidth(optionDimentions.x);
            window.setPrefHeight(optionDimentions.y * 2);
            options =  new MenuOption[2];
            Image editIcon = new Image("/resources/editButton.png");
            Image deleteIcon = new Image("/resources/trashBin.png");
            options[0] = new MenuOption("Edit",editIcon,this,optionDimentions,0,window);
            options[1] = new MenuOption("Delete",deleteIcon,this,optionDimentions,optionDimentions.y - 3,window);
        }
    }

    public void executeMenuOption(String option) {
        menuHaver.executeMenuOption(option);
        this.window.setVisible(false);
    }

}
