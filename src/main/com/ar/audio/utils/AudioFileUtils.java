package com.ar.audio.utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Alan Ross
 * @version 0.1
 */
public final class AudioFileUtils
{
	public static AudioFormat getFormat()
	{
		float sampleRate = 44100;
		int sampleSizeInBits = 8;
		int channels = 1; // mono
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat( sampleRate, sampleSizeInBits, channels, signed, bigEndian );
	}

	public static SourceDataLine getLine( AudioFormat audioFormat ) throws LineUnavailableException
	{
		DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );

		SourceDataLine res = ( SourceDataLine ) AudioSystem.getLine( info );

		res.open( audioFormat );

		return res;
	}

	public static void rawplay( AudioFormat targetFormat, AudioInputStream din ) throws IOException, LineUnavailableException
	{
		byte[] data = new byte[ 4096 ];

		SourceDataLine line = getLine( targetFormat );

		if( line != null )
		{
			// Start
			line.start();
			int numBytesRead = 0;
			int numBytesWritten = 0;

			while( numBytesRead != -1 )
			{
				numBytesRead = din.read( data, 0, data.length );

				if( numBytesRead != -1 )
				{
					numBytesWritten = line.write( data, 0, numBytesRead );
				}
			}

			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}

	public static void readAudioFileData( final String filePath )
	{
		File fileIn = new File( filePath );

		int totalFramesRead = 0;

		try
		{
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( fileIn );
			int bytesPerFrame = audioInputStream.getFormat().getFrameSize();

			if( bytesPerFrame == AudioSystem.NOT_SPECIFIED )
			{
				// some audio formats may have unspecified frame size
				// in that case we may read any amount of bytes
				bytesPerFrame = 1;
			}

			// Set an arbitrary buffer size of 1024 frames.
			int numBytes = 1024 * bytesPerFrame;
			byte[] audioBytes = new byte[ numBytes ];
			try
			{
				int numBytesRead = 0;
				int numFramesRead = 0;

				// Try to read numBytes bytes from the file.
				while( ( numBytesRead = audioInputStream.read( audioBytes ) ) != -1 )
				{
					// Calculate the number of frames actually read.
					numFramesRead = numBytesRead / bytesPerFrame;
					totalFramesRead += numFramesRead;
					// Here, do something useful with the audio data that's
					// now in the audioBytes array...
				}
			}
			catch( Exception ex )
			{
				// Handle the error...
			}
		}
		catch( Exception e )
		{
			// Handle the error...
		}
	}

	private AudioFileUtils()
	{
	}


	@Override
	public String toString()
	{
		return "[AudioFileUtils]";
	}
}