package world.bentobox.challenges.panel;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * @deprecated All panels are reworked.
 */
@Deprecated
public class CreateChallengePanel {

    public CreateChallengePanel(ChallengesAddon addon, User user) {
        new PanelBuilder().size(49).listener(new CreateChallengeListener(addon, user)).user(user).build();
    }    
    
}
