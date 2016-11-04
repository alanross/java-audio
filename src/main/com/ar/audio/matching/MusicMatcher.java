package com.ar.audio.matching;

import com.ar.audio.recognizer.fft.Complex;
import com.ar.audio.recognizer.fft.FFT;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.FileWriter;
import java.io.IOException;

/**
 * VM settings:
 * -Xms1024m -Xmx2048m -XX:+UseParallelGC -XX:PermSize=1024M -XX:MaxPermSize=2048M
 *
 * @author Alan Ross
 * @version 0.1
 */

public class MusicMatcher extends JPanel implements Scrollable, IAudioProviderObserver
{
	private static final long serialVersionUID = 1L;
	private static final Runtime runtime = Runtime.getRuntime();

	private static final int AMOUNT_OF_POINTS = 4;
	// Using a little bit of error-correction, damping
	private static final int FUZ_FACTOR = 2;


	private double preMult = 20; // 0.0, 7000.0
	private double brightness = 0; // -300, 300
	private double contrast = 50; // 0.0, 10000.0
	private boolean useRed = true;

	private BufferedImage img;

	private int[] imgPixels;


	private static final int CHUNK_SIZE = 1024;//4096;


	Complex[][] _results;

	public MusicMatcher( String filename )
	{
		try
		{
			AudioFileReader.read( this, filename );
			//WavRecorder.record(this);
		}
		catch( Exception e )
		{
			System.out.println( "There was an exception reading the audiofile " + e );
			System.exit( ERROR );
		}
	}

	public void onNewAudioReceived( byte[] audioData )
	{
		byte[] audio = audioData;
		System.out.println( "onRecordCompleted " + audioData.length );

		performFFTAnalysis( audio );

		int w = _results.length;
		int h = _results[ 0 ].length;

		setPreferredSize( new Dimension( w, h ) );
		img = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
		imgPixels = ( ( DataBufferInt ) img.getRaster().getDataBuffer() ).getData();
		updateImage();
		setBackground( Color.BLACK );
		repaint();
	}

	private void performFFTAnalysis( byte[] audio )
	{
		final int totalSize = audio.length;
		int amountPossible = totalSize / CHUNK_SIZE;

		System.out.println( "Started FFT " + totalSize );

		// For frequency domain we need complex numbers
		_results = new Complex[ amountPossible ][];

		// For all the chunks:
		for( int times = 0; times < amountPossible; times++ )
		{
			Complex[] complex = new Complex[ CHUNK_SIZE ];

			for( int i = 0; i < CHUNK_SIZE; i++ )
			{
				// Put the time domain data into a complex number with imaginary
				// part as 0:
				complex[ i ] = new Complex( audio[ ( times * CHUNK_SIZE ) + i ], 0 );
			}

			// Perform FFT analysis on the chunk:
			_results[ times ] = FFT.fft( complex );

			//memoryInfo();
		}

		System.out.println( "Completed FFT" );
	}


	protected void updateImage()
	{
		try
		{
			FileWriter fw = new FileWriter( "points.txt" );

			final int endCol = _results.length;
			final int endRow = _results[ 0 ].length;

			final int[] range = new int[]{ 40, 80, 120, 180, endRow };

			for( int col = 0; col < endCol; col++ )
			{
				int[] highscores = new int[ AMOUNT_OF_POINTS + 1 ];
				int[] recordPoints = new int[ AMOUNT_OF_POINTS + 1 ];

				for( int row = 0; row < endRow; row++ )
				{
					double val = _results[ col ][ row ].abs();
					imgPixels[ col + row * img.getWidth() ] = colorFor( val );

					int magnitude = ( int ) ( contrast * Math.log1p( Math.abs( preMult * val ) ) );

					int index = 0;

					while( range[ index ] < row )
					{
						index++;
					}

					// Save the highest magnitude and corresponding frequency:
					if( highscores[ index ] < magnitude )
					{
						highscores[ index ] = magnitude;
						recordPoints[ index ] = row; //freq
					}

				}

				//Write the points to a file:
				for( int a = 0; a < AMOUNT_OF_POINTS; a++ )
				{
					//System.out.print(recordPoints[a] + "\t");
					imgPixels[ col + recordPoints[ a ] * img.getWidth() ] = 0xff0000;
					fw.append( recordPoints[ a ] + "\t" );
				}
				//System.out.println("");
				fw.append( "\n" );
			}

			fw.close();
		}
		catch( IOException e )
		{
			System.out.println( "Could now write file " + e );
		}
	}

	@Override
	protected void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		Graphics2D g2 = ( Graphics2D ) g;

		// Flip upside down while rendering the spectrogram
		g2.translate( 0, img.getHeight() );
		g2.scale( 1.0, -1.0 );

		Rectangle clipBounds = g2.getClipBounds();
		if( clipBounds.x + clipBounds.width > img.getWidth() )
		{
			clipBounds.width = img.getWidth() - clipBounds.x;
		}
		if( clipBounds != null )
		{
			g2.drawImage( img, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y
					+ clipBounds.height, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y
					+ clipBounds.height, Color.BLACK, null );
		}
	}

	private int colorFor( double val )
	{
		int greyVal = ( int ) ( brightness + ( contrast * Math.log1p( Math.abs( preMult * val ) ) ) );

		if( useRed )
		{
			if( greyVal < 0 )
			{
				return 0;
			}
			else if( greyVal <= 255 )
			{
				return ( greyVal << 16 ) | ( greyVal << 8 ) | ( greyVal );
			}
			else if( greyVal <= 512 )
			{
				greyVal -= 256;
				greyVal = 256 - greyVal;
				return 0xff0000 | ( greyVal << 8 ) | ( greyVal );
			}
			else
			{
				return 0xff0000;
			}
		}
		else
		{
			greyVal = Math.min( 255, Math.max( 0, greyVal ) );
			return ( greyVal << 16 ) | ( greyVal << 8 ) | ( greyVal );
		}
	}

	private long hash( String line )
	{
		String[] p = line.split( "\t" );
		long p1 = Long.parseLong( p[ 0 ] );
		long p2 = Long.parseLong( p[ 1 ] );
		long p3 = Long.parseLong( p[ 2 ] );
		long p4 = Long.parseLong( p[ 3 ] );
		return ( p4 - ( p4 % FUZ_FACTOR ) ) * 100000000 +
				( p3 - ( p3 % FUZ_FACTOR ) ) * 100000 +
				( p2 - ( p2 % FUZ_FACTOR ) ) * 100 +
				( p1 - ( p1 % FUZ_FACTOR ) );
	}


	// --------------------- Scrollable interface ------------------------

	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction )
	{
		return ( int ) ( visibleRect.width * 0.9 );
	}

	public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction )
	{
		return 50;
	}

	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	private static void memoryInfo()
	{
		int mb = 1024 * 1024;
		long tm = runtime.totalMemory() / mb;
		long fm = runtime.freeMemory() / mb;
		long um = tm - fm;

		System.out.println( "Used:" + um + "mb / Total:" + tm + "mb" );
	}
}
