package net.litetex.capes.provider.suppliers;

import java.util.function.Supplier;
import java.util.stream.Stream;

import net.litetex.capes.provider.CapeProvider;


@SuppressWarnings({"checkstyle:InterfaceIsType", "PMD.ConstantsInInterface"})
public interface ModMetadataProviderSupplier extends Supplier<Stream<CapeProvider>>
{
	String CAPE = "cape";
}
