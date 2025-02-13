import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;

public class Effects {
    public static void addDefaultHoverEffect(Node node) {
        ColorAdjust brighten = new ColorAdjust();
        brighten.setBrightness(0.15);
        node.setOnMouseEntered(e -> {;
            node.setEffect(brighten);
        });
        node.setOnMouseExited(e -> {;
            node.setEffect(null);
        });
    }
    public static Color changeBrightness(Color input, double brightness) {
        double red = input.getRed() * brightness;
        double green = input.getGreen() * brightness;
        double blue = input.getBlue() * brightness;

        //cap values
        red = Math.max(Math.min(1,red),0);
        green = Math.max(Math.min(1,green),0);
        blue = Math.max(Math.min(1,blue),0);

        return new Color(red,green,blue,1);
    }
}
