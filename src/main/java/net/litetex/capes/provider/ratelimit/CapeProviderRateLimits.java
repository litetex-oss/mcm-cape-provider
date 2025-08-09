package net.litetex.capes.provider.ratelimit;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import net.litetex.capes.provider.CapeProvider;


public class CapeProviderRateLimits
{
	private static final Logger LOG = LoggerFactory.getLogger(CapeProviderRateLimits.class);
	
	private final Map<CapeProvider, Optional<RateLimiter>> limiters = Collections.synchronizedMap(new HashMap<>());
	
	public void waitForRateLimit(final CapeProvider capeProvider)
	{
		this.limiters.computeIfAbsent(
				capeProvider,
				cp -> cp.rateLimitedReqPerSec() > 0
					? Optional.of(RateLimiter.create(cp.rateLimitedReqPerSec(), Duration.ofSeconds(1)))
					: Optional.empty())
			.ifPresent(rateLimiter -> {
				final double waitedSec = rateLimiter.acquire();
				if(waitedSec > 0)
				{
					LOG.debug("{} waited for {}ms", capeProvider.id(), (int)(waitedSec * 1000));
				}
			});
	}
}
