package dev.ruzgar.bettercoords;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Settings screen opened from Mod Menu. Edits are applied and saved when the screen closes
 * (Done button or Escape).
 */
public class BetterCoordsConfigScreen extends Screen {
	private static final int CONTROL_WIDTH = 200;
	private static final int CONTROL_HEIGHT = 20;
	private static final int FIELD_WIDTH = 62;
	private static final int FIELD_GAP = 7;
	private static final Pattern NUMBER = Pattern.compile("-?\\d*\\.?\\d*");

	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA0A0A0;
	private static final int SECTION = 0xFFFFD75F;

	// Row layout (scaled GUI pixels). Kept compact so everything fits at the minimum 240px GUI height.
	private static final int TITLE_Y = 6;
	private static final int MOD_TOGGLE_Y = 18;
	private static final int GLITCH_TOGGLE_Y = 40;
	private static final int FACING_TOGGLE_Y = 62;
	private static final int SECTION_Y = 88;
	private static final int FAKE_TOGGLE_Y = 100;
	private static final int FIELD_LABEL_Y = 124;
	private static final int FIELD_Y = 134;
	private static final int REANCHOR_Y = 158;
	private static final int INFO_Y = 182;
	private static final int INFO_WIDTH = 260;

	private final Screen parent;

	// Screen-local state so edits survive a window resize (init() re-creates the widgets).
	private boolean modEnabled;
	private boolean glitchEnabled;
	private boolean showFacing;
	private boolean fakeEnabled;
	private String xText;
	private String yText;
	private String zText;
	private boolean reanchorRequested;

	private TextFieldWidget xField;
	private TextFieldWidget yField;
	private TextFieldWidget zField;
	private ButtonWidget reanchorButton;

	public BetterCoordsConfigScreen(Screen parent) {
		super(Text.translatable("bettercoords.config.title"));
		this.parent = parent;

		BetterCoordsConfig cfg = BetterCoords.config();
		this.modEnabled = cfg.enabled;
		this.glitchEnabled = cfg.glitchEnabled;
		this.showFacing = cfg.showFacing;
		this.fakeEnabled = cfg.fakeCoordsEnabled;
		this.xText = formatNumber(cfg.fakeX);
		this.yText = formatNumber(cfg.fakeY);
		this.zText = formatNumber(cfg.fakeZ);
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int left = centerX - CONTROL_WIDTH / 2;

		// Mod on/off
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.modEnabled)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.enabled.tooltip")))
				.build(left, MOD_TOGGLE_Y, CONTROL_WIDTH, CONTROL_HEIGHT,
						Text.translatable("bettercoords.config.enabled"),
						(button, value) -> this.modEnabled = value));

		// Glitch mode
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.glitchEnabled)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.glitch.tooltip")))
				.build(left, GLITCH_TOGGLE_Y, CONTROL_WIDTH, CONTROL_HEIGHT,
						Text.translatable("bettercoords.config.glitch"),
						(button, value) -> this.glitchEnabled = value));

		// Facing line
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.showFacing)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.facing.tooltip")))
				.build(left, FACING_TOGGLE_Y, CONTROL_WIDTH, CONTROL_HEIGHT,
						Text.translatable("bettercoords.config.facing"),
						(button, value) -> this.showFacing = value));

		// --- Fake coordinates section ---
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.fakeEnabled)
				.tooltip(value -> Tooltip.of(Text.translatable("bettercoords.config.fake.enabled.tooltip")))
				.build(left, FAKE_TOGGLE_Y, CONTROL_WIDTH, CONTROL_HEIGHT,
						Text.translatable("bettercoords.config.fake.enabled"),
						(button, value) -> {
							this.fakeEnabled = value;
							this.updateFakeControls();
						}));

		this.xField = this.addNumberField(left, FIELD_Y, this.xText, Text.translatable("bettercoords.config.fake.x"), s -> this.xText = s);
		this.yField = this.addNumberField(left + FIELD_WIDTH + FIELD_GAP, FIELD_Y, this.yText, Text.translatable("bettercoords.config.fake.y"), s -> this.yText = s);
		this.zField = this.addNumberField(left + 2 * (FIELD_WIDTH + FIELD_GAP), FIELD_Y, this.zText, Text.translatable("bettercoords.config.fake.z"), s -> this.zText = s);

		this.reanchorButton = this.addDrawableChild(ButtonWidget.builder(
						Text.translatable("bettercoords.config.fake.reanchor"),
						button -> {
							this.reanchorRequested = true;
							button.setMessage(Text.translatable("bettercoords.config.fake.reanchor.done"));
							button.active = false;
						})
				.dimensions(left, REANCHOR_Y, CONTROL_WIDTH, CONTROL_HEIGHT)
				.tooltip(Tooltip.of(Text.translatable("bettercoords.config.fake.reanchor.tooltip")))
				.build());
		if (this.reanchorRequested) {
			this.reanchorButton.setMessage(Text.translatable("bettercoords.config.fake.reanchor.done"));
		}

		// Done
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
				.dimensions(left, this.height - 26, CONTROL_WIDTH, CONTROL_HEIGHT)
				.build());

		this.updateFakeControls();
	}

	private TextFieldWidget addNumberField(int x, int y, String initial, Text label, java.util.function.Consumer<String> onChange) {
		TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, FIELD_WIDTH, CONTROL_HEIGHT, label);
		field.setMaxLength(20);
		field.setTextPredicate(s -> NUMBER.matcher(s).matches());
		field.setText(initial);
		field.setChangedListener(onChange);
		return this.addDrawableChild(field);
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
		int left = centerX - CONTROL_WIDTH / 2;

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, TITLE_Y, WHITE);

		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.translatable("bettercoords.config.fake.section"), centerX, SECTION_Y, SECTION);

		// X / Y / Z labels above the fields
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("bettercoords.config.fake.x"), left + FIELD_WIDTH / 2, FIELD_LABEL_Y, WHITE);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("bettercoords.config.fake.y"), left + FIELD_WIDTH + FIELD_GAP + FIELD_WIDTH / 2, FIELD_LABEL_Y, WHITE);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("bettercoords.config.fake.z"), left + 2 * (FIELD_WIDTH + FIELD_GAP) + FIELD_WIDTH / 2, FIELD_LABEL_Y, WHITE);

		context.drawWrappedTextWithShadow(this.textRenderer,
				Text.translatable("bettercoords.config.fake.info"), centerX - INFO_WIDTH / 2, INFO_Y, INFO_WIDTH, GRAY);
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
