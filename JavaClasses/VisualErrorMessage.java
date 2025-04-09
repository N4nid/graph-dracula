import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class VisualErrorMessage implements Hideble{
  private Label errorLabel;
  private static final double heightPadding = 15;
  private static final double defaultHeight = 40;
  private static final double defaultCharWidth = 12.7;
  
  private Anchor posAnchor;
  
  public VisualErrorMessage(Pane parentPane, Pane onTopPane) {
    errorLabel = new Label();
    
    errorLabel.getStyleClass().add("black");
    errorLabel.getStyleClass().add("thin-border");
    errorLabel.getStyleClass().add("red-border");
    errorLabel.getStyleClass().add("small-text");
    
    errorLabel.setPrefHeight(defaultHeight);
    errorLabel.setPadding(new Insets(0,10,0,10));
    posAnchor = new Anchor(errorLabel,onTopPane,new TwoDVec<Double>(0.0,-heightPadding-defaultHeight),"pos");
    posAnchor.applyAnchor();
    
    onTopPane.localToParentTransformProperty().addListener((obs, oldVal, newVal) -> {
      posAnchor.applyAnchor();
    });
    
    parentPane.getChildren().add(errorLabel);
    errorLabel.setViewOrder(-3);
    errorLabel.setVisible(false);
  }
  
  public void displayError(String error) {
    errorLabel.setVisible(true);
    errorLabel.setText(error);
    errorLabel.setPrefWidth(error.length() * defaultCharWidth);
  }

  public boolean hide() {
    if (!errorLabel.hoverProperty().getValue()) {
      errorLabel.setVisible(false);
      return true;
    }
    return false;
  }
  
  
}
