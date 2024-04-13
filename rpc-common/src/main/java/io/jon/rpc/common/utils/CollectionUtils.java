package io.jon.rpc.common.utils;

import java.util.Collection;

/**
 * @description 集合工具类
 */
public class CollectionUtils {

    /**
     * 判断两个集合是否相等，如果T是对象类型，则需要重写对象的hashcode()和equals()方法
     */
    public static <T> boolean equals(Collection<T> c1, Collection<T> c2){
        if (isEmpty(c1) || isEmpty(c2)) return false;
        return c1.size() == c2.size() && c1.containsAll(c2) && c2.containsAll(c1);
    }

    public static <T> boolean isEmpty(Collection<T> c){
        return c == null || c.isEmpty();
    }
}
