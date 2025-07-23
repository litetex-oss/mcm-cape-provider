package net.litetex.capes.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.util.GameProfileUtil;
import net.litetex.capes.util.collections.MaxSizedHashMap;
import net.minecraft.util.logging.UncaughtExceptionHandler;


@SuppressWarnings({"checkstyle:MagicNumber", "PMD.GodClass"})
public class PlayerCapeHandlerManager
{
	private static final Logger LOG = LoggerFactory.getLogger(PlayerCapeHandlerManager.class);
	
	private final ExecutorService loadExecutors;
	
	private final Map<UUID, PlayerCapeHandler> instances;
	private final RealPlayerValidator realPlayerValidator;
	
	private final Capes capes;
	
	public PlayerCapeHandlerManager(final Capes capes)
	{
		this.capes = capes;
		this.loadExecutors = Executors.newFixedThreadPool(
			Optional.ofNullable(capes.config().getLoadThreads())
				.filter(x -> x > 0 && x < 1_000)
				.orElse(2),
			new ThreadFactory()
			{
				private static final AtomicInteger COUNTER = new AtomicInteger(0);
				
				@Override
				public Thread newThread(@NotNull final Runnable r)
				{
					final Thread thread = new Thread(r);
					thread.setName("Cape-" + COUNTER.getAndIncrement());
					thread.setDaemon(true);
					thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(LOG));
					return thread;
				}
			});
		
		this.instances = Collections.synchronizedMap(new MaxSizedHashMap<>(capes.playerCacheSize()));
		this.realPlayerValidator = new RealPlayerValidator(capes.playerCacheSize());
	}
	
	public PlayerCapeHandler getProfile(final GameProfile profile)
	{
		return this.instances.get(profile.getId());
	}
	
	// Only use this when required to keep RAM consumption low!
	public PlayerCapeHandler getOrCreateProfile(final GameProfile profile)
	{
		return this.instances.computeIfAbsent(profile.getId(), ignored -> new PlayerCapeHandler(this.capes, profile));
	}
	
	public void clearCache()
	{
		this.instances.clear();
	}
	
	public void onLoadTexture(final GameProfile profile)
	{
		this.onLoadTexture(
			profile,
			this.capes.validateProfile(),
			this.capes.activeCapeProviders(),
			null);
	}
	
	public void onLoadTexture(
		final GameProfile profile,
		final boolean validateProfile,
		final Collection<CapeProvider> capeProviders,
		final Runnable onAfterLoaded)
	{
		if(LOG.isDebugEnabled())
		{
			LOG.debug("onLoadTexture: {}/{} validate={}", profile.getName(), profile.getId(), validateProfile);
		}
		this.loadExecutors.submit(() -> {
			try
			{
				this.onLoadTextureInternalAsync(profile, validateProfile, capeProviders, onAfterLoaded);
			}
			catch(final Exception ex)
			{
				LOG.warn("Failed to async load texture for {}/{}", profile.getName(), profile.getId(), ex);
			}
		});
	}
	
	private void onLoadTextureInternalAsync(
		final GameProfile profile,
		final boolean validateProfile,
		final Collection<CapeProvider> capeProviders,
		final Runnable onAfterLoaded)
	{
		if(this.shouldOnlyLoadForSelfAndIsNotSelf(profile)
			|| validateProfile && !capeProviders.isEmpty() && !this.realPlayerValidator.isReal(profile))
		{
			return;
		}
		
		final PlayerCapeHandler handler = this.getOrCreateProfile(profile);
		handler.resetCape();
		
		final Optional<CapeProvider> optFoundCapeProvider = capeProviders.stream()
			.filter(handler::trySetCape)
			.findFirst();
		
		if(LOG.isDebugEnabled())
		{
			optFoundCapeProvider.ifPresentOrElse(
				cp ->
					LOG.debug("Loaded cape from {} for {}/{}", cp.id(), profile.getName(), profile.getId()),
				() -> LOG.debug("Found no cape for {}/{}", profile.getName(), profile.getId())
			);
		}
		
		if(onAfterLoaded != null)
		{
			onAfterLoaded.run();
		}
	}
	
	private boolean shouldOnlyLoadForSelfAndIsNotSelf(final GameProfile profile)
	{
		return this.capes.config().isOnlyLoadForSelf()
			&& !GameProfileUtil.isSelf(profile);
	}
}
