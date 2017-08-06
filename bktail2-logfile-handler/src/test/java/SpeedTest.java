import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import de.bkusche.bktail2.logfilehandler.internal.BufferedRandomAccessFile;

public class SpeedTest {

	final static int CR = 10;
	
	Map<Long,Range> res = new HashMap<>();
	List<Long> rangeList = new LinkedList<>();
	
	public Function<File, Long> countLines = t -> {
		try (Stream<String> stream = Files.lines(t.toPath())) {
			return stream.parallel().count();
		} catch(Throwable e){}
		return 0L;
	};
	
	public Function<File, Long> countLines2 = t -> {
		try(BufferedInputStream r = new BufferedInputStream(new FileInputStream(t))) {
			long lines = 0;

		    byte[] buffer = new byte[8192];
		    int read;

		    while ((read = r.read(buffer)) != -1) {
		        for (int i = 0; i < read; i++) {
		            if (buffer[i] == CR) lines++;
		        }
		    }

			return lines;
		} catch (Throwable e) {}
		return 0L;
	};
	
	public Function<File, Long> countLines3 = t -> {
		try (FileChannel fc =  FileChannel.open(t.toPath())) {
	        ByteBuffer buffer = ByteBuffer.allocate(32784);
	        int read = 0;
	        long lines = 0;
	        while ((read = fc.read(buffer)) != -1) {
	            buffer.flip();

	            while (buffer.hasRemaining()) {
	                if( (char) buffer.get() == CR) lines ++;                    
	            }
	            buffer.clear();
	        }
	        return lines;
		} catch(Throwable e){e.getStackTrace();}
		return 0L;
	};
	
	
	public Function<File, List<Long>> fileMapping = t -> {
		
		try(BufferedInputStream r = new BufferedInputStream(new FileInputStream(t))) {
			long lines = 0;

		    byte[] buffer = new byte[8192];
		    int read = 0;
		    long to = 0; 
		    long from = 0;
 
		    while ((read = r.read(buffer)) != -1) {
		        for (int i = 0; i < read; i++) {
		            if (buffer[i] == CR){
		            	
		            	to += i;
//		            	System.out.println("line: "+lines+" from: "+from+" to: "+to);
		            	if( rangeList.size() <= lines){
//		            		res.put(lines, new Range(from, to));
//		            		res.put(lines, null);
//		            		rangeList.add(new Range(from, to));
		            		rangeList.add(to);
		            	}
		            	lines++;
		            	from = to;
		            }
		        }
		    }

			return rangeList;
		} catch (Throwable e) {}
		return rangeList;
	};
	
