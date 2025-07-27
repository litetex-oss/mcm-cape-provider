package net.litetex.capes.menu.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import net.litetex.capes.Capes;
import net.litetex.capes.menu.TickBoxWidget;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.DefaultMinecraftCapeProvider;
import net.litetex.capes.provider.antifeature.AntiFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;


@SuppressWarnings("checkstyle:MagicNumber")
public class ProviderListWidget extends AlwaysSelectedEntryListWidget<ProviderListWidget.ProviderListEntry>
{
	private static final int ITEM_HEIGHT = 21;
	
	private final Screen parent;
	
	public ProviderListWidget(
		final MinecraftClient client,
		final int width,
		final int height,
		final Screen parent)
	{
		super(client, width, height, 0, ITEM_HEIGHT);
		this.parent = parent;
		
		this.load();
	}
	
	private void load()
	{
		final Capes capes = Capes.instance();
		
		final List<String> activeProviderIds = new ArrayList<>(capes.config().getActiveProviderIds());
		
		this.replaceEntries(
			Stream.concat(
				capes.getAllProviders().values()
					.stream()
					.filter(Capes.EXCLUDE_DEFAULT_MINECRAFT_CP)
					.sorted(Comparator.comparing(cp -> {
						final int index = activeProviderIds.indexOf(cp.id());
						return index != -1 ? index : Integer.MAX_VALUE;
					}))
					.map(cp -> this.createEntry(
						cp,
						activeProviderIds.contains(cp.id()))),
				Stream.of(this.createEntry(
					DefaultMinecraftCapeProvider.INSTANCE,
					capes.isUseDefaultProvider()
				))
			).toList()
		);
		
		this.getFirst().upVisible(false);
		this.getEntry(this.children().size() - 2).downVisible(false);
		
		// MinecraftProvider is always there
		final ProviderListEntry last = this.children().getLast();
		last.upVisible(false);
		last.downVisible(false);
	}
	
	private ProviderListEntry createEntry(
		final CapeProvider capeProvider,
		final boolean active)
	{
		return new ProviderListEntry(
			capeProvider,
			active,
			this.parent,
			(self, a) -> this.save(),
			this::onPositionChanged);
	}
	
	private void save()
	{
		final Capes capes = Capes.instance();
		
		final List<CapeProvider> capeProviders = this.children().stream()
			.filter(ProviderListEntry::isActive)
			.map(ProviderListEntry::capeProvider)
			.toList();
		final List<String> idsWithoutDefault = capeProviders.stream()
			.filter(Capes.EXCLUDE_DEFAULT_MINECRAFT_CP)
			.map(CapeProvider::id)
			.toList();
		
		capes.config().setActiveProviderIds(idsWithoutDefault);
		capes.config().setUseDefaultProvider(idsWithoutDefault.size() != capeProviders.size());
		
		capes.saveConfigAndMarkRefresh();
	}
	
	private void onPositionChanged(final ProviderListEntry entry, final boolean up)
	{
		final List<ProviderListEntry> children = this.children();
		final int selfIndex = children.indexOf(entry);
		final int otherIndex = selfIndex + (up ? -1 : 1);
		final ProviderListEntry other = children.get(otherIndex);
		
		Collections.swap(children, selfIndex, otherIndex);
		
		final ProviderListEntry higherEntry = up ? entry : other;
		final ProviderListEntry lowerEntry = up ? other : entry;
		
		if(selfIndex == 0 || otherIndex == 0)
		{
			higherEntry.upVisible(false);
			higherEntry.downVisible(true);
			
			lowerEntry.upVisible(true);
		}
		final int lastIndex = children.size() - 2; // MinecraftCape provider is always there
		if(selfIndex == lastIndex || otherIndex == lastIndex)
		{
			higherEntry.downVisible(true);
			
			lowerEntry.upVisible(true);
			lowerEntry.downVisible(false);
		}
		
		this.save();
	}
	
