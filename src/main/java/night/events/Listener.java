/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import lombok.Generated;
import night.Night;
import night.events.SubscribeEvent;

public class Listener {
    private static Method lookupMethod;
    private final Class<?> subscriber;
    private Consumer<Object> consumer;
    private final int priority;

   public Listener(Class<?> klass, Object object, Method method) {
      this.subscriber = method.getParameters()[0].getType();
      this.priority = method.getAnnotation(SubscribeEvent.class).priority();

      try {
         MethodType type = MethodType.methodType(void.class, method.getParameters()[0].getType());
         MethodHandle handle = MethodHandles.lookup().findVirtual(klass, method.getName(), type);
         MethodType invokedType = MethodType.methodType(Consumer.class, klass);
         this.consumer = (Consumer)LambdaMetafactory.metafactory(
               MethodHandles.lookup(), "accept", invokedType, MethodType.methodType(void.class, Object.class), handle, type
            )
            .getTarget()
            .invoke((Object)object);
      } catch (Throwable throwable) {
         Night.LOGGER.error("Fast event dispatch failed for " + klass.getName() + "." + method.getName() + ", falling back to reflection", throwable);
         if (!method.canAccess(object)) {
            method.setAccessible(true);
         }

         this.consumer = new Listener.ReflectiveFallbackConsumer(method, object);
      }
   }

    public void invoke(Object event) {
        this.consumer.accept(event);
    }

    @Generated
    public Class<?> getSubscriber() {
        return this.subscriber;
    }

    @Generated
    public Consumer<Object> getConsumer() {
        return this.consumer;
    }

    @Generated
    public int getPriority() {
        return this.priority;
    }

    private static final class ReflectiveFallbackConsumer
    implements Consumer<Object> {
        private final Method method;
        private final Object target;

        private ReflectiveFallbackConsumer(Method method, Object target) {
            this.method = method;
            this.target = target;
        }

        @Override
        public void accept(Object event) {
            try {
                this.method.invoke(this.target, event);
            }
            catch (Exception exception) {
                Night.LOGGER.error("The Event System threw an exception!", (Throwable)exception);
            }
        }
    }
}

