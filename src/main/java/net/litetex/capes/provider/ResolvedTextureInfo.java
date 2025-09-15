package net.litetex.capes.provider;

import org.apache.commons.codec.binary.Base64;


public interface ResolvedTextureInfo
{
	byte[] imageBytes();
	
	boolean animated();
	
	record ByteArrayTextureInfo(byte[] imageBytes, boolean animated) implements ResolvedTextureInfo
	{
	}
	
	record Base64TextureInfo(String base64Texture, boolean animated) implements ResolvedTextureInfo
	{
		@Override
		public byte[] imageBytes()
		{
			if(this.base64Texture == null || this.base64Texture.isEmpty())
			{
				return null;
			}
			try
			{
				return Base64.decodeBase64(this.base64Texture);
			}
			catch(final Exception ex)
			{
				return null;
			}
		}
	}
}
