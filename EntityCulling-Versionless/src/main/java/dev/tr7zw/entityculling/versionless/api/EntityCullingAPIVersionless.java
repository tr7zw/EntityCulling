package dev.tr7zw.entityculling.versionless.api;

import dev.tr7zw.transition.event.*;
import lombok.*;
import lombok.experimental.*;

@UtilityClass
public final class EntityCullingAPIVersionless {

    @Getter
    private final static Event<CullingProvider> CULLING_EVENT = EventFactory.createEvent();

}
