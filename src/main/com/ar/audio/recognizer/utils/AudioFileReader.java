package com.ar.audio.recognizer.utils;

import com.ar.audio.utils.AudioFileUtils;
import org.tritonus.sampled.convert.PCM2PCMConversionProvider;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Alan Ross
 * @version 0.1
 */
public final class AudioFileReader
{
	public AudioFileReader()
	{
	}

	public byte[] getAudioData( String filePath )
	{
		byte[] result = new byte[0];

		File file = new File( filePath );

		AudioInputStream origInputStream = null;

		try
		{
			origInputStream = AudioSystem.getAudioInputStream( file );
		}
		catch( UnsupportedAudioFileException e )
		{
			e.printStackTrace();
			return result;
		}
		catch( IOException e )
		{
			e.printStackTrace();
			return result;
		}

		AudioFormat origFormat = origInputStream.getFormat();

		AudioFormat decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				origFormat.getSampleRate(),
				16,
				origFormat.getChannels(),
				origFormat.getChannels() * 2,
				origFormat.getSampleRate(),
				false
		);

		AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream( decodedFormat, origInputStream );

		PCM2PCMConversionProvider conversionProvider = new PCM2PCMConversionProvider();

		if( !conversionProvider.isConversionSupported( AudioFileUtils.getFormat(), decodedFormat ) )
		{
			System.out.println( "Conversion is not supported" );
			return result;
		}

		AudioInputStream inputStream = conversionProvider.getAudioInputStream( AudioFileUtils.getFormat(), decodedInputStream );

		try
		{
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			byte[] buffer = new byte[ 4096 ];

			int numBytesRead;

			while( ( numBytesRead = inputStream.read( buffer, 0, buffer.length ) ) != -1 )
			{
				outputStream.write( buffer, 0, numBytesRead );
			}

			inputStream.close();
			outputStream.close();

			result = outputStream.toByteArray();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public String toString()
	{
		return "[AudioFileReader]";
	}
}