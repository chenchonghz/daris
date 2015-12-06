package daris.client.model.object;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;

public interface DObjectBuilder {

    ReadOnlyObjectProperty<DObject> parentProperty();

    StringProperty nameProperty();

    StringProperty descriptionProperty();
    
    StringProperty typeProperty();
    
    StringProperty contentTypeProperty();
}
