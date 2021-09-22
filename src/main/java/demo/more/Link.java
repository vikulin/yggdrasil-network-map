package demo.more;

public class Link implements Comparable<Link> {
	
	public Link(String keyFrom, String keyTo) {
		this.keyFrom = keyFrom;
		this.keyTo = keyTo;
	}
	
	private String keyFrom;
	
	private String keyTo;

	public String getKeyFrom() {
		return keyFrom;
	}

	public void setKeyFrom(String key) {
		this.keyFrom = key;
	}

	public String getKeyTo() {
		return keyTo;
	}

	public void setKeyTo(String key) {
		this.keyTo = key;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.keyFrom!=null && this.keyTo!=null && obj instanceof Link && this.keyFrom.equals(((Link)obj).getKeyFrom()) && this.keyTo.equals(((Link)obj).getKeyTo());
	}
	
	@Override
	public int hashCode() {
		String key = this.keyFrom+this.keyTo;
		return key.hashCode();
	}

	@Override
	public int compareTo(Link o) {
		if(o!=null && o.getKeyFrom()!=null && keyFrom!=null) {
			if(keyFrom.compareTo(o.getKeyFrom())==0) {
				return keyTo.compareTo(o.getKeyTo());
			}
			return keyFrom.compareTo(o.getKeyFrom());
		}
		return -1;
	}
}
