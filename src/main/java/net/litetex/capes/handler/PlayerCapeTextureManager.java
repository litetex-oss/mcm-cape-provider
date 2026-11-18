package net.litetex.capes.handler;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ExternalTextureProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureResources;
import net.minecraft.resources.Identifier;


// See also: SkinTextureDownloader
public class PlayerCapeTextureManager
{
	private static final Logger LOG = LoggerFactory.getLogger(PlayerCapeTextureManager.class);
	
	private final Map<Identifier, TextureResources> textures = new ConcurrentHashMap<>();
	
	public void registerTextures(final Collection<? extends TextureToRegister> texturesToRegister)
	{
		final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		// Do texturing work NOT on Render thread
		CompletableFuture.runAsync(
				() -> texturesToRegister.forEach(t ->
					this.registerTexture(textureManager, t.identifier(), t.image())),
				Minecraft.getInstance())
			.exceptionally(ex -> {
				LOG.warn("Failed to register textures", ex);
				return null;
			});
	}
	
	private void registerTexture(
		final TextureManager textureManager,
		final Identifier identifier,
		final NativeImage image)
	{
		try(image)
		{
			final TextureResources texture = TextureResources.from2dImage(identifier::toString, image);
			textureManager.registerProvider(identifier, new ExternalTextureProvider(texture));
			final TextureResources old = this.textures.put(identifier, texture);
			if(old != null)
			{
				old.close();
			}
		}
	}
	
	public void unload(final Collection<Identifier> ids)
	{
		for(final Identifier id : ids)
		{
			final TextureResources textureResources = this.textures.remove(id);
			if(textureResources != null)
			{
				this.freeTexture(id, textureResources);
			}
		}
	}
	
	public void clear()
	{
		this.textures.forEach(this::freeTexture);
		this.textures.clear();
	}
	
	private void freeTexture(final Identifier id, final TextureResources t)
	{
		try
		{
			t.close();
			Minecraft.getInstance().getTextureManager().release(id);
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to close texture[id={}]", id, ex);
		}
	}
	
	public interface TextureToRegister
	{
		Identifier identifier();
		
		NativeImage image();
	}
}
