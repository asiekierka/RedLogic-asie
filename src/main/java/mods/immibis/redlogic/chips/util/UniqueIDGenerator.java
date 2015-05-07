package mods.immibis.redlogic.chips.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Unnecessarily complicated unique ID generator
 */
public final class UniqueIDGenerator {
	private long sessionID;
	private long startTime;
	private long counter;
	
	public synchronized String generateID() {
		return String.format("%016X_%016X_%016X", sessionID, startTime, (counter++));
	}
	
	public UniqueIDGenerator(File counterFile) {
		try {
			sessionID = readAndIncrementSessionCounterFile(counterFile, 0);
			startTime = System.currentTimeMillis();
			counter = 0;
			
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static long readAndIncrementSessionCounterFile(File f, int tries) throws IOException {
		
		if(tries > 5)
			throw new IOException("repeatedly failed to open "+f);
		
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		
		try {
			FileChannel ch = raf.getChannel();
			
			FileLock lock = ch.lock();
			
			try {
				long size = ch.size();
				
				if(size != 0 && size != 8)
					throw new IOException(f+" has wrong size (should be 8 bytes, is "+size+")");
				
				ByteBuffer buf = ByteBuffer.allocate(8);
				
				long oldValue;
				
				if(size != 0) {
					while(buf.position() < 8)
						if(ch.read(buf) < 0)
							throw new IOException("failed to read 8 bytes from "+f);
		
					buf.flip();
					oldValue = buf.asLongBuffer().get();
				
				} else {
					oldValue = System.nanoTime(); // unpredictable starting value in case counter file is lost
				}
				
				ch.position(0);
				buf.asLongBuffer().put(oldValue + 1);
				while(buf.position() < 8)
					if(ch.write(buf) < 0)
						throw new IOException("failed to rewrite "+f);
				
				return oldValue;
				
			} finally {
				lock.release();
			}
		} finally {
			raf.close();
		}
	}
}
