package cn.i.search.core.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class CommonUtil {

	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	private static ObjectMapper jacksonJsonMapper = new ObjectMapper();

	public CommonUtil() {

	}

	/**
	 * 判断字符串是否为空字符串
	 * 
	 * @param arg
	 * @return
	 */
	public static boolean isNullOrEmpty(String arg) {
		return Strings.isNullOrEmpty(arg) || arg.trim().length() < 1;
	}

	/**
	 * 将空转化为空字符串，如果不为空将去掉前后空格
	 * 
	 * @param arg
	 * @return
	 */
	public static String nullToEmpty(String arg) {
		if (null == arg) {
			return "";
		}
		return arg.trim();
	}

	/**
	 * 将对象转化为字符串
	 * 
	 * @param 0
	 * @return
	 */
	public static String objectToString(Object o) {
		if (o != null) {
			return String.valueOf(o).trim();
		}
		return "";
	}

	/**
	 * Map转化为Bean 1、public class才能使用 2、需要无参的构造函数
	 * 
	 * @param map
	 * @param obj
	 */
	public static Object transMap2Bean(Map<String, Object> map, Class<?> beanClass) {
		if (map == null) {
			return null;
		}
		if (map.size() < 1) {
			return null;
		}
		try {
			Object obj = beanClass.newInstance();
			BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor property : propertyDescriptors) {
				String key = property.getName();
				if (map.containsKey(key)) {
					Object value = map.get(key);
					Method setter = property.getWriteMethod();
					if (null != setter) {
						setter.invoke(obj, value);
					}
				}
			}

		} catch (IntrospectionException e) {
			logger.error("Map2Bean error!", e);
		} catch (IllegalAccessException e) {
			logger.error("Map2Bean error!", e);
		} catch (IllegalArgumentException e) {
			logger.error("Map2Bean error!", e);
		} catch (InvocationTargetException e) {
			logger.error("Map2Bean error!", e);
		} catch (InstantiationException e) {
			logger.error("Map2Bean error!", e);
		}
		return null;
	}

	/**
	 * 利用refect完成obj和Map之间转换 1、public class才能使用 2、需要无参的构造函数
	 * 
	 * @param map
	 * @param beanClass
	 * @return
	 */
	public static Object transMapToBean(Map<String, Object> map, Class<?> beanClass) {
		if (map == null) {
			return null;
		}
		if (map.isEmpty()) {
			return null;
		}

		try {
			Object obj = beanClass.newInstance();
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				int mod = field.getModifiers();
				if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
					continue;
				}
				field.setAccessible(true);
				field.set(obj, map.get(field.getName()));
			}
			return obj;
		} catch (InstantiationException e) {
			logger.error("Map2Bean error!", e);
		} catch (IllegalAccessException e) {
			logger.error("Map2Bean error!", e);
		}
		return null;
	}

	/**
	 * 根据提供的字段名称去获取对象该字段的值
	 * 
	 * @param fieldName
	 *            字段名称
	 * @param o
	 *            对象
	 * @return
	 * @throws SearchEngineException
	 */
	public static Object getFieldValueByName(String fieldName, Object o) {
		if (o != null && !"".equals(nullToEmpty(fieldName))) {
			try {
				String firstLetter = fieldName.substring(0, 1).toUpperCase();
				StringBuilder getter = new StringBuilder().append("get").append(firstLetter)
						.append(fieldName.substring(1));
				Method method = o.getClass().getMethod(getter.toString(), new Class[] {});
				method.setAccessible(true);
				return method.invoke(o, new Object[] {});
			} catch (NoSuchMethodException e) {
				logger.error("没有对应的方法", e);
			} catch (IllegalAccessException e) {
				logger.error("没有对应的方法", e);
			} catch (IllegalArgumentException e) {
				logger.error("没有对应的方法", e);
			} catch (InvocationTargetException e) {
				logger.error("没有对应的方法", e);
			}
		}
		return null;
	}

	/**
	 * 将对象转化为jsonStr
	 * 
	 * @param o
	 * @return
	 */
	public static String toJsonString(Object o) {
		try {
			return jacksonJsonMapper.writeValueAsString(o);
		} catch (JsonGenerationException e) {
			logger.error("JSON工具转化对象方法出错", e);
		} catch (JsonMappingException e) {
			logger.error("JSON工具转化对象方法出错", e);
		} catch (IOException e) {
			logger.error("JSON工具转化对象方法出错", e);
		}
		return "";
	}

}
