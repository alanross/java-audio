package com.ar.audio.recognizer;

import com.ar.audio.recognizer.analyzer.AudioAnalyser;
import com.ar.audio.recognizer.analyzer.AudioAnalyserResult;
import com.ar.audio.recognizer.utils.AudioFileReader;
import com.ar.audio.recognizer.utils.AudioMicRecoder;
import com.ar.audio.recognizer.utils.IAudioMicRecorderObserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alan Ross
 * @version 0.1
 */
public class AudioMatcher implements IAudioMicRecorderObserver
{
	private AudioFileReader _fileReader;
	private AudioMicRecoder _micRecoder;
	private AudioAnalyser _analyser;

	private Map<Integer, String> _songNames = new HashMap<Integer, String>();

	private IAudioMatcherObserver _observer;

	public AudioMatcher( IAudioMatcherObserver observer )
	{
		_fileReader = new AudioFileReader();
		_micRecoder = new AudioMicRecoder( this );
		_analyser = new AudioAnalyser();

		_observer = observer;
	}

	private void writeDataPointsToFile( int songId, String data )
	{
		try
		{
			FileWriter fw = new FileWriter( "song" + songId + ".txt" );
			BufferedWriter bw = new BufferedWriter( fw );
			bw.write( data );
			bw.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	private void processRecursive( String directoryName )
	{
		File directory = new File( directoryName );

		File[] files = directory.listFiles();

		for( File file : files )
		{
			if( file.isFile() && file.getName().contains( ".mp3" ) )
			{
				processFile( file.getAbsolutePath() );
			}
			else if( file.isDirectory() )
			{
				processRecursive( file.getAbsolutePath() );
			}
		}
	}

	private void processFile( String filePath )
	{
		byte[] audioData = _fileReader.getAudioData( filePath );

		String songName = new File( filePath ).getName();

		if( audioData.length == 0 )
		{
			_observer.onInfo( "No audio. Skipping: " + songName );

			return;
		}
		else
		{
			_observer.onInfo( "Indexing song: " + songName );

			AudioAnalyserResult result = _analyser.addSong( audioData );

			writeDataPointsToFile( result.songId, result.data );

			//JFrame spectrumView = new SpectrumView( results, CHUNK_SIZE, highScores, recordPoints );
			//spectrumView.setVisible( true );

			_songNames.put( result.songId, songName );
		}
	}

	@Override
	public void onRecordedAudioDataAvailable( byte[] audioData )
	{
		if( audioData.length == 0 )
		{
			_observer.onMatchingFailure( "No audio recorded." );
			return;
		}

		int songId = _analyser.matchSong( audioData );

		if( songId > 0 )
		{
			_observer.onMatchingSuccess( _songNames.get( songId ) );
		}
		else
		{
			_observer.onMatchingFailure( "No matchSong found." );
		}
	}

	public void indexDirectory( final String directoryName )
	{
		Thread t = new Thread( new Runnable()
		{
			public void run()
			{
				processRecursive( directoryName );
			}
		} );

		t.start();

	}

	public void startFindMatch()
	{
		_micRecoder.start();
	}

	public void stopFindMatch()
	{
		_micRecoder.stop();
	}

	public boolean isListening()
	{
		return _micRecoder.isRecording();
	}

	@Override
	public String toString()
	{
		return "[AudioMatcher]";
	}
}
