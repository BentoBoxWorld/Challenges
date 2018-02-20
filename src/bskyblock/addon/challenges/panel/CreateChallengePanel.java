package bskyblock.addon.challenges.panel;

import bskyblock.addon.challenges.Challenges;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.api.panels.builders.PanelBuilder;

public class CreateChallengePanel {

    public CreateChallengePanel(Challenges addon, User user) {
        new PanelBuilder().setSize(49).setListener(new CreateChallengeListener(addon, user)).setUser(user).build();
    }    
    
}
