package application.adapters.persistence.jpa;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * In-memory stand-in for a Spring Data JPA repository, for tests without MySQL.
 * It answers findById, findAll, save, saveAll and existsById, and derives the other
 * query methods from their names, as Spring Data does: findByOrderId,
 * existsBySellerIdAndNameIgnoreCase, findByStatus...
 */
final class FakeSpringDataRepository {

    private FakeSpringDataRepository() {
    }

    @SuppressWarnings("unchecked")
    static <R> R of(Class<R> repositoryInterface) {
        Map<Object, Object> rows = new LinkedHashMap<>();
        return (R) Proxy.newProxyInstance(repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                (proxy, method, args) -> answer(rows, method, args));
    }

    private static Object answer(Map<Object, Object> rows, Method method, Object[] args) throws Exception {
        String name = method.getName();
        switch (name) {
            case "findById":
                return Optional.ofNullable(rows.get(args[0]));
            case "existsById":
                return rows.containsKey(args[0]);
            case "findAll":
                return new ArrayList<>(rows.values());
            case "save":
                rows.put(read(args[0], "Id"), args[0]);
                return args[0];
            case "saveAll":
                List<Object> saved = new ArrayList<>();
                for (Object entity : (Iterable<?>) args[0]) {
                    rows.put(read(entity, "Id"), entity);
                    saved.add(entity);
                }
                return saved;
            case "toString":
                return "FakeSpringDataRepository" + rows.keySet();
            case "hashCode":
                return System.identityHashCode(rows);
            case "equals":
                return false;
            default:
                return derived(rows, method, args);
        }
    }

    private static Object derived(Map<Object, Object> rows, Method method, Object[] args) {
        String name = method.getName();
        String criteria;
        if (name.startsWith("findBy"))
            criteria = name.substring("findBy".length());
        else if (name.startsWith("existsBy"))
            criteria = name.substring("existsBy".length());
        else
            throw new UnsupportedOperationException(name);

        String[] properties = criteria.split("And");
        Predicate<Object> matches = entity -> {
            for (int i = 0; i < properties.length; i++) {
                String property = properties[i];
                boolean ignoreCase = property.endsWith("IgnoreCase");
                if (ignoreCase)
                    property = property.substring(0, property.length() - "IgnoreCase".length());
                Object value = read(entity, property);
                Object expected = args[i];
                boolean equal = ignoreCase
                        ? value != null && ((String) value).equalsIgnoreCase((String) expected)
                        : value != null && value.equals(expected);
                if (!equal)
                    return false;
            }
            return true;
        };

        List<Object> found = rows.values().stream().filter(matches).toList();
        Class<?> returned = method.getReturnType();
        if (returned == boolean.class)
            return !found.isEmpty();
        if (returned == Optional.class)
            return found.stream().findFirst();
        return new ArrayList<>(found);
    }

    private static Object read(Object entity, String property) {
        try {
            return entity.getClass().getMethod("get" + property).invoke(entity);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No property " + property + " in " + entity.getClass(), e);
        }
    }
}