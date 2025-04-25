# graph-dracula

<img width="116" alt="graph_dracula_logo_v11" src="https://github.com/user-attachments/assets/6fdd3f8a-937a-461a-ad6d-46b3d2aaf447" />


Project Trelle Board: <https://trello.com/b/yCEKTda/graph-dracula>

## Building it

**Requirements:**
- java
- javafx SDK 21

You can get the SDK here: <https://gluonhq.com/products/javafx>

### Linux

After extracting the SDK somewhere sensible and cloning this repository you can compile everything with following commands: 

`cd /path/to/repository/JavaClasses`

`javac -classpath $(pwd) --module-path /path/to/SDK/lib --add-modules=javafx.controls,javafx.media,javafx.web,javafx.fxml -deprecation -g -encoding UTF-8 *.java`

And run it with the following:

`java -classpath $(pwd) --module-path /path/to/SDK/lib --add-modules=javafx.controls,javafx.media,javafx.web,javafx.fxml -Dprism.verbose=true Main.java`

This could maybe also work on Windows with some slight modifications, but is not tested.
