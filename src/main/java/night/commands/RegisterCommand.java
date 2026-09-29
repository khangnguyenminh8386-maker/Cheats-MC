/*
 * Decompiled with CFR 0.152.
 */
package night.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface RegisterCommand {
    public String name();

    public String tag() default "";

    public String description() default "No description.";

    public String syntax() default "";

    public String[] aliases() default {};
}

