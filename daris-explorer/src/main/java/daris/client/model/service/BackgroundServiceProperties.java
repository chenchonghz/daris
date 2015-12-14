package daris.client.model.service;

import java.util.Date;

import arc.mf.client.RemoteTask;
import arc.mf.model.service.BackgroundService;
import arc.mf.model.service.BackgroundServiceMonitor;
import arc.mf.model.service.BackgroundServiceMonitorHandler;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BackgroundServiceProperties
        implements BackgroundServiceMonitorHandler {

    private BackgroundServiceMonitor _bsm;
    private ObjectProperty<BackgroundService> _backgroundServiceProperty;
    private LongProperty _idProperty;
    private ObjectProperty<RemoteTask.State> _stateProperty;
    private StringProperty _nameProperty;
    private StringProperty _descriptionProperty;
    private StringProperty _currentActivityProperty;
    private StringProperty _errorProperty;
    private LongProperty _operationsTotalProperty;
    private LongProperty _operationsCompletedProperty;
    private ObjectProperty<Date> _startTimeProperty;
    private ObjectProperty<Date> _endTimeProperty;
    private ObjectProperty<Double> _execTimeProperty;
    private ObjectProperty<Double> _progressProperty;
    private StringProperty _progressMessageProperty;

    public BackgroundServiceProperties(long id) {
        _backgroundServiceProperty = new SimpleObjectProperty<BackgroundService>();
        _idProperty = new SimpleLongProperty();
        _idProperty.set(id);
        _stateProperty = new SimpleObjectProperty<RemoteTask.State>();
        _nameProperty = new SimpleStringProperty();
        _descriptionProperty = new SimpleStringProperty();
        _currentActivityProperty = new SimpleStringProperty();
        _errorProperty = new SimpleStringProperty();
        _operationsTotalProperty = new SimpleLongProperty();
        _operationsTotalProperty.set(0);
        _operationsCompletedProperty = new SimpleLongProperty();
        _operationsCompletedProperty.set(0);
        _startTimeProperty = new SimpleObjectProperty<Date>();
        _endTimeProperty = new SimpleObjectProperty<Date>();
        _execTimeProperty = new SimpleObjectProperty<Double>();
        _execTimeProperty.set(0.0);
        _progressProperty = new SimpleObjectProperty<Double>();
        _progressProperty.set(0.0);
        _progressMessageProperty = new SimpleStringProperty();
        _bsm = new BackgroundServiceMonitor(id, this);
        startMonitor();
    }

    public void startMonitor() {
        _bsm.execute(1000);
    }

    public void stopMonitor() {
        _bsm.cancel();
    }

    public ObjectProperty<BackgroundService> backgroundServiceProperty() {
        return _backgroundServiceProperty;
    }

    public LongProperty idProperty() {
        return _idProperty;
    }

    public ObjectProperty<RemoteTask.State> stateProperty() {
        return _stateProperty;
    }

    public StringProperty nameProperty() {
        return _nameProperty;
    }

    public StringProperty descriptionProperty() {
        return _descriptionProperty;
    }

    public StringProperty currentActivityProperty() {
        return _currentActivityProperty;
    }

    public StringProperty errorProperty() {
        return _errorProperty;
    }

    public LongProperty operationsTotalProperty() {
        return _operationsTotalProperty;
    }

    public LongProperty operationsCompletedProperty() {
        return _operationsCompletedProperty;
    }

    public ObjectProperty<Date> startTimeProperty() {
        return _startTimeProperty;
    }

    public ObjectProperty<Date> endTimeProperty() {
        return _endTimeProperty;
    }

    public ObjectProperty<Double> executionTimeProperty() {
        return _execTimeProperty;
    }

    public ObjectProperty<Double> progressProperty() {
        return _progressProperty;
    }

    public StringProperty progressMessageProperty() {
        return _progressMessageProperty;
    }

    @Override
    public void checked(BackgroundService bs) {
        _backgroundServiceProperty.set(bs);
        _stateProperty.set(bs.state());
        if (_nameProperty.get() == null) {
            _nameProperty.set(bs.name());
        }
        if (_descriptionProperty.get() == null) {
            _descriptionProperty.set(bs.description());
        }
        _currentActivityProperty.set(bs.currentActivity());
        _errorProperty.set(bs.error());
        _operationsTotalProperty.set(bs.totalOperations());
        _operationsCompletedProperty.set(bs.subOperationsCompleted());
        if (bs.totalOperations() > 0) {
            _progressProperty.set((double) bs.subOperationsCompleted()
                    / (double) bs.totalOperations());
            _progressMessageProperty.set(String.format("%d/%d",
                    bs.subOperationsCompleted(), bs.totalOperations()));
        }
        _startTimeProperty.set(bs.startTime());
        _endTimeProperty.set(bs.endTime());
        _execTimeProperty.set(bs.executionTime());
        _progressProperty.set((double) bs.subOperationsCompleted()
                / (double) bs.totalOperations());
    }

}
