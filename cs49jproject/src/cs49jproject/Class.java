package cs49jproject;

import java.util.ArrayList;

public class Class {
	
	private String packageName;
	private String className;
	private String accessSpecifier;
	private int starline;
	private String constructorParameter;
	private String constructor;
	private ArrayList<Variable> instanceVariable;
	private ArrayList<String> paramType;
	
	public Class()
	{
		this.className = null;
		this.packageName = null;
		this.setAccessSpecifier("package");
		this.setStartingLine(0);
		this.instanceVariable = new ArrayList<>();
		this.paramType = new ArrayList<>();
		this.constructor = constructor;
		this.constructorParameter = constructorParameter;
	}
	public String getPackage() 
	{
		return packageName;
	}
	
	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public String getClassName() 
	{
		return className;
	}

	public void setAccessSpecifier(String group) {
		this.accessSpecifier = group;
	}

	public void setClassName(String className)
	{
		this.className = className;
		
	}

	public void setStartingLine(int linenumber) 
	{
		this.starline = linenumber;
	}

	public String getAccesSpecifier()
	{
		return accessSpecifier;
	}

	public int getStartingLine() 
	{
		return this.starline;
	}

	public void addInstanceVariable(String type, String name, String value) 
	{
		this.instanceVariable.add(new Variable(type,name,value));
		
	}

	public void setInstanceVariable(ArrayList<Variable> instanceVariables) 
	{
		this.instanceVariable = instanceVariable;
	}
	
	
	public ArrayList<Variable> getInstanceVariable()
	{
		return instanceVariable;
	}
	public String getConstructorParameter()
	{
		return constructorParameter;
	}
	
	public void setConstructorParameter(String constructorParameter)
	{
		this.constructorParameter = constructorParameter;
	}

	public void addParamType(String typeName) 
	{
		paramType.add(typeName);
	}
	
	public ArrayList<String> getParamType() 
	{
		return paramType;
	}
	public String getConstructor() {
		return constructor;
	}
	public void setConstructor(String constructor) {
		this.constructor = constructor;
	}

}