	@Override
	public int getRowWidth()
	{
		return this.getWidth() - 4;
	}
	
	static class ProviderListEntry extends AlwaysSelectedEntryListWidget.Entry<ProviderListEntry>
	{
		private static final int BTN_EDIT_CAPE_WIDTH = 96;
		
		private static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE =
			Identifier.of("textures/gui/sprites/transferable_list/move_up_highlighted.png");
		private static final Identifier MOVE_UP_TEXTURE =
			Identifier.of("textures/gui/sprites/transferable_list/move_up.png");
		private static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE =
			Identifier.of("textures/gui/sprites/transferable_list/move_down_highlighted.png");
		private static final Identifier MOVE_DOWN_TEXTURE =
			Identifier.of("textures/gui/sprites/transferable_list/move_down.png");
		private static final ButtonTextures WARNING_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("social_interactions/report_button"),
			Identifier.ofVanilla("social_interactions/report_button_disabled"),
			Identifier.ofVanilla("social_interactions/report_button_highlighted")
		);
		
		private final CapeProvider capeProvider;
		
		private final Supplier<MutableText> nameTextSupplier;
		private boolean isNameHovering;
		
		private final Runnable onTxtClick;
		
		private final TickBoxWidget chbxActive;
		private final TextWidget txtName;
		private final TexturedButtonWidget icoWarn;
		private final ButtonWidget btnEditCape;
		private final UpDownIconWidget icoMoveUp;
		private final UpDownIconWidget icoMoveDown;
		
		public ProviderListEntry(
			final CapeProvider capeProvider,
			final boolean activated,
			final Screen parentScreen,
			final BiConsumer<ProviderListEntry, Boolean> onActiveChanged,
			final BiConsumer<ProviderListEntry, Boolean> onPositionChange)
		{
			this.capeProvider = capeProvider;
			
			final MinecraftClient client = MinecraftClient.getInstance();
			
			this.chbxActive = new TickBoxWidget(
				13,
				activated,
				false,
				(w, ticked) -> onActiveChanged.accept(this, ticked));
			
			final String homepageUrl = capeProvider.homepageUrl();
			final boolean hasHomePageUrl = homepageUrl != null;
			
			this.nameTextSupplier = () ->
				formatMutableTextIf(Text.literal(this.capeProvider.name()), hasHomePageUrl, Formatting.BLUE);
			this.txtName = new TextWidget(
				this.nameTextSupplier.get(),
				MinecraftClient.getInstance().textRenderer);
			
			this.txtName.active = hasHomePageUrl;
			this.onTxtClick = hasHomePageUrl
				? () -> client.setScreen(new ConfirmLinkScreen(
				open -> {
					if(open)
					{
						Util.getOperatingSystem().open(homepageUrl);
					}
					client.setScreen(parentScreen);
				}, homepageUrl, true))
				: null;
			
			final List<AntiFeature> antiFeatures = capeProvider.antiFeatures();
			this.icoWarn = !antiFeatures.isEmpty()
				? new TexturedButtonWidget(
				ITEM_HEIGHT - 4,
				ITEM_HEIGHT - 4,
				WARNING_BUTTON_TEXTURES,
				btn -> {
				},
				ScreenTexts.EMPTY)
				: null;
			if(this.icoWarn != null)
			{
				this.icoWarn.setTooltip(Tooltip.of(antiFeatures.stream()
					.map(AntiFeature::message)
					.map(t -> Text.literal("- ").append(t))
					.reduce((t1, t2) -> t1.append("\n").append(t2))
					.orElseThrow()));
			}
			
			this.btnEditCape = capeProvider.hasChangeCapeUrl()
				? ButtonWidget
				.builder(
					Text.literal("Edit cape"), btn ->
					{
						final String link = capeProvider.changeCapeUrl(client);
						client.setScreen(new ConfirmLinkScreen(
							open -> {
								if(open)
								{
									Util.getOperatingSystem().open(link);
								}
								client.setScreen(parentScreen);
							}, link, true));
					})
				.size(BTN_EDIT_CAPE_WIDTH, ITEM_HEIGHT - 4)
				.build()
				: null;
			
			this.icoMoveUp = new UpDownIconWidget(
				15,
				10,
				MOVE_UP_TEXTURE,
				MOVE_UP_HIGHLIGHTED_TEXTURE,
				32,
				32,
				Text.translatable("gui.up"),
				-16,
				-4,
				14,
				4,
				() -> onPositionChange.accept(this, true));
			this.icoMoveDown = new UpDownIconWidget(
				15,
				10,
				MOVE_DOWN_TEXTURE,
				MOVE_DOWN_HIGHLIGHTED_TEXTURE,
				32,
				32,
				Text.translatable("gui.down"),
				-16,
				-20,
				14,
				20,
				() -> onPositionChange.accept(this, false));
		}
		
