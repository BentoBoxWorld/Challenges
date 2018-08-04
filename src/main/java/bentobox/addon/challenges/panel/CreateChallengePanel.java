package bentobox.addon.challenges.panel;

import bentobox.addon.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;

public class CreateChallengePanel {

    public CreateChallengePanel(ChallengesAddon addon, User user) {
        new PanelBuilder().size(49).listener(new CreateChallengeListener(addon, user)).user(user).build();
    }    
    
}
