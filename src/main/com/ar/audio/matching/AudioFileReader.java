package com.ar.audio.matching;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

/**
 * http://www.jsresources.org/examples/AudioDataBuffer.html To enable mp3
 * reading: 1 add tritonus_mp3-0.3.6.jar & tritonus_share-0.3.6.jar from
 * http://www.tritonus.org/plugins.html 2 add jl1.0.1.jar from
 * http://www.javazoom.net/javalayer/javalayer.html 2 add mp3spi1.9.5.jar from
 * http://www.javazoom.net/mp3spi/mp3spi.html More to this subject of java sound
 * and mp3 http://www.eclipsezone.com/eclipse/forums/t18465.html
 * 
 * Stereo to mono:
 * http://www.tm.tfh-wildau.de/~omayer/Mustererkennung/SpeechRecognitionFramework/edu/cmu/sphinx/frontend/util/Microphone.java
 * http://stackoverflow.com/questions/3125934/lineunavailableexception-for-playing-mp3-with-java
 * 
 * @author Alan Ross
 * @version 0.1
 */
public class AudioFileReader
{
	private static final boolean DEBUG = false;

	// The size of the temporary read buffer, in frames.
	private static final int BUFFER_LENGTH = 1024;

	public static void read( IAudioProviderObserver observer, String filename) throws IOException, UnsupportedAudioFileException
	{
		File file = new File(filename);

		System.out.println( file.getAbsolutePath() );
		FileInputStream fin = new FileInputStream(file);
		byte data[] = new byte[(int)file.length()];
		fin.read(data);
		AudioInputStream rawInput = null;
		AudioInputStream audioInputStream = null; 
		ByteArrayOutputStream audioData = null;

		rawInput = AudioSystem.getAudioInputStream( new ByteArrayInputStream(data) );

		// decode mp3
		AudioFormat baseFormat = rawInput.getFormat();
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(), // sample rate (same as base format)
				16, // sample size in bits (thx to Javazoom)
				baseFormat.getChannels(), // # of Channels
				baseFormat.getChannels() * 2, // Frame Size
				baseFormat.getSampleRate(), // Frame Rate
				false // Big Endian
		);

		audioInputStream = AudioSystem.getAudioInputStream( audioFormat, rawInput );
		
		audioData = new ByteArrayOutputStream();
		int bufferSize = BUFFER_LENGTH * audioFormat.getFrameSize();
		byte[] buffer = new byte[bufferSize];
		
		System.out.println("Base AudioFormat: " + baseFormat.toString());
		System.out.println("AudioFormat: " + audioFormat.toString());
		System.out.println("BufferSize: " + bufferSize);

		while (true)
		{
			if (DEBUG) System.out.println("trying to read (bytes): " + buffer.length);
			int bytesRead = audioInputStream.read(buffer);
			if (DEBUG) System.out.println("read (bytes): " + bytesRead);
			if (bytesRead == -1)
				break;

			audioData.write( buffer, 0, bytesRead );
		}

		System.out.println( "Completed reading " + filename );
		
		if( audioFormat.getChannels() > 1 )
			observer.onNewAudioReceived( convertStereoToMono( audioData.toByteArray(), 2 ) );
		else
			observer.onNewAudioReceived( audioData.toByteArray() );
	}

	/**
	 * Converts stereo com.adjazent.audio.matching.audio to mono.
	 * 
	 * @param samples the com.adjazent.audio.matching.audio samples, each double in the array is one sample
	 * @param channels the number of channels in the stereo com.adjazent.audio.matching.audio
	 */
	private static byte[] convertStereoToMono(byte[] samples, int channels)
	{
		System.out.println( "Convert Stereo to Mono. Channels: " + channels );
		assert (samples.length % channels == 0);

		byte[] finalSamples = new byte[samples.length / channels];

		for (int i = 0, j = 0; i < samples.length; j++)
		{
			byte sum = samples[i++];
			for (int c = 1; c < channels; c++)
			{
				sum += samples[i++];
			}
			finalSamples[j] = (byte)(sum / channels);
		}
		
		System.out.println( "Completed Stereo to Mono conversion");

		return finalSamples;
	}


	private static AudioFormat getFormat()
	{
	    float sampleRate = 44100;
	    int sampleSizeInBits = 8;
	    int channels = 1; //mono
	    boolean signed = true;
	    boolean bigEndian = true;
	    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
}
