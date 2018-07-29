package bskyblock.addon.challenges.panel;

import bskyblock.addon.challenges.ChallengesAddon;
import world.bentobox.bbox.api.panels.builders.PanelBuilder;
import world.bentobox.bbox.api.user.User;

public class CreateChallengePanel {

    public CreateChallengePanel(ChallengesAddon addon, User user) {
        new PanelBuilder().size(49).listener(new CreateChallengeListener(addon, user)).user(user).build();
    }    
    
}
