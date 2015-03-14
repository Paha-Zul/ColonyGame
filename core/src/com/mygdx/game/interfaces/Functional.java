package com.mygdx.game.interfaces;

import com.mygdx.game.entity.Entity;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/17/2014.
 */
public class Functional {
	public interface Perform<T> {
		void perform(T t);
	}

    public interface Criteria<T>{
        boolean criteria(T t);
    }

	public interface Callback{
		void callback();
	}

	public interface GetEnt{
		Entity getEnt(ArrayList<Entity> entList);
	}

    public interface PerformAndGet<V, T>{
        V performAndGet(T t);
    }

}
