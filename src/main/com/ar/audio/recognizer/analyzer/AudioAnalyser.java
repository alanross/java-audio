package com.ar.audio.recognizer.analyzer;

import com.ar.audio.recognizer.fft.Complex;
import com.ar.audio.recognizer.fft.FFT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VM settings:
 * -Xms1024m -Xmx2048m -XX:+UseParallelGC -XX:PermSize=1024M -XX:MaxPermSize=2048M
 *
 * @author Alan Ross
 * @version 0.1
 */
public final class AudioAnalyser
{
	// Using a little bit of error-correction, damping
	private static final int FUZ_FACTOR = 2;

	//audio frame
	private static final int CHUNK_SIZE = 4096;

	// frequency bands
	private static final int UPPER_LIMIT = 300;
	private static final int LOWER_LIMIT = 40;
	private static final int[] RANGE = new int[]{ 40, 80, 120, 180, UPPER_LIMIT + 1 };

	private Map<Long, List<DataPoint>> _hashes = new HashMap<Long, List<DataPoint>>();

	private int _songId = 0;

	public AudioAnalyser()
	{
	}

	private long getHash( long p1, long p2, long p3, long p4 )
	{
		return ( p4 - ( p4 % FUZ_FACTOR ) ) * 100000000 +
				( p3 - ( p3 % FUZ_FACTOR ) ) * 100000 +
				( p2 - ( p2 % FUZ_FACTOR ) ) * 100 +
				( p1 - ( p1 % FUZ_FACTOR ) );
	}

	private Complex[][] performFFT( byte audioData[] )
	{
		final int totalSize = audioData.length;

		int amountPossible = totalSize / CHUNK_SIZE;

		// When turning into frequency domain we'll need complex numbers:
		Complex[][] results = new Complex[ amountPossible ][];

		// For all the chunks:
		for( int times = 0; times < amountPossible; times++ )
		{
			Complex[] complex = new Complex[ CHUNK_SIZE ];

			for( int i = 0; i < CHUNK_SIZE; i++ )
			{
				// Put the time domain data into a complex number with imaginary part as 0:
				complex[ i ] = new Complex( audioData[ ( times * CHUNK_SIZE ) + i ], 0 );
			}

			// Perform FFT analysis on the chunk:
			results[ times ] = FFT.fft( complex );
		}

		return results;
	}

	// Find out in which range
	private int getFrequencyRange( int freq )
	{
		int i = 0;

		while( RANGE[ i ] < freq )
		{
			i++;
		}

		return i;
	}

	private int getBestMatch( Map<Integer, Map<Integer, Integer>> candidates )
	{
		int bestCount = 0;
		int bestMatch = -1;

		for( Map.Entry<Integer, Map<Integer, Integer>> candidate : candidates.entrySet() )
		{
			Integer songId = candidate.getKey();
			Map<Integer, Integer> candidateData = candidate.getValue();

			int bestCountForSong = 0;

			for( Map.Entry<Integer, Integer> data : candidateData.entrySet() )
			{
				if( data.getValue() > bestCountForSong )
				{
					bestCountForSong = data.getValue();
				}
			}

			if( bestCountForSong > bestCount )
			{
				bestCount = bestCountForSong;
				bestMatch = songId;
			}
		}

		return bestMatch;
	}

