package dev.ruzgar.bettercoords;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Settings screen opened from Mod Menu: a two column grid of switches, the two toggle keys, and the
 * fake coordinate controls. Edits are applied and saved when the screen closes (Done or Escape).
 */
public class BetterCoordsConfigScreen extends Screen {
	private static final int PANEL_WIDTH = 304;
	private static final int COL_WIDTH = 149;
	private static final int COL_GAP = 6;
	private static final int ROW_HEIGHT = 20;
	private static final int FIELD_WIDTH = 97;
	private static final Pattern NUMBER = Pattern.compile("-?\\d*\\.?\\d*");

	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA0A0A0;
	private static final int SECTION = 0xFFFFD75F;
	private static final int DIVIDER = 0x40FFFFFF;

	// Row layout (scaled GUI pixels). Kept compact so everything fits at the minimum 240px height.
	private static final int TITLE_Y = 7;
	private static final int TOGGLE_ROW_1_Y = 22;
	private static final int TOGGLE_ROW_2_Y = 46;
	private static final int KEYS_SECTION_Y = 73;
	private static final int KEYS_ROW_Y = 85;
	private static final int FAKE_SECTION_Y = 112;
	private static final int FIELD_LABEL_Y = 124;
	private static final int FIELD_Y = 134;
	private static final int REANCHOR_Y = 158;
	private static final int INFO_Y = 183;

	private static final int LISTEN_NONE = -1;
	private static final int LISTEN_MOD = 0;
	private static final int LISTEN_FAKE = 1;

	private final Screen parent;

	// Screen-local state so edits survive a window resize (init() re-creates the widgets).
	private boolean modEnabled;
	private boolean glitchEnabled;
	private boolean showFacing;
	private boolean fakeEnabled;
	private int modKey;
	private int fakeKey;
	private String xText;
	private String yText;
	private String zText;
	private boolean reanchorRequested;
	private int listening = LISTEN_NONE;

	private TextFieldWidget xField;
	private TextFieldWidget yField;
	private TextFieldWidget zField;
	private ButtonWidget reanchorButton;
	private ButtonWidget modKeyButton;
	private ButtonWidget fakeKeyButton;

	public BetterCoordsConfigScreen(Screen parent) {
		super(Text.translatable("bettercoords.config.title"));
		this.parent = parent;

		BetterCoordsConfig cfg = BetterCoords.config();
		this.modEnabled = cfg.enabled;
		this.glitchEnabled = cfg.glitchEnabled;
		this.showFacing = cfg.showFacing;
		this.fakeEnabled = cfg.fakeCoordsEnabled;
		this.modKey = cfg.toggleModKey;
		this.fakeKey = cfg.toggleFakeCoordsKey;
		this.xText = formatNumber(cfg.fakeX);
		this.yText = formatNumber(cfg.fakeY);
		this.zText = formatNumber(cfg.fakeZ);
	}

