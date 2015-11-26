package daris.client.model.task;

import java.util.ArrayList;

import arc.mf.event.SystemEventChannel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class ObservableTaskManager<T extends ObservableTask> {

    private ListProperty<T> _tasksProperty;
    private ObjectProperty<T> _currentTaskProperty;

    ObservableTaskManager() {
        _tasksProperty = new SimpleListProperty<T>(
                FXCollections.observableList(new ArrayList<T>()));
        _currentTaskProperty = new SimpleObjectProperty<T>();
    }

    void addTask(T task) {
        _tasksProperty.add(task);
        launchNextTask();
    }

    void removeTask(T task) {
        _tasksProperty.remove(task);
        task.discard();
    }

    public void addListener(
            ChangeListener<? super ObservableList<T>> listener) {
        _tasksProperty.addListener(listener);
    }

    private void launchNextTask() {
        if (_currentTaskProperty.get() != null) {
            return;
        }
        if (_tasksProperty.size() > 0) {
            T task = _tasksProperty.get(0);
            _currentTaskProperty.set(task);
            task.addStateChangeListener(state -> {
                if (state.finished()) {
                    SystemEventChannel.checkNow();
                    _currentTaskProperty.set(null);
                    _tasksProperty.remove(task);
                    launchNextTask();
                }
            });
            task.submit();
        }
    }
}
