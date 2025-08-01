package net.litetex.capes.config;

public enum ModProviderHandling
{
	ON(true, true),
	ONLY_LOAD(true, false),
	OFF(false, false);
	
	final boolean load;
	final boolean activateByDefault;
	
	ModProviderHandling(final boolean load, final boolean activateByDefault)
	{
		this.load = load;
		this.activateByDefault = activateByDefault;
	}
	
	public boolean load()
	{
		return this.load;
	}
	
	public boolean activateByDefault()
	{
		return this.activateByDefault;
	}
}
