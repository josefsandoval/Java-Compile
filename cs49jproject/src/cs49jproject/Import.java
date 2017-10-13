package cs49jproject;

public class Import {

	private String name;
	private int linenumber;
	
	public Import(String name, int linenumber)
	{
		this.name = name;
		this.linenumber = linenumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLinenumber() {
		return linenumber;
	}


	public String toString()
	{
		return String.format("%s", name);
	}
}
