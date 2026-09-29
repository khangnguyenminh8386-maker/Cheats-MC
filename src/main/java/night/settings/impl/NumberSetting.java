/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.settings.impl;

import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.settings.Setting;

public class NumberSetting
extends Setting {
    private Number value;
    private final Number defaultValue;
    private final Number minimum;
    private final Number maximum;
    private final Number step;
    private final boolean zeroIsIgnore;

    public NumberSetting(String name, String description, Number value, Number minimum, Number maximum) {
        this(name, name, description, new Setting.Visibility(), value, minimum, maximum, 0);
    }

    public NumberSetting(String name, String tag, String description, Number value, Number minimum, Number maximum) {
        this(name, tag, description, new Setting.Visibility(), value, minimum, maximum, 0);
    }

    public NumberSetting(String name, String description, Setting.Visibility visibility, Number value, Number minimum, Number maximum) {
        this(name, name, description, visibility, value, minimum, maximum, 0);
    }

    public NumberSetting(String name, String tag, String description, Setting.Visibility visibility, Number value, Number minimum, Number maximum) {
        this(name, tag, description, visibility, value, minimum, maximum, 0);
    }

    public NumberSetting(String name, String tag, String description, Setting.Visibility visibility, Number value, Number minimum, Number maximum, Number step) {
        this(name, tag, description, visibility, value, minimum, maximum, step, false);
    }

    public NumberSetting(String name, String tag, String description, Setting.Visibility visibility, Number value, Number minimum, Number maximum, Number step, boolean zeroIsIgnore) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
        this.zeroIsIgnore = zeroIsIgnore;
    }

    public void setValue(Number value) {
        switch (this.getType().ordinal()) {
            case 1: {
                this.value = Math.clamp(value.longValue(), this.minimum.longValue(), this.maximum.longValue());
                break;
            }
            case 2: {
                this.value = Math.clamp(value.doubleValue(), this.minimum.doubleValue(), this.maximum.doubleValue());
                break;
            }
            case 3: {
                this.value = Float.valueOf(Math.clamp(value.floatValue(), this.minimum.floatValue(), this.maximum.floatValue()));
                break;
            }
            default: {
                this.value = Math.clamp((long)value.intValue(), this.minimum.intValue(), this.maximum.intValue());
            }
        }
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public Type getType() {
        if (this.defaultValue.getClass() == Long.class) {
            return Type.LONG;
        }
        if (this.defaultValue.getClass() == Double.class) {
            return Type.DOUBLE;
        }
        if (this.defaultValue.getClass() == Float.class) {
            return Type.FLOAT;
        }
        return Type.INTEGER;
    }

    @Generated
    public Number getValue() {
        return this.value;
    }

    @Generated
    public Number getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public Number getMinimum() {
        return this.minimum;
    }

    @Generated
    public Number getMaximum() {
        return this.maximum;
    }

    @Generated
    public Number getStep() {
        return this.step;
    }

    @Generated
    public boolean isZeroIsIgnore() {
        return this.zeroIsIgnore;
    }

    public static enum Type {
        INTEGER,
        LONG,
        DOUBLE,
        FLOAT;

    }

    public static class Visibility
    extends Setting.Visibility {
        private final NumberSetting value;
        private final Number targetValue;
        private final Condition condition;

        public Visibility(NumberSetting value, Number targetValue, Condition condition) {
            super(value);
            this.value = value;
            this.targetValue = targetValue;
            this.condition = condition;
        }

        @Override
        public void update() {
            if (this.value.getVisibility() != null) {
                this.value.getVisibility().update();
                if (!this.value.getVisibility().isVisible()) {
                    this.setVisible(false);
                    return;
                }
            }
            if (this.value.getType() == Type.INTEGER) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().intValue() == this.targetValue.intValue() : (this.condition == Condition.SMALLER ? this.value.getValue().intValue() < this.targetValue.intValue() : this.value.getValue().intValue() > this.targetValue.intValue()));
            }
            if (this.value.getType() == Type.LONG) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().longValue() == this.targetValue.longValue() : (this.condition == Condition.SMALLER ? this.value.getValue().longValue() < this.targetValue.longValue() : this.value.getValue().longValue() > this.targetValue.longValue()));
            }
            if (this.value.getType() == Type.DOUBLE) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().doubleValue() == this.targetValue.doubleValue() : (this.condition == Condition.SMALLER ? this.value.getValue().doubleValue() < this.targetValue.doubleValue() : this.value.getValue().doubleValue() > this.targetValue.doubleValue()));
            }
            if (this.value.getType() == Type.FLOAT) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().floatValue() == this.targetValue.floatValue() : (this.condition == Condition.SMALLER ? this.value.getValue().floatValue() < this.targetValue.floatValue() : this.value.getValue().floatValue() > this.targetValue.floatValue()));
            }
        }

        public static enum Condition {
            EQUALS,
            SMALLER,
            BIGGER;

        }
    }
}

