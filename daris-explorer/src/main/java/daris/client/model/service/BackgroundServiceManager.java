package daris.client.model.service;

import java.util.Iterator;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class BackgroundServiceManager {

    private ListProperty<BackgroundServiceProperties> _servicesProperty;

    private BackgroundServiceManager() {
        _servicesProperty = new SimpleListProperty<BackgroundServiceProperties>(
                FXCollections.observableArrayList());
    }

    public ListProperty<BackgroundServiceProperties> servicesProperty() {
        return _servicesProperty;
    }

    private static BackgroundServiceManager _instance;

    public static BackgroundServiceManager get() {
        if (_instance == null) {
            _instance = new BackgroundServiceManager();
        }
        return _instance;
    }

    public void addService(long id) {
        _servicesProperty.get().add(new BackgroundServiceProperties(id));
    }

    public void removeService(long id) {
        for (Iterator<BackgroundServiceProperties> it = _servicesProperty.get()
                .iterator(); it.hasNext();) {
            BackgroundServiceProperties bs = it.next();
            if (bs.idProperty().get() == id) {
                it.remove();
                // TODO;
            }
        }
    }
}
