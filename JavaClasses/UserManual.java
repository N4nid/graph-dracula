import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.net.URL;
import java.util.Set;

public class UserManual {
    private static final double defaultWindowWidth = 1200;
    private static final double defaultWindowHeight = 800;

    private Stage stage;
    private Pane root;

    public UserManual(Window parent) {
        root = new Pane();
        root.setPrefWidth(defaultWindowWidth);
        root.setPrefHeight(defaultWindowHeight);
        String css = this.getClass().getResource("/resources/application.css").toExternalForm();
        root.getStylesheets().add(css);
        root.getStyleClass().add("black");

        stage = new Stage(StageStyle.DECORATED);
        stage.setScene(new Scene(root,defaultWindowWidth,defaultWindowHeight));
        stage.initOwner(parent);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();


        WebView webView = new WebView();

        webView.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() { //This code hides the scroll bar
            @Override public void onChanged(Change<? extends Node> change) {
                Set<Node> deadSeaScrolls = webView.lookupAll(".scroll-bar");
                for (Node scroll : deadSeaScrolls) {
                    scroll.setVisible(false);
                }
            }
        });
        root.getChildren().add(webView);
        webView.setPrefWidth(root.getWidth());
        webView.setPrefHeight(root.getHeight());
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            webView.setPrefWidth(root.getWidth());
        });
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            webView.setPrefHeight(root.getHeight());
        });
        URL indexURL = this.getClass().getResource("/resources/UserManual/index.html");
        webView.getEngine().load(indexURL.toString());
    }
}
