package com.ar.audio.recognizer;

/**
 * @author Alan Ross
 * @version 0.1
 */
public interface IAudioMatcherObserver
{
	void onInfo( String message );

	void onMatchingSuccess( String result );

	void onMatchingFailure( String error );
}