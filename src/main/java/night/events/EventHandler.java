/*
 * Decompiled with CFR 0.152.
 */
package night.events;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import night.events.Event;
import night.events.Listener;
import night.events.SubscribeEvent;

public class EventHandler {
    private final Map<Object, List<Listener>> listeners = new ConcurrentHashMap<Object, List<Listener>>();
    private final Map<Class<?>, List<Listener>> staticListeners = new ConcurrentHashMap();
    private final Map<Class<?>, List<Listener>> listenerMap = new ConcurrentHashMap();

   public void subscribe(Object object) {
      for (Listener listener : this.getListeners(object.getClass(), object)) {
         this.insert(this.listenerMap.computeIfAbsent(listener.getSubscriber(), new Function<Class<?>, List<Listener>>() {
            public List<Listener> apply(Class<?> aClass) {
               return new CopyOnWriteArrayList<>();
            }
         }), listener);
      }
   }

    public void unsubscribe(Object object) {
        for (Listener listener : this.getListeners(object.getClass(), object)) {
            List<Listener> listeners = this.listenerMap.get(listener.getSubscriber());
            if (listeners == null) continue;
            listeners.remove(listener);
        }
    }

    public void post(Event event) {
        List<Listener> listeners = this.listenerMap.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (Listener listener : listeners) {
            listener.invoke(event);
        }
    }

    public boolean isListening(Class<?> eventKlass) {
        List<Listener> listeners = this.listenerMap.get(eventKlass);
        return listeners != null && !listeners.isEmpty();
    }

   private List<Listener> getListeners(final Class<?> klass, final Object object) {
      Function<Object, List<Listener>> func = new Function<Object, List<Listener>>() {
         public List<Listener> apply(Object o) {
            List<Listener> listeners = new CopyOnWriteArrayList<>();
            EventHandler.this.processListeners(listeners, klass, object);
            return listeners;
         }
      };
      if (object == null) {
         return this.staticListeners.computeIfAbsent(klass, func);
      }

      for (Object key : this.listeners.keySet()) {
         if (key == object) {
            return this.listeners.get(object);
         }
      }

      List<Listener> appliedListeners = func.apply(object);
      this.listeners.put(object, appliedListeners);
      return appliedListeners;
   }

    private void processListeners(List<Listener> listeners, Class<?> klass, Object object) {
        for (Method method : klass.getDeclaredMethods()) {
            if (!this.isValid(method)) continue;
            listeners.add(new Listener(klass, object, method));
        }
        if (klass.getSuperclass() != null) {
            this.processListeners(listeners, klass.getSuperclass(), object);
        }
    }

    private boolean isValid(Method method) {
        if (!method.isAnnotationPresent(SubscribeEvent.class)) {
            return false;
        }
        if (method.getReturnType() != Void.TYPE) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }
        return !method.getParameters()[0].getType().isPrimitive();
    }

    private void insert(List<Listener> listeners, Listener listener) {
        int index;
        for (index = 0; index < listeners.size() && listener.getPriority() <= listeners.get(index).getPriority(); ++index) {
        }
        listeners.add(index, listener);
    }
}

