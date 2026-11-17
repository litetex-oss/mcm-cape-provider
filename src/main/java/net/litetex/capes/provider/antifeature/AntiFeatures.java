package net.litetex.capes.provider.antifeature;

import java.util.Map;


public final class AntiFeatures
{
	public static final AntiFeature BAD_CONNECTION =
		new DefaultAntiFeature("Insecure/Unstable connection to server");
	public static final AntiFeature PAYMENT_TO_UNLOCK =
		new DefaultAntiFeature("Requires payment to unlock");
	public static final AntiFeature EXPLICIT =
		new DefaultAntiFeature("Hosts explicit/unsafe/disturbing content");
	public static final AntiFeature ABANDONED =
		new DefaultAntiFeature("Potentially abandoned");
	public static final AntiFeature OVERWRITES =
		new DefaultAntiFeature("Promotes/Displays a default cape");
	public static final AntiFeature NO_FREE_CHOICE =
		new DefaultAntiFeature("Cannot be chosen freely");
	public static final AntiFeature MICROTRANSACTIONS =
		new DefaultAntiFeature("Requires payment for some capes");
	
	public static final Map<String, AntiFeature> ALL_DEFAULT = Map.ofEntries(
		Map.entry("bad_connection", BAD_CONNECTION),
		// Legacy
		Map.entry("payment_to_unlock_cape", PAYMENT_TO_UNLOCK),
		Map.entry("payment_to_unlock", PAYMENT_TO_UNLOCK),
		Map.entry("explicit", EXPLICIT),
		Map.entry("abandoned", ABANDONED),
		Map.entry("overwrite", OVERWRITES),
		Map.entry("no_free_choice", NO_FREE_CHOICE),
		Map.entry("microtransactions", MICROTRANSACTIONS)
	);
	
	private AntiFeatures()
	{
	}
}
