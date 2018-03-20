package monarch.ontology.phenoworkbench.util;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

public class Files {
	private final File tmpdir = com.google.common.io.Files.createTempDir();

	private Files() {
		// Exists only to defeat instantiation.
	}

	private static Files instance = null;

	public static Files getInstance() {
		if(instance == null) {
			instance = new Files();
		}
		return instance;
	}

	public Optional<File> deleteMakeTmpDirectory(String name) {
		File dir = new File(getTmpdir(),name);
		if (deleteMake(dir)) return Optional.of(dir);
		return Optional.empty();
	}

	private boolean deleteMake(File dir) {
		try {
			if(dir.exists()) FileUtils.forceDelete(dir);
			dir.mkdir();
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	public File getTmpdir() {
		return tmpdir;
	}

}
