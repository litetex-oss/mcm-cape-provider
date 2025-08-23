package net.litetex.capes.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.ratelimit.CapeProviderRateLimits;
import net.litetex.capes.util.GameProfileUtil;
import net.litetex.capes.util.collections.DiscardingQueue;
import net.litetex.capes.util.collections.MaxSizedHashMap;
import net.minecraft.util.logging.UncaughtExceptionHandler;


@SuppressWarnings({"checkstyle:MagicNumber", "PMD.GodClass"})
public class PlayerCapeHandlerManager
{
	private static final Logger LOG = LoggerFactory.getLogger(PlayerCapeHandlerManager.class);
	
	private final ExecutorService loadExecutors;
	
	private final Map<UUID, PlayerCapeHandler> instances;
	private final RealPlayerValidator realPlayerValidator;
	private final Map<Future<?>, UUID> submittedTasks = Collections.synchronizedMap(new WeakHashMap<>());
	private final CapeProviderRateLimits capeProviderRateLimits = new CapeProviderRateLimits();
	
	private final Capes capes;
	private final boolean debugEnabled;
	
	public PlayerCapeHandlerManager(final Capes capes)
	{
		this.capes = capes;
		this.debugEnabled = LOG.isDebugEnabled();
		
		final int nThreads = Optional.ofNullable(capes.config().getLoadThreads())
			.filter(x -> x > 0 && x < 1_000)
			.orElse(2);
		final int loadExecutorWorkQueueSize = Math.max(capes.playerCacheSize() / 10, 10);
		LOG.debug("LoadExecutor threads={} workQueue size={}", nThreads, loadExecutorWorkQueueSize);
		this.loadExecutors =
			new ThreadPoolExecutor(
				nThreads,
				nThreads,
				0L,
				TimeUnit.MILLISECONDS,
				new DiscardingQueue<>(
					loadExecutorWorkQueueSize, r -> {
					LOG.warn("Overloaded - Discarded loading task for {}", this.submittedTasks.get(r));
				}),
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
		this.realPlayerValidator = new RealPlayerValidator(
			capes.playerCacheSize(),
			capes.useRealPlayerOnlineValidation());
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
		if(this.debugEnabled)
		{
			LOG.debug("onLoadTexture: {}/{} validate={}", profile.getName(), profile.getId(), validateProfile);
		}
		
		final Runnable runnable = () -> {
			try
			{
				this.onLoadTextureInternalAsync(profile, validateProfile, capeProviders, onAfterLoaded);
			}
			catch(final Exception ex)
			{
				LOG.warn("Failed to async load texture for {}/{}", profile.getName(), profile.getId(), ex);
			}
		};
		this.submittedTasks.put(this.loadExecutors.submit(runnable), profile.getId());
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
			.filter(cp -> {
				this.capeProviderRateLimits.waitForRateLimit(cp);
				return handler.trySetCape(cp);
			})
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
