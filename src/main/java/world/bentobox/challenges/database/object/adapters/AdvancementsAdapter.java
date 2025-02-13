package world.bentobox.challenges.database.object.adapters;


import java.lang.reflect.Type;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class AdvancementsAdapter implements JsonSerializer<Advancement>, JsonDeserializer<Advancement>
{
	@Override
    public JsonElement serialize(Advancement src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();
        result.add("name", new JsonPrimitive(src.getKey().getKey()));
		return result;
	}

	@Override
    public Advancement deserialize(JsonElement json,
		Type typeOfT,
		JsonDeserializationContext context)
		throws JsonParseException
	{
		JsonObject jsonObject = json.getAsJsonObject();
		String name = jsonObject.get("name").getAsString();

        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(Bukkit.advancementIterator(), Spliterator.ORDERED), false)
                .filter(a -> a.getKey().getKey().equals(name)).findFirst().orElse(null);
    }

}