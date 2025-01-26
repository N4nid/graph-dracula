module com.example.graphdraculagui {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.graphdraculagui to javafx.fxml;
    exports com.example.graphdraculagui;
}