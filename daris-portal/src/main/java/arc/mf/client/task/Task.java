package arc.mf.client.task;

import java.util.Date;

import arc.mf.client.xml.XmlElement;

public class Task {
	public enum State {
		PENDING, EXECUTING, COMPLETED, ABORT_PENDING, ABORTED, FAILED, SUSPENDED, STATE_FAILED_WILL_RETRY;

		public static State state(String s) {
			if (s.equalsIgnoreCase("ABORT-PENDING")) {
				return ABORT_PENDING;
			}

			if (s.equalsIgnoreCase("FAILED-WILL-RETRY")) {
				return STATE_FAILED_WILL_RETRY;
			}

			return State.valueOf(s);
		}
	}

	private long _id;
	private State _state;
	private double _waitTime;
	private Date _startTime;
	private Date _endTime;
	private double _execTime;
	private long _completed;
	private long _total;

	protected Task(XmlElement xe) throws Throwable {
		_id = xe.longValue("@id");

		_state = State.state(xe.value("state").toUpperCase());
		_startTime = xe.dateValue("start-time");
		_endTime = xe.dateValue("end-time");
		_execTime = xe.doubleValue("exec-time", 0.0);
		_waitTime = xe.doubleValue("wait-time", 0.0);
		_total = xe.longValue("total", -1);
		_completed = xe.longValue("completed", 0);
	}

	/**
	 * The unique (task) identifier.
	 * 
	 * @return
	 */
	public long id() {
		return _id;
	}

	/**
	 * Returns the current state of the task.
	 * 
	 * @return
	 */
	public State state() {
		return _state;
	}

	/**
	 * Was the task aborted? This is a convenience method that checks the state
	 * to see if ABORTED.
	 * 
	 * @return
	 */
	public boolean aborted() {
		return _state.equals(State.ABORTED);
	}

	/**
	 * Has the task failed?
	 * 
	 * @return
	 */
	public boolean failed() {
		switch (_state) {
		case FAILED:
			return true;
		}

		return false;
	}

	/**
	 * Has the task finished?
	 * 
	 * @return
	 */
	public boolean finished() {
		switch (_state) {
		case COMPLETED:
		case ABORTED:
		case FAILED:
			return true;
		}

		return false;
	}

	/**
	 * Returns the time started, or null.
	 *
	 * @return
	 */
	public Date startTime() {
		return _startTime;
	}

	/**
	 * Returns the time the task ended, if it has ended.
	 * 
	 * @return
	 */
	public Date endTime() {
		return _endTime;
	}

	/**
	 * Returns the total execution time (in seconds).
	 * 
	 * @return
	 */
	public double executionTime() {
		return _execTime;
	}

	public double waitTime() {
		return _waitTime;
	}

	/**
	 * Returns the number of sub-operations completed.
	 * 
	 * @return
	 */
	public long subOperationsCompleted() {
		return _completed;
	}

	public long totalOperations() {
		return _total;
	}

}
