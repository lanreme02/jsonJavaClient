package com.olanrewaju.barrier;

public class Semaphor {

	boolean lock = true;
	Object condition = new Object();

	public void acquire() {

		while (lock) {
			try {
				synchronized (condition) {
					condition.wait();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		lock = true;

	}

	public void release() {

		synchronized (condition) {
			condition.notify();
		}
		lock = false;

	}
}
