package cs49jproject;


public class Variable 
{

	private String type;
	private String name;
	private String value;
	
	public Variable(String type, String name, String value) 
	{
		this.type = type;
		this.name = name;
		this.value = value;
	}
	
	public Variable(String type) 
	{
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