	@Override
	protected void init() {
		int left = this.left();
		int rightCol = left + COL_WIDTH + COL_GAP;

		// --- switches, two per row ---
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.modEnabled)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.enabled.tooltip")))
				.build(left, TOGGLE_ROW_1_Y, COL_WIDTH, ROW_HEIGHT,
						Text.translatable("bettercoords.config.enabled"),
						(button, value) -> this.modEnabled = value));

		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.glitchEnabled)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.glitch.tooltip")))
				.build(rightCol, TOGGLE_ROW_1_Y, COL_WIDTH, ROW_HEIGHT,
						Text.translatable("bettercoords.config.glitch"),
						(button, value) -> this.glitchEnabled = value));

		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.showFacing)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.facing.tooltip")))
				.build(left, TOGGLE_ROW_2_Y, COL_WIDTH, ROW_HEIGHT,
						Text.translatable("bettercoords.config.facing"),
						(button, value) -> this.showFacing = value));

		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.fakeEnabled)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.fake.enabled.tooltip")))
				.build(rightCol, TOGGLE_ROW_2_Y, COL_WIDTH, ROW_HEIGHT,
						Text.translatable("bettercoords.config.fake.enabled"),
						(button, value) -> {
							this.fakeEnabled = value;
							this.updateFakeControls();
						}));

		// --- toggle keys ---
		this.modKeyButton = this.addDrawableChild(ButtonWidget.builder(
						Text.empty(), button -> this.startListening(LISTEN_MOD))
				.dimensions(left, KEYS_ROW_Y, COL_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.of(Text.translatable("bettercoords.config.keys.hint")))
				.build());

		this.fakeKeyButton = this.addDrawableChild(ButtonWidget.builder(
						Text.empty(), button -> this.startListening(LISTEN_FAKE))
				.dimensions(rightCol, KEYS_ROW_Y, COL_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.of(Text.translatable("bettercoords.config.keys.hint")))
				.build());

		// --- fake coordinates ---
		this.xField = this.addNumberField(left, this.xText, Text.translatable("bettercoords.config.fake.x"), s -> this.xText = s);
		this.yField = this.addNumberField(left + FIELD_WIDTH + COL_GAP, this.yText, Text.translatable("bettercoords.config.fake.y"), s -> this.yText = s);
		this.zField = this.addNumberField(left + 2 * (FIELD_WIDTH + COL_GAP), this.zText, Text.translatable("bettercoords.config.fake.z"), s -> this.zText = s);

		this.reanchorButton = this.addDrawableChild(ButtonWidget.builder(
						Text.translatable("bettercoords.config.fake.reanchor"), button -> {
							this.reanchorRequested = true;
							button.setMessage(Text.translatable("bettercoords.config.fake.reanchor.done"));
							button.active = false;
						})
				.dimensions(left, REANCHOR_Y, PANEL_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.of(Text.translatable("bettercoords.config.fake.reanchor.tooltip")))
				.build());
		if (this.reanchorRequested) {
			this.reanchorButton.setMessage(Text.translatable("bettercoords.config.fake.reanchor.done"));
		}

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
				.dimensions(left + PANEL_WIDTH / 4, this.height - 26, PANEL_WIDTH / 2, ROW_HEIGHT)
				.build());

		this.refreshKeyButtons();
		this.updateFakeControls();
	}

	private int left() {
		return this.width / 2 - PANEL_WIDTH / 2;
	}

	private TextFieldWidget addNumberField(int x, String initial, Text label, Consumer<String> onChange) {
		TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, FIELD_Y, FIELD_WIDTH, ROW_HEIGHT, label);
		field.setMaxLength(20);
		field.setTextPredicate(s -> NUMBER.matcher(s).matches());
		field.setText(initial);
		field.setChangedListener(onChange);
		return this.addDrawableChild(field);
	}

	private void startListening(int slot) {
		this.listening = slot;
		this.refreshKeyButtons();
	}

	/** Escape clears a binding; anything else becomes the new key. */
	@Override
	public boolean keyPressed(KeyInput input) {
		if (this.listening == LISTEN_NONE) {
			return super.keyPressed(input);
		}

		int key = input.key() == GLFW.GLFW_KEY_ESCAPE ? Keybinds.UNBOUND : input.key();
		if (this.listening == LISTEN_MOD) {
			this.modKey = key;
			if (key != Keybinds.UNBOUND && this.fakeKey == key) {
				this.fakeKey = Keybinds.UNBOUND;
			}
		} else {
			this.fakeKey = key;
			if (key != Keybinds.UNBOUND && this.modKey == key) {
				this.modKey = Keybinds.UNBOUND;
			}
		}

		this.listening = LISTEN_NONE;
		this.refreshKeyButtons();
		return true;
	}

	private void refreshKeyButtons() {
		this.modKeyButton.setMessage(keyButtonText("bettercoords.config.keys.mod", this.modKey, this.listening == LISTEN_MOD));
		this.fakeKeyButton.setMessage(keyButtonText("bettercoords.config.keys.fake", this.fakeKey, this.listening == LISTEN_FAKE));
	}

	private static Text keyButtonText(String labelKey, int keyCode, boolean listening) {
		Text value = listening
				? Text.literal("> ? <").formatted(Formatting.YELLOW)
				: Keybinds.displayName(keyCode);
		return Text.translatable(labelKey, value);
	}

	private void updateFakeControls() {
		boolean on = this.fakeEnabled;
		this.xField.setEditable(on);
		this.yField.setEditable(on);
		this.zField.setEditable(on);
		this.xField.active = on;
		this.yField.active = on;
		this.zField.active = on;
		this.reanchorButton.active = on && !this.reanchorRequested;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int centerX = this.width / 2;
		int left = this.left();

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, TITLE_Y, WHITE);

		this.drawSection(context, Text.translatable("bettercoords.config.keys.section"), KEYS_SECTION_Y);
		this.drawSection(context, Text.translatable("bettercoords.config.fake.section"), FAKE_SECTION_Y);

		// X / Y / Z labels above the fields
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("bettercoords.config.fake.x"), left + FIELD_WIDTH / 2, FIELD_LABEL_Y, WHITE);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("bettercoords.config.fake.y"), left + FIELD_WIDTH + COL_GAP + FIELD_WIDTH / 2, FIELD_LABEL_Y, WHITE);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("bettercoords.config.fake.z"), left + 2 * (FIELD_WIDTH + COL_GAP) + FIELD_WIDTH / 2, FIELD_LABEL_Y, WHITE);

		context.drawWrappedTextWithShadow(this.textRenderer,
				Text.translatable("bettercoords.config.fake.info"), left, INFO_Y, PANEL_WIDTH, GRAY);
	}

	/** Section label with a rule running out to both edges of the panel. */
	private void drawSection(DrawContext context, Text label, int y) {
		int centerX = this.width / 2;
		int left = this.left();
		int halfLabel = this.textRenderer.getWidth(label) / 2;
		int ruleY = y + 4;
		int pad = 6;

		context.drawCenteredTextWithShadow(this.textRenderer, label, centerX, y, SECTION);
		context.fill(left, ruleY, centerX - halfLabel - pad, ruleY + 1, DIVIDER);
		context.fill(centerX + halfLabel + pad, ruleY, left + PANEL_WIDTH, ruleY + 1, DIVIDER);
	}

	@Override
	public void close() {
		this.applyAndSave();
		this.client.setScreen(this.parent);
	}

	private void applyAndSave() {
		BetterCoordsConfig cfg = BetterCoords.config();

		double newX = parseNumber(this.xText, cfg.fakeX);
		double newY = parseNumber(this.yText, cfg.fakeY);
		double newZ = parseNumber(this.zText, cfg.fakeZ);

		boolean valuesChanged = newX != cfg.fakeX || newY != cfg.fakeY || newZ != cfg.fakeZ;
		boolean turnedOn = this.fakeEnabled && !cfg.fakeCoordsEnabled;

		cfg.enabled = this.modEnabled;
		cfg.glitchEnabled = this.glitchEnabled;
		cfg.showFacing = this.showFacing;
		cfg.fakeCoordsEnabled = this.fakeEnabled;
		cfg.toggleModKey = this.modKey;
		cfg.toggleFakeCoordsKey = this.fakeKey;
		cfg.fakeX = newX;
		cfg.fakeY = newY;
		cfg.fakeZ = newZ;

		// Wherever the player is on the next frame becomes the fake point.
		if (turnedOn || valuesChanged || this.reanchorRequested) {
			cfg.needsCalibration = true;
		}

		cfg.save();
	}

	private static double parseNumber(String text, double fallback) {
		try {
			return Double.parseDouble(text.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static String formatNumber(double value) {
		if (value == Math.rint(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return String.format(Locale.ROOT, "%.3f", value);
	}
}
