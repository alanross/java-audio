package com.ar.audio.basic;

import com.ar.audio.utils.AudioFileUtils;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Make sure that following jars in classpath:
 * <p/>
 * jl1.0.1.jar
 * jtransforms-2.4.jar
 * mp3spi1.9.5.jar
 * tritonus_mp3-0.3.6.jar
 * tritonus_remaining-0.3.6.jar
 * tritonus_share-0.3.6.jar
 *
 * @author Alan Ross
 * @version 0.1
 */
public final class Mp3Player
{
	public Mp3Player()
	{
	}

	public AudioInputStream createAudioInputStreamHandle( String filename )
	{
		try
		{
			File file = new File( filename );
			FileInputStream fin = new FileInputStream( file );
			byte data[] = new byte[ ( int ) file.length() ];
			fin.read( data );

			AudioInputStream rawInput = AudioSystem.getAudioInputStream( new ByteArrayInputStream( data ) );

			// decode mp3
			AudioFormat baseFormat = rawInput.getFormat();
			AudioFormat decodedFormat = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), // sample rate (same as base format)
					16, // sample size in bits
					baseFormat.getChannels(), // # of Channels
					baseFormat.getChannels() * 2, // Frame Size
					baseFormat.getSampleRate(), // Frame Rate
					false // Big Endian
			);

			return AudioSystem.getAudioInputStream( decodedFormat, rawInput );
		}
		catch( FileNotFoundException e )
		{
			System.out.println( "File not found" + e );
		}
		catch( IOException ioe )
		{
			System.out.println( "Exception while reading the file " + ioe );
		}
		catch( UnsupportedAudioFileException uafe )
		{
			System.out.println( "UnsupportedAudioFileException " + uafe );
		}

		return null;
	}

	public void play( String filename )
	{
		try
		{
			File file = new File( filename );
			AudioInputStream in = AudioSystem.getAudioInputStream( file );
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(),
					16,
					baseFormat.getChannels(),
					baseFormat.getChannels() * 2,
					baseFormat.getSampleRate(),
					false );

			AudioInputStream din = AudioSystem.getAudioInputStream( decodedFormat, in );

			AudioFileUtils.rawplay( decodedFormat, din );

			in.close();
		}
		catch( FileNotFoundException fileNotFoundException )
		{
			System.out.println( "File not found" + fileNotFoundException );
		}
		catch( IOException iOException )
		{
			System.out.println( "Exception while reading the file " + iOException );
		}
		catch( UnsupportedAudioFileException unsupportedAudioFileException )
		{
			System.out.println( "UnsupportedAudioFileException " + unsupportedAudioFileException );
		}
		catch( LineUnavailableException lineUnavailableException )
		{
			lineUnavailableException.printStackTrace();
		}
	}


	@Override
	public String toString()
	{
		return "[Mp3Player]";
	}
}