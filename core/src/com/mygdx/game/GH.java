package com.mygdx.game;

/**
 * Created by Bbent_000 on 11/17/2014.
 */
public class GH {
	public static <T> T as(Class<T> t, Object o) {
		return t.isInstance(o) ? t.cast(o) : null;
	}
}
