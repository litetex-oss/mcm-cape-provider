package net.litetex.capes.fabric;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.custom.BaseCustomProviderConfig;
import net.litetex.capes.provider.custom.Owners;
import net.litetex.capes.provider.custom.local.LocalCustomProvider;
import net.litetex.capes.provider.custom.local.LocalCustomProviderConfig;
import net.litetex.capes.provider.custom.remote.RemoteCustomProvider;
import net.litetex.capes.provider.custom.remote.RemoteCustomProviderConfig;
import net.litetex.capes.provider.suppliers.ModMetadataProviderSupplier;
import net.litetex.capes.provider.suppliers.NameExtraFormatter;
import net.litetex.capes.provider.suppliers.SimpleFilesystemProviders;


public class FabricModMetadataProviderSupplier implements ModMetadataProviderSupplier
{
	private static final Logger LOG = LoggerFactory.getLogger(FabricModMetadataProviderSupplier.class);
	
	@Override
	public Stream<CapeProvider> get()
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
			.map(config -> switch(config)
			{
				case final LocalCustomProviderConfig c -> new LocalCustomProvider(c);
				case final RemoteCustomProviderConfig c -> new RemoteCustomProvider(c);
				default -> throw new IllegalStateException("Unknown config type " + config.getClass().getSimpleName());
			});
	}
	
	protected BaseCustomProviderConfig createCustomProviderConfig(final ModContainer mc)
	{
		final ModMetadata metadata = mc.getMetadata();
		
		final String id = "mod-" + metadata.getId();
		String name = metadata.getName() + " (Mod)";
		
		final CustomValue cape = metadata.getCustomValue(CAPE);
		final boolean capeIsString = cape.getType() == CustomValue.CvType.STRING;
		if(cape.getType() == CustomValue.CvType.BOOLEAN
			|| capeIsString && !cape.getAsString().startsWith("http"))
		{
			return this.createLocal(id, name + NameExtraFormatter.format(cape.getAsString()), null, mc);
		}
		else if(capeIsString)
		{
			return new RemoteCustomProviderConfig(
				id,
				name,
				cape.getAsString()
			);
		}
		
		if(cape.getType() != CustomValue.CvType.OBJECT)
		{
			return null;
		}
		
		final CustomValue.CvObject capeObj = cape.getAsObject();
		name += Optional.ofNullable(capeObj.get("name-extra"))
			.map(CustomValue::getAsString)
			.map(NameExtraFormatter::format)
			.orElse("");
		
		final Owners owners = Optional.ofNullable(capeObj.get("owners"))
			.map(CustomValue::getAsObject)
			.map(ownersObj -> new Owners(
				mapOwnersSubArray(ownersObj, "uuids", UUID::fromString),
				mapOwnersSubArray(ownersObj, "names", Function.identity()))
			)
			.orElse(null);
		
		final String url = Stream.of("url", "uriTemplate")
			.map(capeObj::get)
			.findFirst()
			.map(CustomValue::getAsString)
			.orElse(null);
		if(url == null)
		{
			return this.createLocal(id, name, owners, mc);
		}
		
		return new RemoteCustomProviderConfig(
			id,
			name,
			owners,
			url,
			null,
			Optional.ofNullable(capeObj.get("changeCapeUrl"))
				.map(CustomValue::getAsString)
				.orElse(null),
			Optional.ofNullable(capeObj.get("homepage"))
				.map(CustomValue::getAsString)
				.orElseGet(() -> {
					final ContactInformation contact = metadata.getContact();
					return contact.get("homepage")
						.or(() -> contact.get("sources"))
						.or(() -> contact.get("issues"))
						.orElse(null);
				}),
			null,
			Optional.ofNullable(capeObj.get("rateLimitedReqPerSec"))
				.map(CustomValue::getAsNumber)
				.map(Number::doubleValue)
				.orElse(null)
		);
	}
	
	private LocalCustomProviderConfig createLocal(
		final String id,
		final String name,
		final Owners owners,
		final ModContainer mc)
	{
		return mc.getRootPaths()
			.stream()
			.map(p -> p.resolve("cape").resolve(SimpleFilesystemProviders.CAPE_FILE_NAME))
			.filter(Files::exists)
			.map(p -> {
				try
				{
					return Files.readAllBytes(p);
				}
				catch(final IOException ex)
				{
					return null;
				}
			})
			.filter(Objects::nonNull)
			.findFirst()
			.map(capeTexture -> new LocalCustomProviderConfig(
				id,
				name,
				this.resolveLocalOwners(owners, mc),
				capeTexture))
			.orElse(null);
	}
	
	private Owners resolveLocalOwners(
		final Owners owners,
		final ModContainer mc)
	{
		if(owners != null)
		{
			return owners;
		}
		
		return mc.getRootPaths()
			.stream()
			.map(p -> p.resolve("cape").resolve(SimpleFilesystemProviders.OWNER_FILE_NAME))
			.filter(Files::exists)
			.map(p -> {
				try
				{
					return Files.readAllLines(p);
				}
				catch(final IOException ex)
				{
					return null;
				}
			})
			.filter(Objects::nonNull)
			.findFirst()
			.map(lines -> Owners.fromLines(LOG, lines))
			.orElse(null);
	}
	
	private static <T> Set<T> mapOwnersSubArray(
		final CustomValue.CvObject ownersObj,
		final String field,
		final Function<String, T> mapper)
	{
		return Optional.ofNullable(ownersObj.get(field))
			.map(CustomValue::getAsArray)
			.map(cva -> IntStream.range(0, cva.size())
				.mapToObj(cva::get)
				.map(CustomValue::getAsString)
				.map(mapper)
				.collect(Collectors.toSet()))
			.orElseGet(Set::of);
	}
}