		public CapeProvider capeProvider()
		{
			return this.capeProvider;
		}
		
		public boolean isActive()
		{
			return this.chbxActive.isTicked();
		}
		
		public void upVisible(final boolean visible)
		{
			this.icoMoveUp.visible = visible;
		}
		
		public void downVisible(final boolean visible)
		{
			this.icoMoveDown.visible = visible;
		}
		
		@Override
		public void render(
			final DrawContext context,
			final int index,
			final int y,
			final int x,
			final int entryWidth,
			final int entryHeight,
			final int mouseX,
			final int mouseY,
			final boolean hovered,
			final float tickDelta)
		{
			this.chbxActive.setPosition(x, y + (ITEM_HEIGHT - this.chbxActive.getHeight() - 4) / 2);
			this.chbxActive.render(context, mouseX, mouseY, tickDelta);
			
			if(this.onTxtClick != null)
			{
				final boolean isOverName = this.txtName.isHovered();
				if(this.isNameHovering != isOverName)
				{
					this.txtName.setMessage(formatMutableTextIf(
						this.nameTextSupplier.get(),
						isOverName,
						Formatting.UNDERLINE));
					
					this.isNameHovering = isOverName;
				}
			}
			
			this.txtName.setPosition(
				x + this.chbxActive.getWidth() + 4,
				y + ((ITEM_HEIGHT - this.txtName.getHeight()) / 2) - 1);
			this.txtName.render(context, mouseX, mouseY, tickDelta);
			
			if(this.icoWarn != null)
			{
				this.icoWarn.setPosition(
					x + entryWidth
						- this.icoWarn.getWidth() - BTN_EDIT_CAPE_WIDTH - this.icoMoveUp.getWidth() - (4 * 3),
					y + ((ITEM_HEIGHT - this.icoWarn.getHeight()) / 2) - 2);
				this.icoWarn.render(context, mouseX, mouseY, tickDelta);
			}
			
			if(this.btnEditCape != null)
			{
				this.btnEditCape.setPosition(
					x + entryWidth - BTN_EDIT_CAPE_WIDTH - this.icoMoveUp.getWidth() - (4 * 2),
					y);
				this.btnEditCape.render(context, mouseX, mouseY, tickDelta);
			}
			
			this.icoMoveUp.setPosition(x + entryWidth - this.icoMoveUp.getWidth() - 4, y);
			this.icoMoveUp.render(context, mouseX, mouseY, tickDelta);
			
			this.icoMoveDown.setPosition(x + entryWidth - this.icoMoveDown.getWidth() - 4, y + (ITEM_HEIGHT / 2));
			this.icoMoveDown.render(context, mouseX, mouseY, tickDelta);
		}
		
