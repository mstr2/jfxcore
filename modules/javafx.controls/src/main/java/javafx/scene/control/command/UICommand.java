package javafx.scene.control.command;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.command.Command;

public interface UICommand extends Command {

    StringProperty textProperty();

    default String getText() {
        return textProperty().get();
    }

    default void setText(String text) {
        textProperty().set(text);
    }

    ObjectProperty<Node> graphicProperty();

    default Node getGraphic() {
        return graphicProperty().get();
    }

    default void setGraphic(Node graphic) {
        graphicProperty().set(graphic);
    }

}
