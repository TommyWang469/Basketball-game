/**
 * Module declaration for the Hoops Showdown JavaFX application.
 *
 * <p>The module exports {@code com.example} so JavaDoc and external launch
 * tools can see the application classes. It requires JavaFX controls for the
 * scene graph and buttons, JavaFX media for background music, and JavaFX FXML
 * because the project template includes that dependency even though the current
 * UI is built directly in Java code.</p>
 */
module com.example {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.example to javafx.fxml;
    exports com.example;
}
