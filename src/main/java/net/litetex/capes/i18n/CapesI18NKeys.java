package net.litetex.capes.i18n;

import net.litetex.capes.Capes;


public final class CapesI18NKeys
{
	private static final String PREFIX = "options." + Capes.MOD_ID + ".";
	
	public static final String CAPE_OPTIONS = PREFIX + "cape_options";
	
	public static final String PREVIEW = PREFIX + "preview";
	public static final String ACTIVATED_PROVIDERS = PREFIX + "activated_providers";
	public static final String TOGGLE_ELYTRA = PREFIX + "toggle_elytra";
	public static final String TOGGLE_PLAYER = PREFIX + "toggle_player";
	
	public static final String MANAGE_PROVIDERS = PREFIX + "manage_providers";
	
	public static final String OTHER = PREFIX + "other";
	public static final String ANIMATED_TEXTURES = PREFIX + "animated_textures";
	public static final String ELYTRA_TEXTURE = PREFIX + "elytra_texture";
	public static final String FROZEN = PREFIX + "frozen";
	public static final String ONLY_LOAD_YOUR_CAPE = PREFIX + "only_load_your_cape";
	public static final String PROVIDERS_FROM_MODS = PREFIX + "providers_from_mods";
	public static final String LOAD = PREFIX + "load";
	public static final String LOAD_PROVIDERS = PREFIX + "load_providers";
	public static final String ACTIVATE_PROVIDERS_BY_DEFAULT = PREFIX + "activate_providers_by_default";
	
	private static final String ANTI_FEATURE_PREFIX = PREFIX + "anti_feature.";
	public static final String ANTI_FEATURE_BAD_CONNECTION = ANTI_FEATURE_PREFIX + "bad_connection";
	public static final String ANTI_FEATURE_PAYMENT_TO_UNLOCK_CAPE = ANTI_FEATURE_PREFIX + "payment_to_unlock_cape";
	public static final String ANTI_FEATURE_EXPLICIT = ANTI_FEATURE_PREFIX + "explicit";
	public static final String ANTI_FEATURE_ABANDONED = ANTI_FEATURE_PREFIX + "abandoned";
	public static final String ANTI_FEATURE_OVERWRITES = ANTI_FEATURE_PREFIX + "overwrite";
	
	private CapesI18NKeys()
	{
	}
}
