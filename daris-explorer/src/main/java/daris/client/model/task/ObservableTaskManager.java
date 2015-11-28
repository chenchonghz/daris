package daris.client.model.task;

import arc.mf.client.agent.task.Task.State;
import arc.mf.event.SystemEventChannel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public abstract class ObservableTaskManager<T extends ObservableTask> {

    private ListProperty<T> _tasksProperty;
    private ObjectProperty<T> _currentTaskProperty;

    ObservableTaskManager() {
        _tasksProperty = new SimpleListProperty<T>(
                FXCollections.observableArrayList());
        _currentTaskProperty = new SimpleObjectProperty<T>();
    }

    synchronized void addTask(T task) {
        _tasksProperty.add(task);
        launchNextTask();
    }

    synchronized void removeTask(T task) {
        _tasksProperty.remove(task);
    }

    public synchronized ListProperty<T> tasksProperty() {
        return _tasksProperty;
    }

    private synchronized void launchNextTask() {
        if (_currentTaskProperty.get() != null) {
            return;
        }
        if (_tasksProperty.size() > 0) {
            for (T task : _tasksProperty) {
                if (task.state() == State.INITIAL) {
                    _currentTaskProperty.set(task);
                    task.addStateChangeListener(state -> {
                        if (state != State.INITIAL) {
                            task.idProperty().set(task.id());
                        }
                        if (state.finished()) {
                            SystemEventChannel.checkNow();
                            _currentTaskProperty.set(null);
                            // _tasksProperty.remove(task);
                            launchNextTask();
                        }
                    });
                    task.submit();
                    break;
                }
            }
        }
    }
}
