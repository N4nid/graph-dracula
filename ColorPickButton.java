import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class ColorPickButton{
  public Button displayButton;
  private Color colorValue;
  
  public ColorPickButton(int xPos, int yPos, Color color, RoundColorPicker pickerParent, int colorIndex) {
    this.displayButton = new Button();
    this.displayButton.setLayoutX(xPos);
    this.displayButton.setLayoutY(yPos);
    displayButton.getStyleClass().add("color-picker");
    colorValue = color;
    displayButton.setStyle("-fx-background-color: " + toRGBCode(colorValue));
    displayButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        pickerParent.pickColor(colorIndex);
      }
    });
    Effects.addDefaultHoverEffect(this.displayButton);
  }

  private static String toRGBCode( Color color )
  {
    return String.format( "#%02X%02X%02X",
    (int)( color.getRed() * 255 ),
    (int)( color.getGreen() * 255 ),
    (int)( color.getBlue() * 255 ) );
  }

}
