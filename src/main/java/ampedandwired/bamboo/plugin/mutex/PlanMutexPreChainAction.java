package ampedandwired.bamboo.plugin.mutex;

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.plugins.PreChainAction;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.TopLevelPlan;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class PlanMutexPreChainAction implements PreChainAction {
    public static final Logger log = Logger.getLogger(PlanMutexPreChainAction.class);
    public static final String PLAN_MUTEX_KEY = "custom.bamboo.planMutex.list";
    private PlanManager planManager;

    @Override
    public void execute(@NotNull Chain chain, @NotNull ChainExecution chainExecution) throws Exception {
        PlanKey thisPlanKey = PlanKeys.getPlanKey(chain.getKey());
        Map<String, String> customConfig = chain.getBuildDefinition().getCustomConfiguration();
        String planMutexKey = customConfig.get(PLAN_MUTEX_KEY);
        if (planMutexKey != null) {
            PlanKey planToWaitFor = isPlanWithMutexKeyExecuting(planMutexKey, thisPlanKey);
            while (planToWaitFor != null) {
                log.info("Waiting for plan " + planToWaitFor + " holding mutex '" + planMutexKey + "' to complete before executing " + thisPlanKey);
                Thread.sleep(1000);
                planToWaitFor = isPlanWithMutexKeyExecuting(planMutexKey, thisPlanKey);
            }
            log.info("No plans with mutex '" + planMutexKey + "' are currently building. Proceeding with build of " + thisPlanKey);
        }
    }

    private PlanKey isPlanWithMutexKeyExecuting(String mutexKeyToCheck, PlanKey excludePlan) {
        List<TopLevelPlan> allPlans = planManager.getAllPlans();
        for (TopLevelPlan plan : allPlans) {
            String planMutexKey = plan.getBuildDefinition().getCustomConfiguration().get(PLAN_MUTEX_KEY);
            if (planMutexKey != null && mutexKeyToCheck.equals(planMutexKey) && !excludePlan.equals(plan.getPlanKey())) {
                if (isPlanCurrentlyExecuting(plan.getPlanKey())) {
                    return plan.getPlanKey();
                }
            }
        }

        return null;
    }

    private boolean isPlanCurrentlyExecuting(PlanKey planKey) {
        Plan plan = planManager.getPlanByKey(planKey);
        return plan.isExecuting() || plan.isActive();
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }
}
