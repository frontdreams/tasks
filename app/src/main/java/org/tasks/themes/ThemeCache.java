package org.tasks.themes;

import static androidx.core.content.ContextCompat.getColor;
import static com.google.common.collect.ImmutableList.copyOf;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.tasks.R;
import org.tasks.billing.Inventory;
import org.tasks.injection.ApplicationScope;
import org.tasks.injection.ForApplication;
import org.tasks.preferences.Preferences;

@ApplicationScope
public class ThemeCache {

  public static final String EXTRA_THEME_OVERRIDE = "extra_theme_override";

  private final List<ThemeBase> themes = new ArrayList<>();
  private final List<ThemeColor> colors = new ArrayList<>();
  private final List<ThemeAccent> accents = new ArrayList<>();
  private final List<WidgetTheme> widgetThemes = new ArrayList<>();
  private final ThemeColor untaggedColor;
  private final Preferences preferences;
  private final Inventory inventory;

  @Inject
  public ThemeCache(Preferences preferences, Inventory inventory, @ForApplication Context context) {
    this.preferences = preferences;
    this.inventory = inventory;
    Resources resources = context.getResources();

    themes.add(
        new ThemeBase(
            context.getString(R.string.theme_light),
            0,
            getColor(context, R.color.grey_50),
            AppCompatDelegate.MODE_NIGHT_NO));
    themes.add(
        new ThemeBase(
            context.getString(R.string.theme_black),
            1,
            getColor(context, R.color.widget_background_black),
            AppCompatDelegate.MODE_NIGHT_YES));
    themes.add(
        new ThemeBase(
            context.getString(R.string.theme_dark),
            2,
            getColor(context, R.color.md_background_dark),
            AppCompatDelegate.MODE_NIGHT_YES));
    themes.add(
        new ThemeBase(
            context.getString(R.string.theme_wallpaper),
            3,
            getColor(context, R.color.black_38),
            AppCompatDelegate.MODE_NIGHT_YES));
    themes.add(
        new ThemeBase(
            context.getString(R.string.theme_day_night),
            4,
            getColor(context, R.color.grey_50),
            AppCompatDelegate.MODE_NIGHT_AUTO));

    String[] colorNames = resources.getStringArray(R.array.colors);
    for (int i = 0; i < ThemeColor.COLORS.length; i++) {
      colors.add(
          new ThemeColor(
              context, colorNames[i], i, ContextCompat.getColor(context, ThemeColor.COLORS[i])));
    }
    String[] accentNames = resources.getStringArray(R.array.accents);
    for (int i = 0; i < ThemeAccent.ACCENTS.length; i++) {
      Resources.Theme theme = new ContextThemeWrapper(context, ThemeAccent.ACCENTS[i]).getTheme();
      accents.add(new ThemeAccent(accentNames[i], i, resolveAttribute(theme, R.attr.colorSecondary)));
    }
    String[] widgetBackgroundNames = resources.getStringArray(R.array.widget_background);
    for (int i = 0; i < WidgetTheme.BACKGROUNDS.length; i++) {
      widgetThemes.add(
          new WidgetTheme(
              widgetBackgroundNames[i],
              i,
              getColor(context, WidgetTheme.BACKGROUNDS[i]),
              getColor(context, i == 0 ? R.color.black_87 : R.color.white_100),
              getColor(context, i == 0 ? R.color.black_54 : R.color.white_70)));
    }
    untaggedColor =
        new ThemeColor(context, null, 19, getColor(context, R.color.tag_color_none_background));
  }

  private static int resolveAttribute(Resources.Theme theme, int attribute) {
    TypedValue typedValue = new TypedValue();
    theme.resolveAttribute(attribute, typedValue, true);
    return typedValue.data;
  }

  public WidgetTheme getWidgetTheme(int index) {
    return widgetThemes.get(index);
  }

  public ThemeBase getThemeBase() {
    return getThemeBase(null);
  }

  public ThemeBase getThemeBase(@Nullable Intent intent) {
    int index = preferences.getInt(R.string.p_theme, 0);
    if (intent != null && intent.hasExtra(EXTRA_THEME_OVERRIDE)) {
      index = intent.getIntExtra(EXTRA_THEME_OVERRIDE, 0);
    } else if (index > 1 && !inventory.hasPro()) {
      index = 0;
    }
    return getThemeBase(index);
  }

  public ThemeBase getThemeBase(int index) {
    return themes.get(index);
  }

  public ThemeColor getThemeColor(int index) {
    return colors.get(index);
  }

  public ThemeAccent getThemeAccent(int index) {
    return accents.get(index);
  }

  public ThemeColor getUntaggedColor() {
    return untaggedColor;
  }

  public List<ThemeAccent> getAccents() {
    return copyOf(accents);
  }

  public List<ThemeColor> getColors() {
    return copyOf(colors);
  }

  public List<WidgetTheme> getWidgetThemes() {
    return copyOf(widgetThemes);
  }
}
