//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.adapters;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.database.objects.adapters.AdapterInterface;
import world.bentobox.challenges.config.SettingsUtils.LevelLore;


/**
 * This adapter allows to serialize and deserialize LevelLore object.
 */
public class LevelLoreAdapter implements AdapterInterface<List<LevelLore>, List<String>>
{
	@SuppressWarnings("unchecked")
	@Override
	public List<LevelLore> deserialize(Object from)
	{
		List<LevelLore> result;

		if (from instanceof List)
		{
			result = ((List<String>) from).stream().
				map(LevelLore::valueOf).
				collect(Collectors.toCollection(ArrayList::new));
		}
		else
		{
			result = new ArrayList<>(0);
		}

		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<String> serialize(Object to)
	{
		List<String> result;

		if (to instanceof List)
		{
			result = ((List<LevelLore>) to).stream().
				map(LevelLore::name).
				collect(Collectors.toCollection(ArrayList::new));
		}
		else
		{
			result = new ArrayList<>(0);
		}

		return result;
	}
}
