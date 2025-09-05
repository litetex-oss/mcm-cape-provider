package net.litetex.capes.fabric;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.litetex.capes.config.CustomProviderConfig;
import net.litetex.capes.provider.ModMetadataProvider;
import net.litetex.capes.provider.suppliers.ModMetadataProviderSupplier;


public class FabricModMetadataProviderSupplier implements ModMetadataProviderSupplier
{
	private static final Logger LOG = LoggerFactory.getLogger(FabricModMetadataProviderSupplier.class);
	
	@Override
	public Stream<ModMetadataProvider> get()
	{
		return FabricLoader.getInstance().getAllMods()
			.stream()
			.filter(mc -> mc.getMetadata().containsCustomValue(CAPE))
			.map(mc -> {
				try
				{
					return this.createCustomProviderConfig(mc);
				}
				catch(final Exception e)
				{
					LOG.warn("Failed to load from {}", mc.getMetadata().getId(), e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.map(ModMetadataProvider::new);
	}
	
	protected CustomProviderConfig createCustomProviderConfig(final ModContainer mc)
	{
		final ModMetadata metadata = mc.getMetadata();
		
		final String id = "mod-" + metadata.getId();
		final String name = metadata.getName() + " (Mod)";
		final CustomValue cape = metadata.getCustomValue(CAPE);
		if(cape.getType() == CustomValue.CvType.STRING)
		{
			return new CustomProviderConfig(
				id,
				name,
				cape.getAsString()
			);
		}
		
		if(cape.getType() != CustomValue.CvType.OBJECT)
		{
			return null;
		}
		
		final CustomValue.CvObject capeObject = cape.getAsObject();
		
		final String url = Stream.of("url", "uriTemplate")
			.map(capeObject::get)
			.findFirst()
			.map(CustomValue::getAsString)
			.orElse(null);
		if(url == null)
		{
			return null;
		}
		
		return new CustomProviderConfig(
			id,
			name,
			url,
			false,
			Optional.ofNullable(capeObject.get("changeCapeUrl"))
				.map(CustomValue::getAsString)
				.orElse(null),
			Optional.of(capeObject.get("homepage"))
				.map(CustomValue::getAsString)
				.orElseGet(() -> {
					final ContactInformation contact = metadata.getContact();
					return contact.get("homepage")
						.or(() -> contact.get("sources"))
						.or(() -> contact.get("issues"))
						.orElse(null);
				}),
			null,
			Optional.ofNullable(capeObject.get("rateLimitedReqPerSec"))
				.map(CustomValue::getAsNumber)
				.map(Number::doubleValue)
				.orElse(null)
		);
	}
}
