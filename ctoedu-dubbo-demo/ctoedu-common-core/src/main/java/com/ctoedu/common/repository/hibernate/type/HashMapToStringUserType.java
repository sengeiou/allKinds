package com.ctoedu.common.repository.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 将List转换为指定分隔符分隔的字符串存储 List的元素类型只支持常见的数据类型 可参考{@link org.apache.commons.beanutils.ConvertUtilsBean}
 *
 * Date:2016年11月4日 下午4:47:29
 * Version:1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class HashMapToStringUserType implements UserType, ParameterizedType, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -616342186975836917L;
	/**
     * 默认 java.lang.String
     */
    
	private Class keyType;

    public void setParameterValues(Properties parameters) {
        String keyType = (String) parameters.get("keyType");
        if (!StringUtils.isEmpty(keyType)) {
            try {
                this.keyType = Class.forName(keyType);
            } catch (ClassNotFoundException e) {
                throw new HibernateException(e);
            }
        } else {
            this.keyType = String.class;
        }


    }

    public int[] sqlTypes() {
        return new int[]{Types.VARCHAR};
    }

    public Class returnedClass() {
        return HashMap.class;
    }

    public boolean equals(Object o, Object o1) throws HibernateException {
        if (o == o1) {
            return true;
        }
        if (o == null || o == null) {
            return false;
        }

        return o.equals(o1);
    }

    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }


    /**
     * 从JDBC ResultSet读取数据,将其转换为自定义类型后返回
     * (此方法要求对克能出现null值进行处理)
     * names中包含了当前自定义类型的映射字段名称
     *
     * @param names
     * @param owner
     * @return
     * @throws HibernateException
     * @throws SQLException
     */
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String valueStr = rs.getString(names[0]);
        if (StringUtils.isEmpty(valueStr)) {
            return newMap();
        }

        Map map = JSONObject.parseObject(valueStr);
        Map result = newMap();
        try {
            for(Object key : map.keySet()) {
                Object value = map.get(key);
                result.put(keyType.getConstructor(String.class).newInstance(key), value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new HibernateException(e);
        }
        return result;
    }

    /**
     * 本方法将在Hibernate进行数据保存时被调用
     * 我们可以通过PreparedStateme将自定义数据写入到对应的数据库表字段
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        String valueStr;
        if (value == null) {
            valueStr = "";
        } else {
            valueStr = JSONObject.toJSONString(value, SerializerFeature.WriteClassName);
        }
        st.setString(index, valueStr);
    }


    private Map newMap() {
        try {
            return HashMap.class.newInstance();
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    /**
     * 提供自定义类型的完全复制方法
     * 本方法将用构造返回对象
     * 当nullSafeGet方法调用之后，我们获得了自定义数据对象，在向用户返回自定义数据之前，
     * deepCopy方法将被调用，它将根据自定义数据对象构造一个完全拷贝，并将此拷贝返回给用户
     * 此时我们就得到了自定义数据对象的两个版本，第一个是从数据库读出的原始版本，其二是我们通过
     * deepCopy方法构造的复制版本，原始的版本将有Hibernate维护，复制版由用户使用。原始版本用作
     * 稍后的脏数据检查依据；Hibernate将在脏数据检查过程中将两个版本的数据进行对比（通过调用
     * equals方法），如果数据发生了变化（equals方法返回false），则执行对应的持久化操作
     *
     * @param o
     * @return
     * @throws HibernateException
     */
    public Object deepCopy(Object o) throws HibernateException {
        if (o == null) return null;
        Map copyMap = newMap();
        copyMap.putAll((Map) o);
        return copyMap;
    }

    /**
     * 本类型实例是否可变
     *
     * @return
     */
    public boolean isMutable() {
        return true;
    }

    /* 序列化 */
    public Serializable disassemble(Object value) throws HibernateException {
        return ((Serializable) value);
    }

    /* 反序列化 */
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

}
