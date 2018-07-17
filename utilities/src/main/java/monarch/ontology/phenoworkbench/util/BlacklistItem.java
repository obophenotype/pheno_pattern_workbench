package monarch.ontology.phenoworkbench.util;

public class BlacklistItem {

	private final String label;
	private final Object context;

	private final OntologyClass blacklisted;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blacklisted == null) ? 0 : blacklisted.hashCode());
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlacklistItem other = (BlacklistItem) obj;
		if (blacklisted == null) {
			if (other.blacklisted != null)
				return false;
		} else if (!blacklisted.equals(other.blacklisted))
			return false;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	BlacklistItem(Object context, OntologyClass blacklisted) {
		this.context = context;
		this.blacklisted = blacklisted;
		this.label = blacklisted.getLabel() + " (" + context.toString() + ")";
	}

	public BlacklistItem(OntologyClass c) {
		this.context = "global";
		this.blacklisted = c;
		this.label = blacklisted.getLabel() + " (" + context.toString() + ")";
	}

	public String getLabel() {
		return label;
	}

	public Object getContext() {
		return context;
	}
	
	public boolean isClassContext() {
		return context instanceof OntologyClass;
	}

	public OntologyClass getBlacklisted() {
		return blacklisted;
	}

}
