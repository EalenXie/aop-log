package name.ealen.demo.controller;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author EalenXie create on 2020/6/24 16:16
 * XML 请求数据测试
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "xml")
public class XmlDataDTO {
    private static final long serialVersionUID = 7653880329569917088L;
    @XmlElement
    private String message;
    @XmlElement
    private String username;
}
