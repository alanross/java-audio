package com.ar.audio.recognizer.analyzer;

import com.ar.audio.recognizer.fft.Complex;

/**
 * @author Alan Ross
 * @version 0.1
 */
public final class AudioAnalyserResult
{
	public final int songId;
	public final String data;
	public final Complex[][] results;
	public final int chunkSize;
	public final double[][] highScores;
	public final double[][] recordPoints;

	public AudioAnalyserResult( int songId, String data, Complex[][] results, int chunkSize, double[][] highScores, double[][] recordPoints )
	{
		this.songId = songId;
		this.data = data;
		this.results = results;
		this.chunkSize = chunkSize;
		this.highScores = highScores;
		this.recordPoints = recordPoints;
	}

	@Override
	public String toString()
	{
		return "[AudioAnalyserResult]";
	}
}