	private List<String> readLines1(Path p, int from, int limit){
		List<String> lineRange = new LinkedList<>();
		try (Stream<String> stream = Files.lines(p)) {
			stream.skip(from).limit(limit)
				.forEach(lineRange::add); //TODO evaluate performance especially with large files
		} catch (Throwable e) {
			// 
		}
		return lineRange;
	}
	
	
	private List<String> readLines2(Path p, final long from, final long limit){
		List<String> lineRange = new LinkedList<>();
		long lineStrart = 0;
		long start = System.currentTimeMillis();
		try(BufferedInputStream r = new BufferedInputStream(new FileInputStream(p.toFile()))) {	
		    final byte[] buffer = new byte[8192];
		    final int[] line = new int[1];
		    final boolean[] reading = new boolean[]{true};
		    int read = 0;
		    while ((read = r.read(buffer)) != -1 && reading[0]) {
		    	lineStrart = searchFromIndex(buffer, line, reading, read, from, lineStrart);
		    }
		    
		} catch (Throwable e) {e.printStackTrace();}
		
		System.out.println("searching: "+(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		try(BufferedRandomAccessFile braf = new BufferedRandomAccessFile(p.toFile(), "r", 8192)){
			int line = 0;
			braf.seek(lineStrart+1);
			while( line < limit){
				line++;
				lineRange.add(braf.readNextLine());
			}
		} catch (Throwable e) {}
		System.out.println("reading "+limit+" from: "+lineStrart+" - "+(System.currentTimeMillis()-start));
		return lineRange;
	}
	
	private final long searchFromIndex(final byte[] buffer, final int[] line, final boolean[] reading, 
			final int read, final long from, long lineStrart) {

		for (int i = 0; i < read; i++) {
			if (buffer[i] == CR) {
				line[0]++;
				if (line[0] == from) {
					reading[0] = false;
					break;
				}
			}
			lineStrart++;
		}
		return lineStrart;
	}
	
	private List<String> readLines3(Path p, final long from, final long to){
		List<String> lineRange = new LinkedList<>();
		long lineStrart = 0;
		long start = System.currentTimeMillis();
		try(BufferedInputStream r = new BufferedInputStream(new FileInputStream(p.toFile()))) {	
		    final byte[] buffer = new byte[8192];
		    final int[] line = new int[1];
		    final boolean[] reading = new boolean[]{true};
		    int read = 0;
		    while ((read = r.read(buffer)) != -1 && reading[0]) {
		    	readLine(buffer, line, reading, read, from, to);
		    }
		    
		} catch (Throwable e) {e.printStackTrace();}
		
		System.out.println("searching: "+(System.currentTimeMillis()-start));
		System.out.println("reading "+to+" from: "+lineStrart+" - "+(System.currentTimeMillis()-start));
		return lineRange;
	}
	
	private void readLine(final byte[] buffer, final int[] line, final boolean[] reading, 
			final int read, final long from, final long to) {

		for (int i = 0; i < read; i++) {
			if (buffer[i] == CR) {
				line[0]++;
			} else {
				if (line[0] >= from/* && line[0] <= to*/ ) {
					
				} else if( line[0] > to){
					reading[0] = false;
					break;
				}
			}
			
			
		}
		
	}
	
	public static void main(String[] args) {

		SpeedTest t = new SpeedTest();
		t.doCountTests();
		t.doReadTests();
	}
	
	
	public void doCountTests(){
		File f = new File("/opt/test1GB.log");
		long start = System.currentTimeMillis();
		long lines = countLines.apply(f);
		System.out.println(lines +" lines in "+ (System.currentTimeMillis()-start)+" ms");
		
		start = System.currentTimeMillis();
		lines = countLines2.apply(f);
		System.out.println(lines +" lines in "+ (System.currentTimeMillis()-start)+" ms");
		
		start = System.currentTimeMillis();
		lines = countLines3.apply(f);
		System.out.println(lines +" lines in "+ (System.currentTimeMillis()-start)+" ms");
////		
		start = System.currentTimeMillis();
		List<Long> res = fileMapping.apply(f);
		System.out.println(res.size() +" lines in "+ (System.currentTimeMillis()-start)+" ms "+res.hashCode());
		
//		start = System.currentTimeMillis();
//		res = fileMapping.apply(f);
//		System.out.println(res.size() +" lines in "+ (System.currentTimeMillis()-start)+" ms "+res.hashCode());
	}
	
	public void doReadTests(){
		final File f = new File("/opt/test1GB.log");
		final int from = 8829288;
//		final int from = 150000;
//		final int from = 50000;
		final int limit = 10000;
		final boolean out = true;
		long start = System.currentTimeMillis();
		List<String> l1 = readLines1(f.toPath(), from, limit);
		System.out.println((System.currentTimeMillis()-start)+" ms");
		
		
//		start = System.currentTimeMillis();
//		List<String> l2 = readLines2(f.toPath(), from, limit);
//		System.out.println((System.currentTimeMillis()-start)+" ms");
//		System.out.println(l2.size());
////		l2.forEach(System.out::println);
//		
//		System.out.println("size equal: "+(l1.size()==l2.size()));
//		if( out ){
//			for( int i = 0; i < l1.size(); i++){
//				if( !l1.get(i).equals(l2.get(i))){
//					System.out.println("error in: "+i);
//					System.out.println("l1: "+l1.get(i));
//					for( int i2 = i; i2 < i+10; i2++)
//					System.out.println("l2: "+l2.get(i2));
//					return;
//				}
//			}
//			System.out.println("nice job!");
//		}
		
		start = System.currentTimeMillis();
		List<String> l3 = readLines3(f.toPath(), from, from+limit);
		System.out.println((System.currentTimeMillis()-start)+" ms");
	}
	
	
	
	
}
