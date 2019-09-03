//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.config;


/**
 * This class holds all enums that are used in Settings class.
 */
public class SettingsUtils
{
	/**
	 * This enum holds all possible values for Gui Opening for users.
	 */
	public enum GuiMode
	{
		/**
		 * Opens user GUI with list of all GameModes.
		 */
		GAMEMODE_LIST,
		/**
		 * Opens user GUI with challenges in given world.
		 */
		CURRENT_WORLD
	}


	/**
	 * This enum holds all possible values for Challenge Lore Message.
	 */
	public enum ChallengeLore
	{
		/**
		 * Level String: 'challenges.gui.challenge-description.level'
		 */
		LEVEL,

		/**
		 * Status String: 'challenges.gui.challenge-description.completed'
		 */
		STATUS,

		/**
		 * Completion Times String:
		 * 		'challenges.gui.challenge-description.completed-times',
		 * 		'challenges.gui.challenge-description.completed-times-of'
		 * 		'challenges.gui.challenge-description.maxed-reached'
		 */
		COUNT,

		/**
		 * Description String: defined in challenge object - challenge.description
		 */
		DESCRIPTION,

		/**
		 * Warning String:
		 * 		'challenges.gui.challenge-description.warning-items-take'
		 * 		'challenges.gui.challenge-description.objects-close-by'
		 * 		'challenges.gui.challenge-description.warning-entities-kill'
		 * 		'challenges.gui.challenge-description.warning-blocks-remove'
		 */
		WARNINGS,

		/**
		 * Environment String: defined in challenge object - challenge.environment
		 */
		ENVIRONMENT,

		/**
		 * Requirement String:
		 * 		'challenges.gui.challenge-description.required-level'
		 * 		'challenges.gui.challenge-description.required-money'
		 * 		'challenges.gui.challenge-description.required-experience'
		 * 		and challenge.requiredItems, challenge.requiredBlocks or challenge.requiredEntities
		 */
		REQUIREMENTS,

		/**
		 * Reward String: message that is defined in challenge.rewardTest and challenge.repeatRewardText
		 */
		REWARD_TEXT,

		/**
		 * Reward other String:
		 * 		'challenges.gui.challenge-description.experience-reward'
		 * 		'challenges.gui.challenge-description.money-reward'
		 * 		'challenges.gui.challenge-description.not-repeatable'
		 */
		REWARD_OTHER,

		/**
		 * Reward Items: List of items that will be rewarded defined in challenge.rewardItems and
		 * challenge.repeatRewardItems.
		 */
		REWARD_ITEMS,

		/**
		 * Reward commands: List of commands that will be rewarded defined in challenge.rewardCommands
		 * and challenge.repeatRewardCommands.
		 */
		REWARD_COMMANDS,
	}


	/**
	 * This enum holds all possible values for Level Lore Message.
	 */
	public enum LevelLore
	{
		/**
		 * Status String: 'challenges.gui.level-description.completed'
		 */
		LEVEL_STATUS,

		/**
		 * Completed Challenge count String: 'challenges.gui.level-description.completed-challenges-of'
		 */
		CHALLENGE_COUNT,

		/**
		 * Unlock message String: defined in challenge level object - challengeLevel.unlockMessage
		 */
		UNLOCK_MESSAGE,

		/**
		 * Count of challenges which can be skipped to unlock next level string:
		 * 		'challenges.gui.level-description.waver-amount'
		 */
		WAIVER_AMOUNT,

		/**
		 * Reward String: message that is defined in challengeLevel.rewardTest
		 */
		LEVEL_REWARD_TEXT,

		/**
		 * Reward other String:
		 * 		'challenges.gui.level-description.experience-reward'
		 * 		'challenges.gui.level-description.money-reward'
		 */
		LEVEL_REWARD_OTHER,

		/**
		 * Reward Items: List of items that will be rewarded defined in challengeLevel.rewardItems.
		 */
		LEVEL_REWARD_ITEMS,

		/**
		 * Reward commands: List of commands that will be rewarded defined in challengeLevel.rewardCommands.
		 */
		LEVEL_REWARD_COMMANDS,
	}
}