import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;

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
}
