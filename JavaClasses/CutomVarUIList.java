import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class CutomVarUIList {
    private Pane backgroundPane;
    private Pane contentPane;
    private ScrollPane scrollPane;


    ArrayList<CutomVarUIList> customVars;

    private static final double customVarElementHeightPadding = 20;
    private static final double customVarElementTopPadding = 30;
    private static final double customVarElementWidthPadding = 10;

    public CutomVarUIList(Pane parentPane) {
        backgroundPane = new Pane();
        backgroundPane.getStyleClass().add("black");
        backgroundPane.getStyleClass().add("border");
        parentPane.getChildren().add(backgroundPane);

        scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("black");
        //backgroundPane.getChildren().add()

        contentPane = new Pane();
        contentPane.getStyleClass().add("black");
    }
}
