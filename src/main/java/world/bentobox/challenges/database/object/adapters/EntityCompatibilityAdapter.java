//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.challenges.database.object.adapters;


import com.google.gson.*;
import org.bukkit.entity.EntityType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

import world.bentobox.bentobox.BentoBox;


/**
 * This is compatibility class for dealing with Mojang renamed entities.
 * Created for update 1.16.
 */
public class EntityCompatibilityAdapter implements
	JsonSerializer<Map<EntityType, Integer>>, JsonDeserializer<Map<EntityType, Integer>>
{
	/**
	 * This method serializes input map as String Key and Integer value.
	 * @param src EnumMap that contains EntityType as key and Integer as value.
	 * @return serialized JsonElement.
	 */
	@Override
	public JsonElement serialize(Map<EntityType, Integer> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject jsonArray = new JsonObject();

		src.forEach((entity, number) ->
		{
			jsonArray.addProperty(entity.name(), number);
		});

		return jsonArray;
	}


	/**
	 * This method deserializes json object that stores Entity Name and amount as integer.
	 * @param json Json element that must be parsed.
	 * @return EnumMap that contains EntityType as key and Integer as value.
	 * @throws JsonParseException
	 */
	@Override
	public Map<EntityType, Integer> deserialize(JsonElement json,
		Type typeOfT,
		JsonDeserializationContext context)
		throws JsonParseException
	{
		Map<EntityType, Integer> map = new EnumMap<>(EntityType.class);

		for (Map.Entry<String, JsonElement> entrySet : json.getAsJsonObject().entrySet())
		{
			try
			{
				EntityType entityType = EntityType.valueOf(entrySet.getKey());
				map.put(entityType, entrySet.getValue().getAsInt());
			}
			catch (IllegalArgumentException e)
			{
				if (entrySet.getKey().equals("PIG_ZOMBIE"))
				{
					// Hacky way how to get new entity name.
					map.put(EntityType.valueOf("ZOMBIFIED_PIGLIN"),
						entrySet.getValue().getAsInt());
				}
				else if (entrySet.getKey().equals("ZOMBIFIED_PIGLIN"))
				{
					// Hacky way how to get new entity name.
					map.put(EntityType.valueOf("PIG_ZOMBIE"),
						entrySet.getValue().getAsInt());
				}
				else
				{
					// No replacement for new entities in older server.
					BentoBox.getInstance().logWarning("[ChallengesAddon] Entity with name `" +
						entrySet.getKey() + "` does not exist in your Minecraft server version." +
						" It will be skipped!");
				}
			}
		}

		return map;
	}
}