	public AudioAnalyserResult addSong( byte audio[] )
	{
		_songId++;

		Complex[][] results = performFFT( audio );

		StringBuilder stringBuilder = new StringBuilder();

		double recordPoints[][] = new double[ results.length ][ UPPER_LIMIT ];
		double highScores[][] = new double[ results.length ][ 5 ];
		long points[][] = new long[ results.length ][ 5 ];

		for( int i = 0; i < results.length; i++ )
		{
			for( int j = 0; j < 5; j++ )
			{
				highScores[ i ][ j ] = 0;
				points[ i ][ j ] = 0;
			}
		}

		for( int i = 0; i < results.length; i++ )
		{
			for( int j = 0; j < UPPER_LIMIT; j++ )
			{
				recordPoints[ i ][ j ] = 0;
			}
		}

		for( int t = 0; t < results.length; t++ )
		{
			for( int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++ )
			{
				double magnitude = Math.log( results[ t ][ freq ].abs() + 1 );

				int freqRange = getFrequencyRange( freq );

				// Save the highest magnitude and corresponding frequency:
				if( magnitude > highScores[ t ][ freqRange ] )
				{
					highScores[ t ][ freqRange ] = magnitude;
					recordPoints[ t ][ freq ] = 1;
					points[ t ][ freqRange ] = freq;
				}
			}

			for( int k = 0; k < 5; k++ )
			{
				stringBuilder.append( "" + highScores[ t ][ k ] + ";" + recordPoints[ t ][ k ] + "\t" );
			}

			stringBuilder.append( "\n" );

			long h = getHash( points[ t ][ 0 ], points[ t ][ 1 ], points[ t ][ 2 ], points[ t ][ 3 ] );

			List<DataPoint> listPoints = _hashes.get( h );

			if( listPoints != null )
			{
				listPoints.add( new DataPoint( _songId, t ) );
			}
			else
			{
				listPoints = new ArrayList<DataPoint>();
				listPoints.add( new DataPoint( _songId, t ) );
				_hashes.put( h, listPoints );
			}
		}

		return new AudioAnalyserResult( _songId,  stringBuilder.toString(), results, CHUNK_SIZE, highScores, recordPoints );
	}

	public int matchSong( byte audio[] )
	{
		Complex[][] results = performFFT( audio );

		// Map<SongId, Map<Offset, Count>>
		Map<Integer, Map<Integer, Integer>> candidates = new HashMap<Integer, Map<Integer, Integer>>();

		double recordPoints[][] = new double[ results.length ][ UPPER_LIMIT ];
		double highScores[][] = new double[ results.length ][ 5 ];
		long points[][] = new long[ results.length ][ 5 ];

		for( int i = 0; i < results.length; i++ )
		{
			for( int j = 0; j < 5; j++ )
			{
				highScores[ i ][ j ] = 0;
				points[ i ][ j ] = 0;
			}
		}

		for( int i = 0; i < results.length; i++ )
		{
			for( int j = 0; j < UPPER_LIMIT; j++ )
			{
				recordPoints[ i ][ j ] = 0;
			}
		}

		for( int t = 0; t < results.length; t++ )
		{
			for( int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++ )
			{
				double magnitude = Math.log( results[ t ][ freq ].abs() + 1 );

				int freqRange = getFrequencyRange( freq );

				// Save the highest magnitude and corresponding frequency:
				if( magnitude > highScores[ t ][ freqRange ] )
				{
					highScores[ t ][ freqRange ] = magnitude;
					recordPoints[ t ][ freq ] = 1;
					points[ t ][ freqRange ] = freq;
				}
			}

			long h = getHash( points[ t ][ 0 ], points[ t ][ 1 ], points[ t ][ 2 ], points[ t ][ 3 ] );

			List<DataPoint> listPoints = _hashes.get( h );

			if( ( listPoints ) != null )
			{
				for( DataPoint dataPoint : listPoints )
				{
					int offset = Math.abs( dataPoint.time - t );

					Map<Integer, Integer> entry = candidates.get( dataPoint.id );

					if( entry == null )
					{
						entry = new HashMap<Integer, Integer>();
						entry.put( offset, 1 );

						candidates.put( dataPoint.id, entry );
					}
					else
					{
						Integer count = entry.get( offset );

						if( count == null )
						{
							entry.put( offset, 1 );
						}
						else
						{
							entry.put( offset, count + 1 );
						}
					}
				}
			}
		}

		return getBestMatch( candidates );
	}

	@Override
	public String toString()
	{
		return "[AudioAnalyser]";
	}
}