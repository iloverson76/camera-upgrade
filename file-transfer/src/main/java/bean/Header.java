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
public class Header implements Serializable {

    private static final long serialVersionUID = 6776721485184922437L;

    /**
     * 总长度=head.length+msg.length?
     */
    private int length;

    /**
     * COMMAND
     */
    private byte[] commandHex;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getCommandHex() {
        return commandHex;
    }

    public void setCommandHex(byte[] commandHex) {
        this.commandHex = commandHex;
    }
}
