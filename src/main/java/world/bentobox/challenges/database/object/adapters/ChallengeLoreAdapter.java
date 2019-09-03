//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.database.object.adapters;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.database.objects.adapters.AdapterInterface;
import world.bentobox.challenges.config.SettingsUtils.ChallengeLore;


/**
 * This adapter allows to serialize and deserialize ChallengeLore object.
 */
public class ChallengeLoreAdapter implements AdapterInterface<List<ChallengeLore>, List<String>>
{
	@SuppressWarnings("unchecked")
	@Override
	public List<ChallengeLore> deserialize(Object from)
	{
		List<ChallengeLore> result;

		if (from instanceof List)
		{
			result = ((List<String>) from).stream().
				map(ChallengeLore::valueOf).
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
			result = ((List<ChallengeLore>) to).stream().
				map(ChallengeLore::name).
				collect(Collectors.toCollection(ArrayList::new));
		}
		else
		{
			result = new ArrayList<>(0);
		}

		return result;
	}
}
