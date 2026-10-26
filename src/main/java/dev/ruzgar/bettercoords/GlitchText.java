package dev.ruzgar.bettercoords;

import java.util.Random;

/**
 * Corrupts a line of text into constantly shifting junk glyphs.
 *
 * <p>Two things keep it from looking like a password generator. Characters are drawn from symbol
 * blocks (arrows, box drawing, maths, Greek, Cyrillic, katakana) rather than plain ASCII, and each
 * position runs on its own timer, so the line churns continuously instead of snapping over all at
 * once. Spaces and the separators that give the line its shape are left alone.
 *
 * <p>One instance per line of text; the state is the currently shown glyphs and when each of them
 * is next due to change.
 */
public final class GlitchText {
	private static final char[] POOL = buildPool();
	private static final long MIN_HOLD_MS = 40L;
	private static final long MAX_HOLD_MS = 420L;
	private static final Random RANDOM = new Random();

	private char[] shown = new char[0];
	private long[] nextChange = new long[0];

	/** Returns junk shaped like {@code template}; only called from the render thread. */
	public String scramble(String template) {
		long now = System.currentTimeMillis();
		if (this.shown.length != template.length()) {
			this.resize(template.length(), now);
		}

		StringBuilder out = new StringBuilder(template.length());
		for (int i = 0; i < template.length(); i++) {
			char c = template.charAt(i);
			if (isStructural(c)) {
				out.append(c);
				continue;
			}
			if (now >= this.nextChange[i]) {
				this.shown[i] = randomGlyph();
				this.nextChange[i] = now + randomHold();
			}
			out.append(this.shown[i]);
		}
		return out.toString();
	}

	/** Keeps the glyphs that are still in range, so walking around does not restart the whole line. */
	private void resize(int length, long now) {
		char[] newShown = new char[length];
		long[] newNextChange = new long[length];
		for (int i = 0; i < length; i++) {
			if (i < this.shown.length) {
				newShown[i] = this.shown[i];
				newNextChange[i] = this.nextChange[i];
			} else {
				newShown[i] = randomGlyph();
				newNextChange[i] = now + randomHold();
			}
		}
		this.shown = newShown;
		this.nextChange = newNextChange;
	}

	/** Separators are kept so the line still reads as coordinates rather than a wall of noise. */
	private static boolean isStructural(char c) {
		return c == ' ' || c == '/' || c == '(' || c == ')';
	}

	private static char randomGlyph() {
		return POOL[RANDOM.nextInt(POOL.length)];
	}

	private static long randomHold() {
		return MIN_HOLD_MS + (long) RANDOM.nextInt((int) (MAX_HOLD_MS - MIN_HOLD_MS));
	}

	/**
	 * Built from code point ranges instead of literals so the source stays plain ASCII. Every range
	 * is covered by the unifont fallback the game ships, and none of them are blank or combining.
	 */
	private static char[] buildPool() {
		StringBuilder pool = new StringBuilder();
		appendRange(pool, 0x00A1, 0x00AC); // punctuation and signs, up to the soft hyphen
		appendRange(pool, 0x00AE, 0x00FF); // accented latin
		appendRange(pool, 0x0100, 0x017F); // latin extended-A
		appendRange(pool, 0x0391, 0x03A1); // greek capitals
		appendRange(pool, 0x03A3, 0x03C9); // greek capitals and lowercase
		appendRange(pool, 0x0410, 0x044F); // cyrillic
		appendRange(pool, 0x2190, 0x21BB); // arrows
		appendRange(pool, 0x2200, 0x22FF); // mathematical operators
		appendRange(pool, 0x2500, 0x257F); // box drawing
		appendRange(pool, 0x2580, 0x259F); // block elements
		appendRange(pool, 0x25A0, 0x25FF); // geometric shapes
		appendRange(pool, 0x2660, 0x266F); // card suits and music
		appendRange(pool, 0xFF66, 0xFF9D); // halfwidth katakana
		return pool.toString().toCharArray();
	}

	private static void appendRange(StringBuilder pool, int firstCodePoint, int lastCodePoint) {
		for (int c = firstCodePoint; c <= lastCodePoint; c++) {
			pool.append((char) c);
		}
	}
}
