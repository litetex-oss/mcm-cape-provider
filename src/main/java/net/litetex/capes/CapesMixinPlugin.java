package net.litetex.capes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import net.fabricmc.loader.api.FabricLoader;


public class CapesMixinPlugin implements IMixinConfigPlugin
{
	private static final Logger LOG = LoggerFactory.getLogger(CapesMixinPlugin.class);
	
	private static final String MIXIN_PACKAGE = "net.litetex.capes.mixins.";
	private static final String MIXIN_COMPAT_PACKAGE = MIXIN_PACKAGE + "compat.";
	
	static final Map<String, BooleanSupplier> CONDITIONS = Map.of();
	
	// Some mods contain invalid characters that can't be used inside the packages
	static final Map<String, String> PACKAGE_MOD_ID_OVERWRITE = Map.of();
	
	static final Set<String> APPLIED_MOD_ID_COMPATS = new HashSet<>();
	
	@Override
	public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName)
	{
		Boolean shouldApply = null;
		String compatModId = null;
		// Handle compat
		if(mixinClassName.startsWith(MIXIN_COMPAT_PACKAGE))
		{
			final String packageModId = mixinClassName.substring(MIXIN_COMPAT_PACKAGE.length()).split("\\.")[0];
			compatModId = PACKAGE_MOD_ID_OVERWRITE.getOrDefault(packageModId, packageModId);
			shouldApply = FabricLoader.getInstance().isModLoaded(compatModId);
		}
		
		final BooleanSupplier supplier = CONDITIONS.get(mixinClassName);
		final boolean apply = supplier == null
			? !Boolean.FALSE.equals(shouldApply) // null or true -> true, false -> false
			: supplier.getAsBoolean();
		if(apply)
		{
			LOG.debug("Applying {}", mixinClassName);
			if(compatModId != null
				&& !APPLIED_MOD_ID_COMPATS.contains(compatModId)
				&& APPLIED_MOD_ID_COMPATS.add(compatModId))
			{
				LOG.info("Applying compat for '{}'", compatModId);
			}
		}
		return apply;
	}
}
