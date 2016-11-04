package com.ar.audio.recognizer.utils;

import com.ar.audio.utils.AudioFileUtils;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Alan Ross
 * @version 0.1
 */
public final class AudioMicRecoder
{
	private final IAudioMicRecorderObserver _observer;

	private boolean _recording = false;

	public AudioMicRecoder( IAudioMicRecorderObserver observer )
	{
		_observer = observer;
	}

	private AudioFormat getFormat()
	{
		float sampleRate = 44100;
		int sampleSizeInBits = 8;
		int channels = 1; // mono
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat( sampleRate, sampleSizeInBits, channels, signed, bigEndian );
	}

	private SourceDataLine getLine( AudioFormat audioFormat ) throws LineUnavailableException
	{
		DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );

		SourceDataLine res = ( SourceDataLine ) AudioSystem.getLine( info );

		res.open( audioFormat );

		return res;
	}

	public void start()
	{
		final AudioFormat format = AudioFileUtils.getFormat(); // Fill AudioFormat with the wanted settings
		final DataLine.Info info = new DataLine.Info( TargetDataLine.class, format );
		final TargetDataLine line;
		try
		{
			line = ( TargetDataLine ) AudioSystem.getLine( info );
		}
		catch( LineUnavailableException e )
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return;
		}

		try
		{
			line.open( format );
			line.start();
		}
		catch( LineUnavailableException e )
		{
			e.printStackTrace();
		}

		Thread listeningThread = new Thread( new Runnable()
		{
			public void run()
			{
				_recording = true;

				try
				{
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					byte[] buffer = new byte[ 1024 ];

					while( _recording )
					{
						int numBytesRead = line.read( buffer, 0, 1024 );

						if( numBytesRead > 0 )
						{
							out.write( buffer, 0, numBytesRead );
						}
					}

					_observer.onRecordedAudioDataAvailable( out.toByteArray() );

					out.close();
					line.close();
				}
				catch( IOException e )
				{
					System.err.println( "I/O problems: " + e );
					_recording = false;
				}
			}
		} );

		listeningThread.start();
	}

	public void stop()
	{
		_recording = false;
	}

	public boolean isRecording()
	{
		return _recording;
	}

	@Override
	public String toString()
	{
		return "[AudioMicRecoder]";
	}
}