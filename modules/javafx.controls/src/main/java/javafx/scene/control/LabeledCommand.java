package javafx.scene.control.command;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.command.Command;
import javafx.scene.command.RelayCommand;
import javafx.scene.control.Labeled;
import java.util.function.Consumer;

public class UIRelayCommand extends Command {

    private final StringProperty text = new SimpleStringProperty(this, "text");

    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public final Node getGraphic() {
        return graphic.get();
    }

    public final void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    private final

    @Override
    public ReadOnlyBooleanProperty executableProperty() {
        return null;
    }

    @Override
    public ReadOnlyBooleanProperty executingProperty() {
        return null;
    }

    @Override
    public void execute(Object parameter) {

    }

    @Override
    protected void configure(Node node) {
        if (node instanceof Labeled labeled) {
            labeled.textProperty().bind(text);
            labeled.graphicProperty().bind(graphic);
        }
    }

}
