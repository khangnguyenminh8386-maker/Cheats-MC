/*
 * Decompiled with CFR 0.152.
 */
package night.gui.api;

import java.util.List;
import night.gui.api.Button;

public interface ExpandableRow {
    public String getRowName();

    public boolean isOpen();

    public float getOpenAmount();

    public List<Button> getButtons();

    public void setRevealHeight(int var1);

    public void setSearchQuery(String var1);
}

