package net.litetex.capes.provider.suppliers;

import java.util.function.Supplier;
import java.util.stream.Stream;

import net.litetex.capes.provider.ModMetadataProvider;


@SuppressWarnings({"checkstyle:InterfaceIsType", "PMD.ConstantsInInterface"})
public interface ModMetadataProviderSupplier extends Supplier<Stream<ModMetadataProvider>>
{
	String CAPE = "cape";
}
