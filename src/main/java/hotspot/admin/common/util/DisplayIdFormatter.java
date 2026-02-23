package hotspot.admin.common.util;

import hotspot.admin.common.domain.DisplayIdType;

public final class DisplayIdFormatter {

    private DisplayIdFormatter() {
    }

    public static String format(DisplayIdType displayIdType, Long id) {
        if (displayIdType == null || id == null) {
            return null;
        }

        String format = "%s-%0" + displayIdType.getPadLength() + "d";
        return format.formatted(displayIdType.getPrefix(), id);
    }
}
