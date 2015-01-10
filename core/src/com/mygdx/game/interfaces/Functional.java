package com.mygdx.game.interfaces;

/**
 * Created by Bbent_000 on 11/17/2014.
 */
public class Functional {
	public interface Perform<T> {
		void perform(T t);
	}

	public interface Callback{
		void callback();
	}

	public interface Test<T>{
		boolean test(T t);
	}
}
