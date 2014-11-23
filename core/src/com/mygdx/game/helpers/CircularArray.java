package com.mygdx.game.helpers;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/22/2014.
 */
public class CircularArray<T> {

	private ArrayList<T> circArray;
	private int length, start, end;

	public CircularArray(int length){
		circArray = new ArrayList<T>(length);
	}

	public void push(T item){
		//If we are full
		if(this.full()){
			this.start = (this.start+1)%this.length;
			this.end = (this.end+1)%this.length;
			circArray.set(this.end, item);
		}else{
			this.end++;
			circArray.add(this.end, item);
		}
	}

	public T get(int index){
		int ind = (this.start + index)%this.length;
		return circArray.get(ind);
	}

	public boolean full(){
		return (end + 1)%length == start;
	}
}
