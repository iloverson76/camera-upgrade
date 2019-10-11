package bean;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class Head implements Serializable {

    private static final long serialVersionUID = 6776721485184922437L;

    /**
     * 总长度=head.length+msg.length?
     */
    private int length;

    /**
     * COMMAND
     */
    private int command;

}
