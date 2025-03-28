import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class CustomVarUIList {
    public Pane backgroundPane;
    private Pane contentPane;
    private ScrollPane scrollPane;
    public Pane parentPane;

    private int lastCustomVarsSize = -1;

    private ArrayList<CustomVarUIElement> customVars = new ArrayList<>();
    private ArrayList<Anchor> UIAnchors = new ArrayList<>();

    private static final double customVarElementHeightPadding = 20;
    private static final double customVarElementTopPadding = 10;
    private static final double customVarElementWidthPadding = 10;

    public CustomVarUIList(Pane parentPane) {
        this.parentPane = parentPane;

        backgroundPane = new Pane();
        backgroundPane.setViewOrder(-1);
        backgroundPane.getStyleClass().add("black");
        backgroundPane.getStyleClass().add("border");
        parentPane.getChildren().add(backgroundPane);

        scrollPane = new ScrollPane();
        scrollPane.relocate(3,3);
        scrollPane.getStyleClass().add("black");
        backgroundPane.getChildren().add(scrollPane);

        contentPane = new Pane();
        contentPane.getStyleClass().add("black");
        scrollPane.setContent(contentPane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        UIAnchors.add(new Anchor(backgroundPane, parentPane, new TwoDVec<Double>(0.0, 0.0), "scale", false, true));
        UIAnchors.add(new Anchor(scrollPane, backgroundPane, new TwoDVec<Double>(-6.0, -6.0), "scale"));
        UIAnchors.add(new Anchor(contentPane, scrollPane, new TwoDVec<Double>(0.0, 0.0), "scale"));

        updateListTransform();

    }

    public void addCustomVar(String name) {
        if (!customVarExists(name)) {
            customVars.add(new CustomVarUIElement(name));
            updateListTransform();
        }
    }

    public boolean customVarExists(String name) {
        for (int i = 0; i < customVars.size(); i++) {
            if (customVars.get(i).name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void updateListTransform() {
        backgroundPane.setLayoutY(parentPane.getLayoutY() + parentPane.getPrefHeight() - backgroundPane.getPrefHeight() - 44);
        for (int i = 0; i < customVars.size(); i++) {
            customVars.get(i).currentWidth = (backgroundPane.getPrefWidth() - customVarElementWidthPadding * 2 - 6);
            customVars.get(i).refresheUI();
        }
        if (customVars.size() != lastCustomVarsSize) {
            if (customVars.size() > 0) {
                backgroundPane.setVisible(true);
                backgroundPane.setPrefHeight(customVarElementTopPadding + (customVarElementHeightPadding + CustomVarUIElement.defaultHeight) * customVars.size());
            } else {
                backgroundPane.setPrefHeight(0);
                backgroundPane.setVisible(false);
            }
            contentPane.getChildren().clear();

            for (int i = 0; i < customVars.size(); i++) {
                Pane currentCustomVarElement = customVars.get(i).background;
                currentCustomVarElement.relocate(customVarElementWidthPadding, customVarElementTopPadding + (customVarElementHeightPadding + CustomVarUIElement.defaultHeight) * i);
                contentPane.getChildren().add(currentCustomVarElement);
            }
            lastCustomVarsSize = customVars.size();
        }
        Anchor.applyAnchors(UIAnchors);
    }
}
