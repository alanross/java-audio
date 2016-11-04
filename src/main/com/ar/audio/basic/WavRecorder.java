package com.ar.audio.basic;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * http://www.jsresources.org/examples/SimpleAudioRecorder.html
 *
 * @author Alan Ross
 * @version 0.1
 */
public class WavRecorder extends Thread
{
	private static WavRecorder _instance;

	private TargetDataLine _line;
	private File _outputFile;
	private AudioFileFormat.Type _targetType;
	private AudioInputStream _audioInputStream;

	public static void startRecording( String fileName )
	{
		if( isRecording() )
		{
			System.out.println( "Already recording. Ignoring." );

			return;
		}

		AudioFormat audioFormat = getFormat();

		DataLine.Info info = new DataLine.Info( TargetDataLine.class, audioFormat );
		TargetDataLine targetDataLine = null;

		try
		{
			targetDataLine = ( TargetDataLine ) AudioSystem.getLine( info );
			targetDataLine.open( audioFormat );
		}
		catch( LineUnavailableException e )
		{
			System.out.println( "unable to get a recording line" );
			e.printStackTrace();
			return;
		}

		_instance = new WavRecorder( targetDataLine, AudioFileFormat.Type.WAVE, fileName );
		_instance.requestStart();
	}

	public static void stopRecording()
	{
		if( !isRecording() )
		{
			System.out.println( "Not recording. Ignoring." );

			return;
		}

		_instance.requestStop();
	}

	public static boolean isRecording()
	{
		return ( _instance != null );
	}

	private static AudioFormat getFormat()
	{
		float sampleRate = 44100;
		int sampleSizeInBits = 16;
		int channels = 2; // mono
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat( sampleRate, sampleSizeInBits, channels, signed, bigEndian );
	}

	public WavRecorder( TargetDataLine line, AudioFileFormat.Type targetType, String fileName )
	{
		_line = line;
		_outputFile = new File( fileName );
		_audioInputStream = new AudioInputStream( line );
		_targetType = targetType;
	}

	protected void requestStart()
	{
		System.out.println( "Start recording." );

		_line.start();

		super.start();
	}

	protected void requestStop()
	{
		System.out.println( "Stop recording." );

		_line.stop();
		_line.close();

		try
		{
			FileInputStream stream = new FileInputStream( _outputFile );
			byte audioData[] = new byte[ ( int ) _outputFile.length() ];
			stream.read( audioData );
		}
		catch( FileNotFoundException e )
		{
			System.out.println( "File not found" + e );
		}
		catch( IOException ioe )
		{
			System.out.println( "Exception while reading the file " + ioe );
		}
	}

	@Override
	public void run()
	{
		try
		{
			AudioSystem.write( _audioInputStream, _targetType, _outputFile );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public String toString()
	{
		return "[WavRecorder]";
	}
}
