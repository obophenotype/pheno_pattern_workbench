package monarch.ontology.phenoworkbench.browser.basic;

public class RunOption {
	
	final private String name;
	
	private String value="";
	
	RunOption(String name) {
		this.name=name;
	}
	
	RunOption(String name, String defaultValue) {
		this.name=name;
		this.value = defaultValue;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

}
