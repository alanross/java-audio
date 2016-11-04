package com.ar.audio.matching;

/**
 * @author Alan Ross
 * @version 0.1
 */
public interface IAudioProviderObserver
{
	void onNewAudioReceived( byte[] audioData );
}
