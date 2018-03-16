package monarch.ontology.phenoworkbench.browser.basic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Branches {
	public static File prepareBranchesFile(File tmp, List<Branch> branches) {
		File branchfile = new File(tmp,"branches.txt");
		
		List<String> branchesfile = new ArrayList<>();
		branches.forEach(b->branchesfile.add(b.getBranchiri()));
		
		try {
			if(branchfile.exists()) FileUtils.deleteQuietly(branchfile);
			FileUtils.writeLines(branchfile, branchesfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return branchfile;
	}
}
