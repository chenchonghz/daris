package daris.client.model.service;

import arc.mf.model.service.BackgroundService;
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

    public void addService(BackgroundService bs) {
        _servicesProperty.get().add(new BackgroundServiceProperties(bs));
    }

    public void addService(BackgroundServiceProperties bsp) {
        _servicesProperty.get().add(bsp);
    }

    public void removeService(long id) {
        int size = _servicesProperty.get().size();
        int idx = -1;
        for (int i = 0; i < size; i++) {
            BackgroundServiceProperties bsp = _servicesProperty.get(i);
            if (bsp.id() == id) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            _servicesProperty.remove(idx);
        }
    }

    public void removeService(BackgroundService bs) {
        removeService(bs.id());
    }

    public void removeService(BackgroundServiceProperties bsp) {
        removeService(bsp.id());
    }
}
