package com.ar.audio.recognizer.utils;

/**
 * @author Alan Ross
 * @version 0.1
 */
public interface IAudioMicRecorderObserver
{
	void onRecordedAudioDataAvailable( byte[] audioData );
}