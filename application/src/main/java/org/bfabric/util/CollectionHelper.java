/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.AbstractNamedBaseEntity;
import org.bfabric.interceptors.CachedMethodResult;

@Named
@ViewScoped
public class CollectionHelper implements Serializable {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(CollectionHelper.class.getName());

    // IMPORTANT: Do not make this method static since it is only used in JSF!
    public static <T> List<T> asList(final Collection<T> collection) {
        return collection != null && !collection.isEmpty() ? new ArrayList<>(collection) : new ArrayList<>();
    }

    // IMPORTANT: Do not make this method static since it is only used in JSF!
    public static <T> Set<T> asSet(final Collection<T> collection) {
        return collection != null && !collection.isEmpty() ? new HashSet<>(collection) : new HashSet<>();
    }

    public static String getCheckboxLayout(List<AbstractNamedBaseEntity> entities) {
        int nameLength = 0;
        for (AbstractNamedBaseEntity entity : entities) {
            // Add a five character placeholder for the checkbox itself.
            nameLength += entity.getName().length() + 5;
        }
        return nameLength <= 100 ? "lineDirection" : "pageDirection";
    }

    public static String getSeparator(final String separator) {
        return separator == null ? ", " : separator;
    }

    public static boolean isCollectionAllFalse(Collection<Boolean> collection) {
        return isCollectionAllFalseOrTrue(collection, true);
    }

    private static boolean isCollectionAllFalseOrTrue(Collection<Boolean> collection, boolean onlyFalse) {
        if (collection != null) {
            for (Boolean value : collection) {
                if (onlyFalse == value) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public static boolean isCollectionAllTrue(Collection<Boolean> collection) {
        return isCollectionAllFalseOrTrue(collection, false);
    }

    public static String print(final Collection<?> collection) {
        return print(collection, null);
    }

    public static String print(final Collection<?> collection, boolean apostropheWrap) {
        return print(collection, null, null, apostropheWrap);
    }

    public static String print(final Collection<?> collection, String methodName) {
        return print(collection, methodName, null, false);
    }

    public static String print(final Collection<?> collection, String methodName, String separator, boolean apostropheWrap) {
        String ret = null;
        if (collection != null && !collection.isEmpty()) {
            String finalSeparator = getSeparator(separator);

            StringBuilder stringBuilder = new StringBuilder();
            for (Object object : collection) {
                String value = Constants.NULL;
                if (object != null) {
                    Class<?> clazz = object.getClass();
                    Method method = null;
                    if (object instanceof AbstractEntity) {
                        try {
                            method = clazz.getMethod(methodName != null ? methodName : "getName");
                        } catch (NoSuchMethodException | SecurityException e1) {
                            try {
                                // Default method is getName
                                method = clazz.getMethod("getName");
                            } catch (NoSuchMethodException | SecurityException e2) {
                                logger.fine("Class " + clazz + " has no method named " + methodName + " or getName");
                            }
                        }
                    }
                    value = object.toString();
                    if (method != null) {
                        try {
                            value = method.invoke(object, (Object[]) null).toString();
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            logger.fine("Method " + method.getName() + " cannot be invoked on object " + object);
                        }
                    }
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(finalSeparator);
                }
                if (apostropheWrap) {
                    stringBuilder.append("'");
                }
                stringBuilder.append(value);
                if (apostropheWrap) {
                    stringBuilder.append("'");
                }
            }
            ret = stringBuilder.toString();
        }

        return ret;
    }

    public static String printBasic(final Collection<?> collection) {
        return printBasic(collection, null);
    }

    public static String printBasic(final Collection<?> collection, String separator) {
        StringBuilder buf = new StringBuilder();
        String finalSeparator = getSeparator(separator);

        if (collection != null && !collection.isEmpty()) {
            for (Iterator<?> item = collection.iterator(); item.hasNext(); ) {
                buf.append(item.next());
                if (item.hasNext()) {
                    buf.append(finalSeparator);
                }
            }
        }

        return buf.toString();
    }

    public static String printDisplayNames(final Collection<?> collection) {
        return print(collection, "getDisplayName");
    }

    public static String printIds(final Collection<?> collection) {
        return print(collection, "getId");
    }

    public static String printNames(final Collection<?> collection) {
        return print(collection, "getName");
    }

    public static String printTypes(final Collection<?> collection) {
        return print(collection, "getType");
    }

    public static <T extends Comparable<T>> List<T> sortObjects(final Collection<T> collection) {
        return sortObjects(new ArrayList<>(collection));
    }

    public static <T extends Comparable<T>> List<T> sortObjects(final List<T> list) {
        try {
            Collections.sort(list);
        } catch (Exception e) {
            logger.severe("Unable to sort objects: " + e);
        }
        return list;
    }

    @CachedMethodResult
    public Iterable<?> getDatatableContent(final Iterable<?> datatableContent, boolean doNothing) {
        if (datatableContent == null) {
            return new ArrayList<>();
        }
        return doNothing || !(datatableContent instanceof Set) ? datatableContent : new ArrayList<>((Collection<?>) datatableContent);
    }
}