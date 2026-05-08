package net.litetex.capes.provider.suppliers;

public final class NameExtraFormatter
{
	@SuppressWarnings("checkstyle:MagicNumber")
	public static String format(final String value)
	{
		if(value.isBlank())
		{
			return "";
		}
		
		final String name = value.trim();
		return " - " + (name.length() > 35 ? (name.substring(0, 30) + "...") : name);
	}
	
	private NameExtraFormatter()
	{
	}
}
