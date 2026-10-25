package az.armudu.cayevi.i18n;

import java.util.Locale;
import java.util.Set;

/** Dəstəklənən dillər və Locale-dan dil kodunun çıxarılması. */
public final class Lang {

    public static final Set<String> SUPPORTED = Set.of("az", "en", "ru");

    private Lang() {
    }

    public static String of(Locale locale) {
        if (locale == null) return "az";
        String code = locale.getLanguage();
        return SUPPORTED.contains(code) ? code : "az";
    }
}
