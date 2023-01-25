package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DerivedAgentBuildHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DerivedAgentBuildHelper.class);

  private DerivedAgentBuildHelper() {}

  /**
   * Build an instance of a class derived from {@code Agent} and set the properties accordingly.
   *
   * <p>The properties are <b>not</b> deeply copied. Instead only references are assigned.
   *
   * @param <A> {@link Agent} extending class
   * @param agent original object of type {@link Agent}
   * @param derivedClazz {@link Class} that will be instantiated and returned
   * @return an instance of {@code derivedClazz} with its properties set to {@code agent}
   */
  public static <A extends Agent> A build(Agent agent, Class<A> derivedClazz) {
    if (agent == null || derivedClazz == null) return null;
    try {
      A derivedInst = derivedClazz.getConstructor().newInstance();
      // collect all the public setters of the new instance
      List<Method> derivedInstSetters =
          Stream.of(derivedInst.getClass().getMethods())
              .filter(m -> m.getName().startsWith("set"))
              .collect(Collectors.toList());
      // go through all the public getters of the passed Agent...
      for (Method agentGetter : agent.getClass().getMethods()) {
        if (!agentGetter.getName().startsWith("get")) continue;
        Type returnType = agentGetter.getGenericReturnType();
        // ...find the corresponding setter of the new object...
        Method[] setters =
            derivedInstSetters.stream()
                .filter(
                    derivSetter ->
                        derivSetter
                                .getName()
                                .equals(agentGetter.getName().replaceFirst("^get", "set"))
                            && derivSetter.getParameterCount() == 1
                            && derivSetter
                                .getParameters()[0]
                                .getParameterizedType()
                                .equals(returnType))
                .toArray(i -> new Method[i]);
        if (setters.length != 1) continue;
        // ...and invoke this setter with the getter's returned value
        setters[0].invoke(derivedInst, agentGetter.invoke(agent));
      }
      return derivedInst;
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      LOGGER.error(
          "Error while building the derived agent instance, reflection threw an exception", e);
      return null;
    }
  }
}
