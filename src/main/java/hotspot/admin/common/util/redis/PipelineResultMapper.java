package hotspot.admin.common.util.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PipelineResultMapper {

    private PipelineResultMapper() {}

    public static Map<String, Object> toMap(
            List<String> requestKeys,
            List<Object> rawResults
    ) {

        List<Object> raw =
                rawResults == null ? List.of() : rawResults;

        Map<String, Object> resultMap =
                new HashMap<>(requestKeys.size());

        int size = Math.min(requestKeys.size(), raw.size());

        for (int i = 0; i < size; i++) {
            resultMap.put(requestKeys.get(i), raw.get(i));
        }

        for (int i = size; i < requestKeys.size(); i++) {
            resultMap.put(requestKeys.get(i), null);
        }

        return resultMap;
    }
}
