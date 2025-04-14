package tsubaki.interpreter;

import java.io.IOException;
import java.util.Map;

public class ExternalProcessFactory {
    public static ExternalProcess create_process(
            String exe_path,
            Map<String, String> arguments
    ) throws IOException {
        return new ExternalProcess(exe_path, arguments);
    }
}