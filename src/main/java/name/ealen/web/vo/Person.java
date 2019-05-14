package name.ealen.web.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Created by EalenXie on 2019/4/17 16:37.
 */
@Data
public class Person {

    @NotEmpty
    private String username;

    @NotNull
    private Integer age;

}
