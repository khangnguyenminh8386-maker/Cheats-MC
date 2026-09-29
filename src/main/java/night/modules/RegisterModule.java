/*
 * Decompiled with CFR 0.152.
 */
package night.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import night.modules.Module;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface RegisterModule {
    public String name();

    public String description() default "No description.";

    public Module.Category category();

    public boolean persistent() default false;

    public boolean toggled() default false;

    public boolean drawn() default true;

    public int bind() default 0;

    public boolean proxyEnhanced() default false;
}

