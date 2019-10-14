package bean;

import com.google.common.io.ByteArrayDataInput;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class UpgradeMsg implements Serializable {

    private static final long serialVersionUID = -3902348559083477437L;

    private byte[] header;

    private byte[] data;

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
