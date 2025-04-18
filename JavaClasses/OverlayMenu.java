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
        window.setViewOrder(-4);
        root.getChildren().add(window);
    }

    private void setMenuPreset(String menuPreset) {
        Image deleteIcon = new Image("/resources/trashBin.png");
        if (menuPreset.equals("equationElement") || menuPreset.equals("hiddenEquationElement")) {
            this.optionDimentions = new TwoDVec<Double>(150.0,43.0);
            options =  new MenuOption[3];
            Image editIcon = new Image("/resources/editButton.png");
            Image hideIcon = new Image("/resources/hide.png");
            Image showIcon = new Image("/resources/previewButton.png");
            options[0] = new MenuOption("edit",editIcon,this,optionDimentions,new TwoDVec<Double>(0.0,0.0),window);
            if (menuPreset.equals("equationElement")) {
                options[1] = new MenuOption("hide",hideIcon,this,optionDimentions,new TwoDVec<Double>(0.0,optionDimentions.y - 3),window);
            }
            else {
                options[1] = new MenuOption("show",showIcon,this,optionDimentions,new TwoDVec<Double>(0.0,optionDimentions.y - 3),window);
            }
            options[2] = new MenuOption("delete",deleteIcon,this,optionDimentions,new TwoDVec<Double>(0.0,optionDimentions.y*2 - 3*2),window);
        }
        if (menuPreset.equals("customVarElement")) {
            this.optionDimentions = new TwoDVec<Double>(150.0,43.0);
            options = new MenuOption[1];
            options[0] = new MenuOption("delete",deleteIcon,this,optionDimentions,new TwoDVec<Double>(0.0,0.0),window);
        }
        if (menuPreset.equals("graphView")) {
            this.optionDimentions = new TwoDVec<Double>(180.0,35.0);
            options =  new MenuOption[2];
            Image recenterIcon = new Image("/resources/recenter.png");
            Image resetZoomIcon = new Image("/resources/resetZoomIcon.png");
            options[0] = new MenuOption("recenter",recenterIcon,18,22,this,optionDimentions,new TwoDVec<Double>(0.0,0.0),window);
            options[1] = new MenuOption("reset zoom",resetZoomIcon,18,22,this,optionDimentions,new TwoDVec<Double>(0.0,optionDimentions.y -3),window);
        }
        if (menuPreset.equals("file")) {
            this.optionDimentions = new TwoDVec<Double>(150.0,43.0);
            options = new MenuOption[1];
            Image closeXIncon = new Image("/resources/closeX.png");
            options[0] = new MenuOption("quit",closeXIncon,this,optionDimentions,new TwoDVec<Double>(0.0,0.0),window);
        }
        if (menuPreset.equals("help")) {
            this.optionDimentions = new TwoDVec<Double>(230.0,43.0);
            options = new MenuOption[2];
            Image userManualIcon = new Image("/resources/userManual.png");
            Image aboutIcon = new Image("/resources/about.png");
            options[0] = new MenuOption("user manual",userManualIcon,this,optionDimentions,new TwoDVec<Double>(0.0,0.0),window);
            options[1] = new MenuOption("about",aboutIcon,this,optionDimentions,new TwoDVec<Double>(0.0,optionDimentions.y -3),window);
        }
        window.setPrefWidth(optionDimentions.x);
        window.setPrefHeight(optionDimentions.y * options.length);
    }

    public void executeMenuOption(String option) {
        menuHaver.executeMenuOption(option);
        this.window.setVisible(false);
    }

}
