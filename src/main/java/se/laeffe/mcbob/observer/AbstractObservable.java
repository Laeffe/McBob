package se.laeffe.mcbob.observer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AbstractObservable {
	private ConcurrentLinkedQueue<Observer> observers = new ConcurrentLinkedQueue<Observer>();
	public void addObserver(Observer ob) {
		observers.add(ob);
	}
	
	public void removeObserver(Observer ob) {
		observers.remove(ob);
	}
	
	public void notifyObservers() {
		for(Observer o : observers) {
			o.update(this);
		}
	}
}
