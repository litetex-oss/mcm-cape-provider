package net.litetex.capes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.litetex.capes.fabric.FabricModDetector;


public class CapesMixinPlugin implements IMixinConfigPlugin
{
	private static final String MIXIN_PACKAGE = "net.litetex.capes.mixins.";
	
	static final Map<String, BooleanSupplier> CONDITIONS = Map.of(
		MIXIN_PACKAGE + "SpecialGuiElementRendererRegisterGuiRendererMixin",
		() -> !FabricModDetector.isFabricRenderingApiPresent()
	);
	
	@Override
	public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName)
	{
		final BooleanSupplier supplier = CONDITIONS.get(targetClassName);
		return supplier == null || supplier.getAsBoolean();
	}
	
	// region Boiler
	
	@Override
	public void onLoad(final String mixinPackage)
	{
	}
	
	@Override
	public String getRefMapperConfig()
	{
		return null;
	}
	
	@Override
	public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets)
	{
	}
	
	@Override
	public List<String> getMixins()
	{
		return null;
	}
	
	@Override
	public void preApply(
		final String targetClassName,
		final ClassNode targetClass,
		final String mixinClassName,
		final IMixinInfo mixinInfo)
	{
	}
	
	@Override
	public void postApply(
		final String targetClassName,
		final ClassNode targetClass,
		final String mixinClassName,
		final IMixinInfo mixinInfo)
	{
	}
	
	// endregion
}
