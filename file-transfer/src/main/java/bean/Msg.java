package bean;

import com.google.common.io.ByteArrayDataInput;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class Msg {

    private Header header;

    private ByteArrayDataInput dataInput;
}
