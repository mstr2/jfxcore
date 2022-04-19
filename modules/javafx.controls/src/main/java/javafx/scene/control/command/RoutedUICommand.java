package javafx.scene.control.command;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.command.RoutedCommand;

public class RoutedUICommand extends RoutedCommand implements UICommand {

    private StringProperty text;

    public StringProperty textProperty() {
        return text != null ? text : (text = new SimpleStringProperty(this, "text"));
    }

    public String getText() {
        return text != null ? text.get() : null;
    }

    public void setText(String text) {
        if (text != null || this.text != null) {
            textProperty().set(text);
        }
    }

    private ObjectProperty<Node> graphic;

    public ObjectProperty<Node> graphicProperty() {
        return graphic != null ? graphic : (graphic = new SimpleObjectProperty<>(this, "graphic"));
    }

    public Node getGraphic() {
        return graphic != null ? graphic.get() : null;
    }

    public void setGraphic(Node graphic) {
        if (graphic != null || this.graphic != null) {
            graphicProperty().set(graphic);
        }
    }

}