		@Override
		public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
		{
			if(this.chbxActive.isMouseOver(mouseX, mouseY))
			{
				return this.chbxActive.mouseClicked(mouseX, mouseY, button);
			}
			if(this.onTxtClick != null && this.txtName.isMouseOver(mouseX, mouseY))
			{
				this.onTxtClick.run();
				return true;
			}
			if(this.btnEditCape != null && this.btnEditCape.isMouseOver(mouseX, mouseY))
			{
				return this.btnEditCape.mouseClicked(mouseX, mouseY, button);
			}
			if(this.icoMoveUp.isMouseOver(mouseX, mouseY))
			{
				return this.icoMoveUp.mouseClicked(mouseX, mouseY, button);
			}
			else if(this.icoMoveDown.isMouseOver(mouseX, mouseY))
			{
				return this.icoMoveDown.mouseClicked(mouseX, mouseY, button);
			}
			return true; // Select
		}
		
		private static MutableText formatMutableTextIf(
			final MutableText text,
			final boolean condition,
			final Formatting formatting)
		{
			return condition ? text.formatted(formatting) : text;
		}
		
		@Override
		public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers)
		{
			if(GLFW.GLFW_KEY_SPACE == keyCode || GLFW.GLFW_KEY_ENTER == keyCode)
			{
				this.chbxActive.toggle();
				return true;
			}
			if(GLFW.GLFW_MOD_SHIFT == modifiers)
			{
				if(GLFW.GLFW_KEY_UP == keyCode && this.icoMoveUp.visible)
				{
					this.icoMoveUp.click();
					return true;
				}
				else if(GLFW.GLFW_KEY_DOWN == keyCode && this.icoMoveDown.visible)
				{
					this.icoMoveDown.click();
					return true;
				}
			}
			
			return super.keyReleased(keyCode, scanCode, modifiers);
		}
		
		@Override
		public Text getNarration()
		{
			return Text.literal(this.capeProvider().name());
		}
	}
	
	
	static class UpDownIconWidget extends ClickableWidget
	{
		private final Identifier texture;
		private final Identifier hoverTexture;
		
		private final int textureWidth;
		private final int textureHeight;
		
		private final int drawOffsetX;
		private final int drawOffsetY;
		
		private final int drawAdditionalWidth;
		private final int drawAdditionalHeight;
		
		private final Runnable onClick;
		
		@SuppressWarnings("PMD.ExcessiveParameterList")
		public UpDownIconWidget(
			final int width,
			final int height,
			final Identifier texture,
			final Identifier hoverTexture,
			final int textureWidth,
			final int textureHeight,
			final Text text,
			final int drawOffsetX,
			final int drawOffsetY,
			final int drawAdditionalWidth,
			final int drawAdditionalHeight,
			final Runnable onClick)
		{
			super(0, 0, width, height, text);
			this.texture = texture;
			this.hoverTexture = hoverTexture;
			this.textureWidth = textureWidth;
			this.textureHeight = textureHeight;
			
			this.drawOffsetX = drawOffsetX;
			this.drawOffsetY = drawOffsetY;
			
			this.drawAdditionalWidth = drawAdditionalWidth;
			this.drawAdditionalHeight = drawAdditionalHeight;
			
			this.onClick = onClick;
		}
		
		@Override
		public void onClick(final double mouseX, final double mouseY)
		{
			super.onClick(mouseX, mouseY);
			this.click();
		}
		
		public void click()
		{
			this.onClick.run();
		}
		
		@Override
		protected void renderWidget(final DrawContext context, final int mouseX, final int mouseY, final float delta)
		{
			context.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				this.isMouseOver(mouseX, mouseY) ? this.hoverTexture : this.texture,
				this.getX() + this.drawOffsetX,
				this.getY() + this.drawOffsetY,
				0.0F,
				0.0F,
				this.getWidth() + this.drawAdditionalWidth,
				this.getHeight() + this.drawAdditionalHeight,
				this.textureWidth,
				this.textureHeight
			);
		}
		
		@Override
		protected void appendClickableNarrations(final NarrationMessageBuilder builder)
		{
		}
		
		@Override
		public boolean isNarratable()
		{
			return false;
		}
	}
}
