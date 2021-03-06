package haven.factories;

import haven.ItemInfo;
import haven.Resource;

public class CenteroflearningFactory implements ItemInfo.InfoFactory {
    public CenteroflearningFactory() {
    }

    public ItemInfo build(ItemInfo.Owner var1, Object... var2) {
        double var3 = ((Number) var2[1]).doubleValue();
        String str = Resource.getLocString(Resource.BUNDLE_LABEL, "Learning bonus: +%d%%");
        return new ItemInfo.AdHoc(var1, String.format(str, Math.round(var3 * 100.0D)));
    }
}