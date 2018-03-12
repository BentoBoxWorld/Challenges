package bskyblock.addon.challenges.panel;

import bskyblock.addon.challenges.ChallengesAddon;
import us.tastybento.bskyblock.api.panels.builders.PanelBuilder;
import us.tastybento.bskyblock.api.user.User;

public class CreateChallengePanel {

    public CreateChallengePanel(ChallengesAddon addon, User user) {
        new PanelBuilder().size(49).listener(new CreateChallengeListener(addon, user)).user(user).build();
    }    
    
}
