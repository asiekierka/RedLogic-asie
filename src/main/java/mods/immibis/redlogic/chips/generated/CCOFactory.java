package mods.immibis.redlogic.chips.generated;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import mods.immibis.redlogic.chips.util.UniqueIDGenerator;

public class CCOFactory {
	public static CCOFactory instance;
	
	private final String NAME_PREFIX = "generated.immibis.redlogic.GeneratedCircuitClass_";
	
	private final File dataDir;
	private final UniqueIDGenerator idGen;
	
	public CCOFactory(File dataDir) {
		if(dataDir.exists() && !dataDir.isDirectory())
			throw new RuntimeException(dataDir+" exists and is not a directory");
		if(!dataDir.exists() && !dataDir.mkdirs())
			throw new RuntimeException("failed to create directory: "+dataDir);
		this.dataDir = dataDir;
		this.idGen = new UniqueIDGenerator(new File(dataDir, "session-counter"));
	}
	
	private class GenClassLoader extends ClassLoader {
		public GenClassLoader(ClassLoader parent) {
			super(parent);
		}
		
		@Override
		protected synchronized Class<?> findClass(String className) throws ClassNotFoundException {
			
			FileInputStream in;
			try {
				in = new FileInputStream(getCacheFile(className));
			} catch(FileNotFoundException ex) {
				throw new ClassNotFoundException(className);
			}
			
			try {
				try {
					FileChannel ch = in.getChannel();
					
					byte[] bytes = new byte[(int)ch.size()];
					ByteBuffer buf = ByteBuffer.wrap(bytes);
					
					while(buf.position() < buf.capacity())
						if(ch.read(buf) < 0)
							throw new IOException("unexpected end of stream");
					
					return defineClass(className, bytes, 0, bytes.length);
					
				} finally {
					in.close();
				}
			} catch(IOException ex) {
				throw new ClassNotFoundException(className, ex);
			}
		}
	}
	private GenClassLoader loader = new GenClassLoader(CCOFactory.class.getClassLoader());
	
	public String generateClassName() {
		return NAME_PREFIX + idGen.generateID();
	}
	
	private File getCacheFile(String className) {
		return new File(dataDir, className + ".class");
	}
	
	public void registerClass(String className, byte[] classBytes) throws IOException {
		File file = getCacheFile(className);
		if(file.exists())
			throw new RuntimeException("Class "+className+" already stored in cache; file is "+file);
		
		FileOutputStream out = new FileOutputStream(file);
		try {
			out.write(classBytes);
		} finally {
			out.close();
		}
	}

	public CompiledCircuitObject createObject(String className) throws IOException {
		if(!className.startsWith(NAME_PREFIX))
			throw new RuntimeException("Wrong class name format: "+className);
		for(int k = NAME_PREFIX.length(); k < className.length(); k++) {
			char ch = className.charAt(k);
			if(!Character.isJavaIdentifierPart(ch))
				throw new RuntimeException("Wrong class name format: "+className);
		}
		try {
			Class<?> clazz = Class.forName(className, true, loader);
			return clazz.asSubclass(CompiledCircuitObject.class).getConstructor().newInstance();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static byte[] serialize(CompiledCircuitObject circuit) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baos).writeObject(circuit);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}

	public static CompiledCircuitObject deserialize(byte[] byteArray) throws IOException, ClassNotFoundException {
		return (CompiledCircuitObject)new ObjectInputStream(new ByteArrayInputStream(byteArray)) {
			@Override
			protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
				if(desc.getName().startsWith("generated."))
					return Class.forName(desc.getName(), true, CCOFactory.instance.loader);
				return super.resolveClass(desc);
			}
		}.readObject();
	}
	
	
}
