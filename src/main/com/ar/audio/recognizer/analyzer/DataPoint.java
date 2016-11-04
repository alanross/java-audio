package com.ar.audio.recognizer.analyzer;

/**
 * @author Alan Ross
 * @version 0.1
 */
public class DataPoint
{
	public int id;
	public int time;

	public DataPoint( int id, int time )
	{
		this.id = id;
		this.time = time;
	}

	@Override
	public String toString()
	{
		return "[DataPoint id:" + id + "]";
	}
}
