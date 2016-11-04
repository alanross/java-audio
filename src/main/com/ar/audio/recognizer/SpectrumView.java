package com.ar.audio.recognizer;

import com.ar.audio.recognizer.fft.Complex;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alan Ross
 * @version 0.1
 */
public class SpectrumView extends JFrame
{
	Complex[][] results = null;
	int size;
	double highScores[][];
	double recordPoints[][];

	public SpectrumView( Complex[][] results, int size, double highScores[][], double recordPoints[][] )
	{
		this.results = results;
		this.size = size;
		this.highScores = highScores;
		this.recordPoints = recordPoints;
		this.setLayout( new FlowLayout() );
		this.setSize( 400, 800 );
	}

	/**
	 * This is the method where the String is drawn.
	 */
	public void paint( Graphics g )
	{
		Graphics2D g2d = ( Graphics2D ) g;

		int blockSizeX = 2;
		int blockSizeY = 3;
		size = 500;
		for( int i = 0; i < results.length; i++ )
		{
			int freq = 1;
			for( int line = 1; line < size; line++ )
			{
				// To get the magnitude of the sound at a given frequency slice
				// get the abs() from the complex number.
				// In this case I use Math.log to get a more manageable number
				// (used for color)
				double magnitude = Math.log( results[ i ][ freq ].abs() + 1 );

				// The more blue in the color the more intensity for a given
				// frequency point:
				g2d.setColor( new Color( 0, ( int ) magnitude * 10, ( int ) magnitude * 20 ) );

				if( freq < 300 && recordPoints[ i ][ freq ] == 1 )
				{
					g2d.setColor( Color.RED );
				}

				// Fill:
				g2d.fillRect( i * blockSizeX, ( size - line ) * blockSizeY, blockSizeX, blockSizeY );

				// I used a improviced logarithmic scale and normal scale:
				if(/* logModeEnabled */false && ( Math.log10( line ) * Math.log10( line ) ) > 1 )
				{
					freq += ( int ) ( Math.log10( line ) * Math.log10( line ) );
				}
				else
				{
					freq++;
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "[SpectrumView]";
	}
}
