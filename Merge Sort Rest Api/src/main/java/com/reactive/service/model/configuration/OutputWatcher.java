package com.reactive.service.model.configuration;

// The purpose of this class is to watch the definition of task output
public class OutputWatcher {

	private long start;
	private long end;
	private boolean ended;
	private long duration;
	private Data data;
	
	public OutputWatcher() {
		start = System.currentTimeMillis();
	}
	
	public void setExecutionToEnd() {
		end=System.currentTimeMillis();
		setEnded(true);
		setDuration(end-start);
	}
	public void setExecutionToEndWithData(Data d) {
		end=System.currentTimeMillis();
		setEnded(true);
		setDuration(end-start);
		//setData(d);
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Object getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	
	
	
	
}
