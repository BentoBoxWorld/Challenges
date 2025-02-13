package world.bentobox.challenges.database.object.adapters;

import com.google.gson.*;
import org.bukkit.advancement.Advancement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AdvancementsListAdapter implements JsonSerializer<List<Advancement>>, JsonDeserializer<List<Advancement>> {

    // Reuse your existing adapter for individual advancements
    private final AdvancementsAdapter advancementAdapter = new AdvancementsAdapter();

    @Override
    public JsonElement serialize(List<Advancement> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (Advancement advancement : src) {
            // Serialize each advancement using existing adapter
            JsonElement element = advancementAdapter.serialize(advancement, advancement.getClass(), context);
            array.add(element);
        }
        return array;
    }

    @Override
    public List<Advancement> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        List<Advancement> advancements = new ArrayList<>();
        JsonArray array = json.getAsJsonArray();
        for (JsonElement element : array) {
            Advancement advancement = advancementAdapter.deserialize(element, Advancement.class, context);
            if (advancement != null) {
                advancements.add(advancement);
            }
        }
        return advancements;
    }
}