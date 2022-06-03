package javafx.scene.command;

import javafx.beans.property.ReadOnlyStringProperty;

public final class StandardCommandCapabilities {

    private StandardCommandCapabilities() {}

    /**
     * Marker interface for a command that implements the <em>message</em> capability.
     */
    public interface Message {
        /**
         * Gets a property that represents the reported message of the command.
         *
         * @defaultValue null
         */
        ReadOnlyStringProperty messageProperty();

        /**
         * Gets the reported message of the command.
         *
         * @return the message or {@code null}
         */
        String getMessage();
    }

    /**
     * Marker interface for a command that implements the <em>title</em> capability.
     */
    public interface Title {
        /**
         * Gets a property that represents the reported title of the command.
         *
         * @defaultValue null
         */
        ReadOnlyStringProperty titleProperty();

        /**
         * Gets the reported title of the command.
         *
         * @return the title or {@code null}
         */
        String getTitle();
    }

}
