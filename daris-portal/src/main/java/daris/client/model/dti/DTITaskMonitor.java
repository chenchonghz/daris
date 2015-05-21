package daris.client.model.dti;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.dti.task.DTITask;
import arc.mf.client.dti.task.DTITaskStatusHandler;

import com.google.gwt.user.client.Timer;

public class DTITaskMonitor<T extends DTITask> {

    public static final int DEFAULT_INTERVAL = 1000;

    public static interface DTITaskAddHandler<T> {
        void added(T task);
    }

    public static interface DTITaskRemoveHandler<T> {
        void removed(T task);
    }

    private Map<Long, T> _tasks;
    private Map<Long, List<DTITaskAddHandler<T>>> _addHandlers;
    private Map<Long, List<DTITaskRemoveHandler<T>>> _removeHandlers;
    private Map<Long, List<DTITaskStatusHandler<T>>> _statusHandlers;

    public DTITaskMonitor(Collection<T> tasks) {
        if (tasks != null) {
            for (T task : tasks) {
                addTask(task, false);
                task.monitor(DEFAULT_INTERVAL, true, new DTITaskStatusHandler<T>() {

                    @Override
                    public void status(final Timer t, final T task) {
                        if (_statusHandlers != null) {
                            List<DTITaskStatusHandler<T>> handlers = _statusHandlers.get(task.id());
                            if (handlers != null) {
                                for (DTITaskStatusHandler<T> h : handlers) {
                                    h.status(t, task);
                                }
                            }
                        }
                    }
                });
            }
        }

    }

    public void addTask(T task, boolean fireEvent) {
        if (_tasks == null) {
            _tasks = new HashMap<Long, T>();
        }
        _tasks.put(task.id(), task);
        if (_addHandlers != null) {
            List<DTITaskAddHandler<T>> handlers = _addHandlers.get(task.id());
            if (handlers != null) {
                for (DTITaskAddHandler<T> h : handlers) {
                    h.added(task);
                }
            }
        }
    }

    public void removeTask(T task, boolean fireEvent) {
        if (_tasks == null) {
            return;
        }
        _tasks.remove(task.id());
        if (_removeHandlers != null) {
            List<DTITaskRemoveHandler<T>> handlers = _removeHandlers.get(task.id());
            if (handlers != null) {
                for (DTITaskRemoveHandler<T> h : handlers) {
                    h.removed(task);
                }
            }
        }
    }

    public void addTaskAddHandler(T task, DTITaskAddHandler<T> ah) {
        if (ah == null) {
            return;
        }
        if (_addHandlers == null) {
            _addHandlers = new HashMap<Long, List<DTITaskAddHandler<T>>>();
        }
        List<DTITaskAddHandler<T>> handlers = _addHandlers.get(task.id());
        if (handlers == null) {
            handlers = new ArrayList<DTITaskAddHandler<T>>();
        }
        handlers.add(ah);
        _addHandlers.put(task.id(), handlers);
    }
    
    
    public void addTaskRemoveHandler(T task, DTITaskRemoveHandler<T> ah) {
        if (ah == null) {
            return;
        }
        if (_removeHandlers == null) {
            _removeHandlers = new HashMap<Long, List<DTITaskRemoveHandler<T>>>();
        }
        List<DTITaskRemoveHandler<T>> handlers = _removeHandlers.get(task.id());
        if (handlers == null) {
            handlers = new ArrayList<DTITaskRemoveHandler<T>>();
        }
        handlers.add(ah);
        _removeHandlers.put(task.id(), handlers);
    }
    
    
    public void addTaskStatusHandler(T task, DTITaskStatusHandler<T> ah) {
        if (ah == null) {
            return;
        }
        if (_statusHandlers == null) {
            _statusHandlers = new HashMap<Long, List<DTITaskStatusHandler<T>>>();
        }
        List<DTITaskStatusHandler<T>> handlers = _statusHandlers.get(task.id());
        if (handlers == null) {
            handlers = new ArrayList<DTITaskStatusHandler<T>>();
        }
        handlers.add(ah);
        _statusHandlers.put(task.id(), handlers);
    }


}
