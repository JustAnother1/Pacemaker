package de.nomagic.printerController.core;

public class Reference 
{
	private String source = null;
	private String command = null;

	public Reference() 
	{
	}

	public Reference(String source) 
	{
		this.source = source;
	}

	public void setCommand(String command) 
	{
		this.command = command;
	}

}
