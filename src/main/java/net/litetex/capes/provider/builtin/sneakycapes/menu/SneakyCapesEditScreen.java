package net.litetex.capes.provider.builtin.sneakycapes.menu;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.NativeImage;

import net.litetex.capes.Capes;
import net.litetex.capes.provider.builtin.sneakycapes.pixels.SneakyCapesCopyPixels;
import net.litetex.capes.provider.builtin.sneakycapes.pixels.SneakyCapesMarkerPixels;
import net.litetex.capes.ui.playerpreview.PreviewPlayerScreen;
import net.litetex.capes.util.CapeProviderTextureAsset;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.PngInfo;


public class SneakyCapesEditScreen extends PreviewPlayerScreen<ViewModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(SneakyCapesEditScreen.class);
	private static final Identifier ID_CAPE_TEXTURE = Identifier.fromNamespaceAndPath(Capes.MOD_ID, "sc-temp-cape");
	private static final Supplier<Path> BASE_TEMP_DIR_PATH = Suppliers.memoize(() -> {
		try
		{
			return Files.createTempDirectory("cp-sc-dwl");
		}
		catch(IOException ioe)
		{
			throw new UncheckedIOException(ioe);
		}
	});
	
	private static final ScopedValue<Boolean> IGNORE_TXT_CAPE_SOURCE_FILE_PATH_RESPONDER = ScopedValue.newInstance();
	
	private final LinearLayout contentLayout = LinearLayout.vertical().spacing(5);
	
	private final MultiLineTextWidget lblCapeSource;
	private final EditBox txtCapeSourceFilePath;
	private final MultiLineTextWidget lblLoadResult;
	private final Button btnApplyAndDownload;
	private final MultiLineTextWidget lblSaveResult;
	
	private ScrollableLayout scrollableLayout;
	
	private Optional<PathAndNativeImage> loadedCapeInfo = Optional.empty();
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public SneakyCapesEditScreen(final Minecraft client, final Screen lastScreen)
	{
		super(client, lastScreen, "Sneaky Capes Editor", new ViewModel());
		
		this.lblCapeSource = new MultiLineTextWidget(
			Component.literal("Drag and drop the cape file (.png) here or enter location"),
			this.font);
		this.contentLayout.addChild(this.lblCapeSource);
		
		this.txtCapeSourceFilePath = new EditBox(
			this.font,
			20,
			20,
			Component.literal("Cape source file path"));
		this.txtCapeSourceFilePath.setMaxLength(4000);
		this.txtCapeSourceFilePath.setResponder(s -> {
			if(IGNORE_TXT_CAPE_SOURCE_FILE_PATH_RESPONDER.isBound()
				&& Boolean.TRUE.equals(IGNORE_TXT_CAPE_SOURCE_FILE_PATH_RESPONDER.get()))
			{
				return;
			}
			
			this.updateCapeFilePath(toCleanCapeFilePath(s));
		});
		this.contentLayout.addChild(this.txtCapeSourceFilePath);
		
		this.lblLoadResult = new MultiLineTextWidget(Component.empty(), this.font);
		this.contentLayout.addChild(this.lblLoadResult);
		
		this.btnApplyAndDownload = Button.builder(
				Component.literal("Apply to skin and download"),
				_ -> this.applyToCurrentSkinAndDownload(this.loadedCapeInfo.orElseThrow()))
			.build();
		this.btnApplyAndDownload.active = false;
		this.contentLayout.addChild(this.btnApplyAndDownload);
		
		this.lblSaveResult = new MultiLineTextWidget(Component.empty(), this.font);
		this.contentLayout.addChild(this.lblSaveResult);
	}
	
	@Override
	protected int initSelfManagedDrawableChildsDeterminePreviewX(final int contentY, final int contentWidth)
	{
		this.lblCapeSource.setMaxWidth(contentWidth);
		this.txtCapeSourceFilePath.setWidth(contentWidth);
		this.lblLoadResult.setMaxWidth(contentWidth);
		this.btnApplyAndDownload.setWidth(contentWidth);
		this.lblSaveResult.setMaxWidth(contentWidth);
		
		this.scrollableLayout = new ScrollableLayout(
			this.minecraft,
			this.contentLayout,
			this.layout.getContentHeight(),
			ScrollableLayout.ReserveStrategy.RIGHT);
		this.scrollableLayout.visitWidgets(this::addSelfManagedDrawableChild);
		this.scrollableLayout.setPosition(DEFAULT_SPACING, contentY);
		this.scrollableLayout.arrangeElements();
		
		return DEFAULT_SPACING
			+ contentWidth
			+ DEFAULT_SPACING;
	}
	
	private void updateCapeFilePath(final Path path)
	{
		this.lblLoadResult.setMessage(Component.empty());
		this.viewModel.updateCapeTextureSupplier(null);
		this.btnApplyAndDownload.active = false;
		this.loadedCapeInfo = Optional.empty();
		
		final Result<PathAndNativeImage> loadedNativeImage = checkCapeFileAtPath(path)
			.flatMap(p -> {
				try
				{
					return Result.ok(new PathAndNativeImage(
						p,
						NativeImage.read(Files.readAllBytes(p))));
				}
				catch(final Exception ex)
				{
					LOG.warn("Failed to read image", ex);
					return Result.error("Failed to read image: " + ex.getMessage());
				}
			});
		
		if(loadedNativeImage.hasError())
		{
			this.lblLoadResult.setMessage(errorComp(loadedNativeImage.errorMsg()));
		}
		else
		{
			this.loadedCapeInfo = Optional.ofNullable(loadedNativeImage.result());
			this.loadedCapeInfo.map(PathAndNativeImage::nativeImage).ifPresent(ni -> {
				this.minecraft.getTextureManager()
					.register(ID_CAPE_TEXTURE, new DynamicTexture(ID_CAPE_TEXTURE::toString, ni));
				this.viewModel.updateCapeTextureSupplier(() -> new CapeProviderTextureAsset(ID_CAPE_TEXTURE));
			});
			this.btnApplyAndDownload.active = true;
		}
		// Otherwise multi line text might be incorrectly aligned
		this.scrollableLayout.arrangeElements();
	}
	
	private void applyToCurrentSkinAndDownload(final PathAndNativeImage pathAndNativeImage)
	{
		this.lblSaveResult.setMessage(Component.empty());
		
		// Find the native image of the body
		final AbstractTexture texture =
			this.minecraft.getTextureManager().getTexture(this.viewModel.skin().body().texturePath());
		if(!(texture instanceof final DynamicTexture dynamicBodyTexture))
		{
			LOG.warn("Failed to find body texture - was: {}", texture);
			this.lblSaveResult.setMessage(errorComp("Failed to find body texture"));
			// Otherwise multi line text might be incorrectly aligned
			this.scrollableLayout.arrangeElements();
			return;
		}
		
		final NativeImage img = new NativeImage(64, 64, true);
		final NativeImage originalBodyImg = dynamicBodyTexture.getPixels();
		
		// Copy everything from body over
		originalBodyImg.copyRect(
			img,
			0, 0,
			0, 0,
			originalBodyImg.getWidth(), originalBodyImg.getHeight(),
			false, false);
		
		// Copy cape
		SneakyCapesCopyPixels.capeToBody(pathAndNativeImage.nativeImage(), img);
		
		// Add markers
		SneakyCapesMarkerPixels.applyMarkers(img);
		
		try
		{
			final Path tempDir = BASE_TEMP_DIR_PATH.get().resolve(String.valueOf(img.getPixelBytes().hashCode()));
			if(!Files.exists(tempDir))
			{
				Files.createDirectory(tempDir);
			}
			final Path generatedFile = tempDir.resolve("skin.png");
			img.writeToFile(generatedFile);
			
			LOG.info("Successfully generated skin file: {}", generatedFile);
			this.lblSaveResult.setMessage(Component.literal("Successfully generated skin file.\n")
				.append(Component.literal(
						"You need to upload the skin in the launcher / on the website "
							+ "and restart the game to apply it!")
					.withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
			Blaze3D.openPath(tempDir);
		}
		catch(final IOException e)
		{
			LOG.warn("Unexpected problem while saving generated file", e);
			this.lblSaveResult.setMessage(errorComp("Failed to save generated file: " + e.getMessage()));
		}
		// Otherwise multi line text might be incorrectly aligned
		this.scrollableLayout.arrangeElements();
	}
	
	@Override
	public void onFilesDrop(final List<Path> files)
	{
		this.updateCapeFilePath(!files.isEmpty() ? files.getFirst() : null);
		ScopedValue.where(IGNORE_TXT_CAPE_SOURCE_FILE_PATH_RESPONDER, true)
			.run(() -> this.txtCapeSourceFilePath.setValue(this.loadedCapeInfo
				.map(PathAndNativeImage::path)
				.map(Path::toString)
				.orElse("")));
	}
	
	@Override
	public void removed()
	{
		this.releaseTempCapeTexture();
		super.removed();
	}
	
	private void releaseTempCapeTexture()
	{
		this.minecraft.getTextureManager().release(ID_CAPE_TEXTURE);
	}
	
	@SuppressWarnings("checkstyle:FinalParameters")
	@Nullable
	static Path toCleanCapeFilePath(String filePath)
	{
		filePath = filePath.trim();
		
		if(filePath.isEmpty())
		{
			return null;
		}
		
		if(filePath.startsWith("\"") && filePath.endsWith("\""))
		{
			filePath = filePath.substring(1, filePath.length() - 1);
		}
		
		// Resolve user home (Unix based systems)
		if(filePath.startsWith("~"))
		{
			filePath = System.getProperty("user.home") + filePath.substring(1);
		}
		
		try
		{
			return Paths.get(filePath);
		}
		catch(final Exception ex)
		{
			return null;
		}
	}
	
	static Result<Path> checkCapeFileAtPath(final Path path)
	{
		try
		{
			if(path == null)
			{
				return Result.error("Path is empty");
			}
			if(!Files.exists(path))
			{
				return Result.error("File does not exist");
			}
			if(!Files.isRegularFile(path))
			{
				return Result.error("File is not a regular file");
			}
			
			final String fileName = path.getFileName().toString().toLowerCase();
			if(!fileName.endsWith(".png"))
			{
				return Result.error("File does not end with .png");
			}
			
			try
			{
				final PngInfo metadata = PngInfo.fromStream(Files.newInputStream(path));
				final int width = metadata.width();
				final int height = metadata.height();
				if(width != 64 || height != 32)
				{
					return Result.error("Incorrect dimensions. Expected 64x32, got " + width + "x" + height);
				}
				
				return Result.ok(path);
			}
			catch(final Exception _)
			{
				return Result.error("Failed to read png metadata");
			}
		}
		catch(final Exception ex)
		{
			LOG.warn("Unexpected error while processing file", ex);
			return Result.error("Unexpected error: " + ex.getMessage());
		}
	}
	
	private static Component errorComp(final String text)
	{
		return Component.literal(text).withColor(TextColor.RED);
	}
	
	record PathAndNativeImage(Path path, NativeImage nativeImage)
	{
	}
	
	record Result<T>(T result, String errorMsg)
	{
		boolean hasError()
		{
			return this.errorMsg != null;
		}
		
		<R> Result<R> flatMap(final Function<T, Result<R>> mapping)
		{
			return this.hasError()
				? Result.error(this.errorMsg())
				: mapping.apply(this.result());
		}
		
		static <T> Result<T> ok(final T result)
		{
			return new Result<>(result, null);
		}
		
		static <T> Result<T> error(final String errorMsg)
		{
			return new Result<>(null, errorMsg);
		}
	}
}
