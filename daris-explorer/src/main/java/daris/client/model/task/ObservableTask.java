package daris.client.model.task;

import arc.mf.client.agent.task.Task;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;

public abstract class ObservableTask extends Task {

    private SimpleObjectProperty<Task.State> _stateProperty;

    public ObservableTask(Task parent, String type) {
        super(parent, type);
        _stateProperty = new SimpleObjectProperty<Task.State>(this, "state",
                super.state());
    }

    protected void setState(Task.State state) {
        super.setState(state);
        Platform.runLater(() -> {
            _stateProperty.set(state);
        });
    }

    public SimpleObjectProperty<Task.State> stateProperty() {
        return _stateProperty;
    }

}
