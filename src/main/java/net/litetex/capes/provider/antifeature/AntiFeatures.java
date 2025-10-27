package net.litetex.capes.provider.antifeature;

import java.util.Map;

import net.litetex.capes.i18n.CapesI18NKeys;


public final class AntiFeatures
{
	public static final AntiFeature BAD_CONNECTION =
		new DefaultAntiFeature(CapesI18NKeys.ANTI_FEATURE_BAD_CONNECTION);
	public static final AntiFeature PAYMENT_TO_UNLOCK_CAPE =
		new DefaultAntiFeature(CapesI18NKeys.ANTI_FEATURE_PAYMENT_TO_UNLOCK_CAPE);
	public static final AntiFeature EXPLICIT =
		new DefaultAntiFeature(CapesI18NKeys.ANTI_FEATURE_EXPLICIT);
	public static final AntiFeature ABANDONED =
		new DefaultAntiFeature(CapesI18NKeys.ANTI_FEATURE_ABANDONED);
	public static final AntiFeature OVERWRITES =
		new DefaultAntiFeature(CapesI18NKeys.ANTI_FEATURE_OVERWRITES);
	
	public static final Map<String, AntiFeature> ALL_DEFAULT = Map.ofEntries(
		Map.entry("bad_connection", BAD_CONNECTION),
		Map.entry("payment_to_unlock_cape", PAYMENT_TO_UNLOCK_CAPE),
		Map.entry("explicit", EXPLICIT),
		Map.entry("abandoned", ABANDONED),
		Map.entry("overwrite", OVERWRITES)
	);
	
	private AntiFeatures()
	{
	}
}
