package dev.tr7zw.entityculling.api.fixes;

import java.util.ArrayList;
import java.util.List;

/**
 * Api for request EntityCulling to stop apply {@link dev.tr7zw.entityculling.mixin.CameraMixin} third-person fix
 */
public class CameraThirdPersonFixApi {
    private static final List<Object> requestersToDisableFix = new ArrayList<>();

    public static void requestToDisable(Object requester) {
        requestersToDisableFix.add(requester);
    }

    public static void reject(Object requester) {
        requestersToDisableFix.remove(requester);
    }

    public static boolean isFixEnable() {
        return requestersToDisableFix.isEmpty();
    }
}
