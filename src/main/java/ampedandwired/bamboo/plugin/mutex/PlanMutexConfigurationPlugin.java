package ampedandwired.bamboo.plugin.mutex;

import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.atlassian.bamboo.v2.build.BaseBuildConfigurationAwarePlugin;
import com.atlassian.bamboo.v2.build.configuration.MiscellaneousBuildConfigurationPlugin;
import org.jetbrains.annotations.NotNull;

public class PlanMutexConfigurationPlugin extends BaseBuildConfigurationAwarePlugin implements MiscellaneousBuildConfigurationPlugin {
    @Override
    public boolean isApplicableTo(@NotNull Plan plan) {
        return plan instanceof TopLevelPlan;
    }

}
