package javafx.scene.control;

import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Template;
import javafx.scene.TemplateContent;
import javafx.scene.layout.Region;

@DefaultProperty("template")
public class ContentView<T> extends Region {

    private final T bindingContext;

    public ContentView(@NamedArg("bindingContext") T bindingContext) {
        this.bindingContext = bindingContext;
    }

    private final ObjectProperty<Template<T>> template = new SimpleObjectProperty<>(this, "content");

    public ObjectProperty<Template<T>> templateProperty() {
        return template;
    }

    public Template<T> getTemplate() {
        return template.get();
    }

    public void setTemplate(Template<T> template) {
        this.template.set(template);
    }

    private final BooleanProperty collapsed = new BooleanPropertyBase(true) {
        @Override
        public Object getBean() {
            return ContentView.this;
        }

        @Override
        public String getName() {
            return "collapsed";
        }

        @Override
        protected void invalidated() {
            TemplateContent<T> content = get() ? null : getTemplate().getContent();
            if (content != null) {
                getChildren().add(content.newInstance(bindingContext));
            } else {
                getChildren().clear();
            }
        }
    };

    public BooleanProperty collapsedProperty() {
        return collapsed;
    }

    public boolean isCollapsed() {
        return collapsed.get();
    }

    public void setCollapsed(boolean value) {
        this.collapsed.set(value);
    }

}
