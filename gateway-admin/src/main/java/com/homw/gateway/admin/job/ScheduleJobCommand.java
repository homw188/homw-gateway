package com.homw.gateway.admin.job;

import com.homw.gateway.common.command.Command;

public abstract class ScheduleJobCommand implements Command<Void> {

	private int repeat = 3;

	@Override
	public Void exec() {
		exec(repeat);
		return null;
	}

	@Override
	public Void exec(int repeat) {
		for (int i = 0; i < repeat; i++) {
			try {
				if (run()) {
					success();
					return null;
				}
			} catch (Exception e) {
				if (i == repeat - 1) {
					fail(e);
				}
			}
		}
		return null;
	}

	protected abstract void success();

	protected abstract void fail(Throwable t);

	protected abstract boolean run();
}
