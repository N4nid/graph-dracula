import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;


public class ParameterUIElement {
    public String name;
    public double value;

    private Pane background;
    private Label nameDisplay;
    private Slider valueSlider;
    private TextField valueInput;

    public ParameterUIElement(String name) {
        this.name = name;
        value = 0;

        background = new Pane();
        nameDisplay = new Label();
        valueSlider = new Slider();
        valueInput = new TextField();
    }
}
