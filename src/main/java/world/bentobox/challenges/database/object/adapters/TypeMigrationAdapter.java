//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.adapters;


import com.google.gson.*;
import java.lang.reflect.Type;

import world.bentobox.challenges.database.object.Challenge;


/**
 * This is a generic JSON serializer and deserializer for abstract classes.
 * It store target class in class object, and instance variables in variables object.
 */
public class TypeMigrationAdapter implements JsonSerializer<Challenge.ChallengeType>, JsonDeserializer<Challenge.ChallengeType>
{
	/**
	 * Use default enum name serialization.
	 */
	@Override
	public JsonElement serialize(Challenge.ChallengeType src, Type typeOfSrc, JsonSerializationContext context)
	{
		return new JsonPrimitive(src.name());
	}


	/**
	 * Deserialize enum with old type format.
	 */
	@Override
	public Challenge.ChallengeType deserialize(JsonElement json,
		Type typeOfT,
		JsonDeserializationContext context)
		throws JsonParseException
	{
		JsonPrimitive primitive = json.getAsJsonPrimitive();

		return switch (primitive.getAsString())
		{
			case "ISLAND", "ISLAND_TYPE" -> Challenge.ChallengeType.ISLAND_TYPE;
			case "INVENTORY", "INVENTORY_TYPE" -> Challenge.ChallengeType.INVENTORY_TYPE;
			case "OTHER", "OTHER_TYPE" -> Challenge.ChallengeType.OTHER_TYPE;
			case "STATISTIC", "STATISTIC_TYPE" -> Challenge.ChallengeType.STATISTIC_TYPE;
			default -> Challenge.ChallengeType.ISLAND_TYPE;
		};
	}
}