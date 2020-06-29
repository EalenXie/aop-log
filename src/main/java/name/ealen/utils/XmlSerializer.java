package name.ealen.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by EalenXie on 2019/7/15 13:35.
 * 数据序列化工具类
 * 依赖 JAXBContext{@link JAXBContext}
 */
public enum XmlSerializer {
    ;

    /**
     * 将XML的字符串(String)转换成Java对象
     *
     * @param target XML字符串
     * @param clazz  要转换成的对象类型
     */
    public static Object xmlToJavaBean(String target, Class<?> clazz) throws JAXBException {
        return JAXBContext.newInstance(clazz).createUnmarshaller().unmarshal(new StringReader(target));
    }

    /**
     * 将XML文件数据反序列化为JavaBean对象(该文件对象的属性必须和Java对象类型的属性一一对应)
     *
     * @param target XML文件对象
     * @param clazz  Java对象类型
     */
    public static Object xmlFileToJavaBean(File target, Class<?> clazz) throws JAXBException {
        return JAXBContext.newInstance(clazz).createUnmarshaller().unmarshal(target);
    }

    /**
     * 将JavaBean对象转成XML字符串
     *
     * @param target 要转换的对象引用(xxx)
     * @param clazz  要转换的对象类型(xxx.class)
     */
    public static String javaBeanToXml(Object target, Class<?> clazz) throws JAXBException {
        StringWriter writer = new StringWriter();
        Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(target, writer);
        return writer.toString().replace("standalone=\"yes\"", "");
    }
}
